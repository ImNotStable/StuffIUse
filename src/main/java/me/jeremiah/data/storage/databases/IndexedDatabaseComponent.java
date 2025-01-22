package me.jeremiah.data.storage.databases;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class IndexedDatabaseComponent<T> implements Closeable {

  private final ScheduledExecutorService scheduler;
  private ScheduledFuture<?> autoRefreshIndexesTask;

  protected final Field idField;
  protected Map<ByteTranslatable, T> entryById;

  protected final Map<String, Field> indexes;
  protected Map<String, Map<ByteTranslatable, T>> indexToEntry;

  public IndexedDatabaseComponent(ScheduledExecutorService scheduler, Class<T> entryClass) {
    this.scheduler = scheduler;
    this.idField = ReflectionUtils.getIdField(entryClass);
    this.indexes = ReflectionUtils.getIndexes(entryClass);
  }

  public void setup(int initialCapacity) {
    entryById = new ConcurrentHashMap<>(initialCapacity);
    indexToEntry = new ConcurrentHashMap<>(indexes.size() + 1, 1);
    for (String index : indexes.keySet()) indexToEntry.put(index, new ConcurrentHashMap<>(initialCapacity));
    autoRefreshIndexesTask = scheduler.scheduleAtFixedRate(this::refreshIndexes, 5, 5, java.util.concurrent.TimeUnit.MINUTES);
  }

  public void refreshIndexes() {
    // ID is not required as it is assumed to be final
    for (Map.Entry<String, Field> index : indexes.entrySet()) {
      Map<ByteTranslatable, T> newIndex = new ConcurrentHashMap<>(entryById.size());
      for (T entry : entryById.values()) {
        newIndex.put(ReflectionUtils.getIndex(index.getValue(), entry), entry);
      }
      indexToEntry.put(index.getKey(), newIndex);
    }
  }

  public Set<Map.Entry<ByteTranslatable, T>> getEntrySet() {
    return entryById.entrySet();
  }

  public void add(@NotNull T entry) {
    entryById.put(ReflectionUtils.getId(idField, entry), entry);
    for (Map.Entry<String, Field> index : indexes.entrySet()) {
      indexToEntry.get(index.getKey()).put(
        ReflectionUtils.getIndex(index.getValue(), entry),
        entry
      );
    }
  }

  public final <R> Optional<R> queryById(@NotNull Object rawId, @NotNull Function<T, R> function) {
    return getById(rawId).map(function);
  }

  public final <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object rawKey, @NotNull Function<T, R> function) {
    return getByIndex(index, rawKey).map(function);
  }

  public Optional<T> updateById(@NotNull Object rawKey, @NotNull Consumer<T> update) {
    return Optional.ofNullable(entryById.get(ByteTranslatable.from(rawKey))).map(entry -> {
      update.accept(entry);
      return entry;
    });
  }

  public Optional<T> updateByIndex(@NotNull String index, Object indexKey, @NotNull Consumer<T> update) {
    return getByIndex(index, indexKey).map(entry -> {
      update.accept(entry);
      return entry;
    });
  }

  public Optional<T> getById(@NotNull Object rawId) {
    ByteTranslatable id = ByteTranslatable.from(rawId);
    return Optional.ofNullable(entryById.get(id));
  }

  public Optional<T> getByIndex(@NotNull String index, @NotNull Object rawKey) {
    ByteTranslatable indexKey = ByteTranslatable.from(rawKey);
    return Optional.ofNullable(indexToEntry.get(index).get(indexKey));
  }

  @Override
  public void close() {
    autoRefreshIndexesTask.cancel(true);
    entryById.clear();
    indexToEntry.clear();
    indexes.clear();
  }

}
