package me.jeremiah.data.storage.databases.byteoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.SortedDatabase;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Database<T> implements Closeable {

  private final ScheduledExecutorService scheduler;

  private final DatabaseInfo info;
  private final boolean useDirtyable;
  private ScheduledFuture<?> autoSaveTask;

  private final Method serialize;
  private final Method deserialize;

  protected Set<T> entries;

  protected final Field idField;
  protected Map<ByteTranslatable, T> entryById;

  protected final Map<String, Field> indexes;
  protected Map<String, Map<ByteTranslatable, T>> indexToEntry;

  protected final SortedDatabase<T> sortedDatabase;

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.info = info;
    this.useDirtyable = Dirtyable.class.isAssignableFrom(entryClass);
    this.serialize = ReflectionUtils.getSerializeMethod(entryClass);
    this.deserialize = ReflectionUtils.getDeserializeMethod(entryClass);
    this.idField = ReflectionUtils.getIdField(entryClass);
    this.indexes = ReflectionUtils.getIndexes(entryClass);
    this.sortedDatabase = new SortedDatabase<>(scheduler, entryClass);
  }

  protected abstract int lookupEntryCount();

  protected void setup() {
    int initialCapacity = lookupEntryCount() * 2;
    entries = ConcurrentHashMap.newKeySet(initialCapacity);
    entryById = new ConcurrentHashMap<>(initialCapacity);
    indexToEntry = new ConcurrentHashMap<>(indexes.size() + 1, 1);
    for (String index : indexes.keySet()) indexToEntry.put(index, new ConcurrentHashMap<>(initialCapacity));
    sortedDatabase.setup();
    loadData();
    sortedDatabase.sort();
    autoSaveTask = scheduler.scheduleAtFixedRate(this::save, info.getAutoSaveInterval(), info.getAutoSaveInterval(), info.getAutoSaveTimeUnit());
  }

  public final void add(T entry) {
    entries.add(entry);
    entryById.put(ReflectionUtils.getId(idField, entry), entry);
    for (Map.Entry<String, Field> index : indexes.entrySet()) {
      indexToEntry.get(index.getKey()).put(
        ReflectionUtils.getIndex(index.getValue(), entry),
        entry
      );
    }
    sortedDatabase.addSorted(entry);
  }

  public final <R> Optional<R> queryById(@NotNull Object id, @NotNull Function<T, R> function) {
    return getById(id).map(function);
  }

  public final <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object key, @NotNull Function<T, R> function) {
    return getByIndex(index, key).map(function);
  }

  public final <R> Optional<R> querySorted(@NotNull String sorted, int index, @NotNull Function<T, R> function) {
    return sortedDatabase.querySorted(sorted, index, function);
  }

  public final Optional<T> updateById(@NotNull Object id, @NotNull Consumer<T> update) {
    return getById(id).map(entry -> {
      update.accept(entry);
      return entry;
    });
  }

  public final Optional<T> updateByIndex(@NotNull String index, @NotNull Object indexKey, @NotNull Consumer<T> update) {
    return getByIndex(index, indexKey).map(entry -> {
      update.accept(entry);
      return entry;
    });
  }

  public final Optional<T> updateSorted(@NotNull String sorted, int index, @NotNull Consumer<T> update) {
    return sortedDatabase.updateSorted(sorted, index, update);
  }

  public final Set<T> getEntries() {
    return Set.copyOf(entries);
  }

  public final Optional<T> getById(@NotNull Object rawId) {
    ByteTranslatable id = ByteTranslatable.from(rawId);
    return Optional.ofNullable(entryById.get(id));
  }

  public final Optional<T> getByIndex(@NotNull String index, @NotNull Object rawIndexKey) {
    ByteTranslatable indexKey = ByteTranslatable.from(rawIndexKey);
    return Optional.ofNullable(indexToEntry.get(index).get(indexKey));
  }

  public final Optional<T> getSorted(@NotNull String sorted, int index) {
    return sortedDatabase.getSorted(sorted, index);
  }

  protected abstract Map<ByteTranslatable, byte[]> getData();

  @SuppressWarnings("unchecked")
  private void loadData() {
    Map<ByteTranslatable, byte[]> data = getData();
    for (Map.Entry<ByteTranslatable, byte[]> rawEntry : data.entrySet())
      add((T) ReflectionUtils.deserialize(deserialize, rawEntry));
  }

  protected abstract void saveData(Map<ByteTranslatable, byte[]> data);

  private void save() {
    Map<ByteTranslatable, byte[]> data = new HashMap<>(entryById.size());
    if (useDirtyable)
      for (Map.Entry<ByteTranslatable, T> entry : entryById.entrySet()) {
        if (((Dirtyable) entry.getValue()).isDirty())
          data.put(entry.getKey(), ReflectionUtils.serialize(serialize, entry.getValue()));
        }
    else
      for (Map.Entry<ByteTranslatable, T> entry : entryById.entrySet())
        data.put(entry.getKey(), ReflectionUtils.serialize(serialize, entry.getValue()));
    saveData(data);
  }

  public void close() {
    sortedDatabase.close();
    autoSaveTask.cancel(false);
    save();
    entries.clear();
    entryById.clear();
    indexToEntry.clear();
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS))
        scheduler.shutdownNow();
    } catch (InterruptedException exception) {
      // TODO Figure log shit out
      Logger.getGlobal().log(Level.SEVERE, "Failed to shutdown scheduler", exception);
      scheduler.shutdownNow();
    }
  }

}
