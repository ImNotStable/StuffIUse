package me.jeremiah.data.storage.databases.objectoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.databases.IndexedDatabaseComponent;
import me.jeremiah.data.storage.databases.SortedDatabaseComponent;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
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

public abstract class Database<T extends Serializable> implements Closeable {

  private final ScheduledExecutorService scheduler;

  private final DatabaseInfo info;
  private final boolean useDirtyable;
  private ScheduledFuture<?> autoSaveTask;

  protected Set<T> entries;

  protected final IndexedDatabaseComponent<T> indexedDatabaseComponent;
  protected final SortedDatabaseComponent<T> sortedDatabaseComponent;

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.info = info;
    this.useDirtyable = Dirtyable.class.isAssignableFrom(entryClass);
    this.indexedDatabaseComponent = new IndexedDatabaseComponent<>(scheduler, entryClass);
    this.sortedDatabaseComponent = new SortedDatabaseComponent<>(scheduler, entryClass);
  }

  protected abstract int lookupEntryCount();

  protected void setup() {
    int initialCapacity = lookupEntryCount() * 2;
    entries = ConcurrentHashMap.newKeySet(initialCapacity);
    indexedDatabaseComponent.setup(initialCapacity);
    sortedDatabaseComponent.setup(initialCapacity);
    loadData();
    sortedDatabaseComponent.sort();
    autoSaveTask = scheduler.scheduleAtFixedRate(this::save, info.getAutoSaveInterval(), info.getAutoSaveInterval(), info.getAutoSaveTimeUnit());
  }

  public final void add(T entry) {
    entries.add(entry);
    indexedDatabaseComponent.add(entry);
    sortedDatabaseComponent.add(entry);
  }

  public final <R> Optional<R> queryById(@NotNull Object rawId, @NotNull Function<T, R> function) {
    return indexedDatabaseComponent.queryById(rawId, function);
  }

  public final <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object rawKey, @NotNull Function<T, R> function) {
    return indexedDatabaseComponent.queryByIndex(index, rawKey, function);
  }

  public final <R> Optional<R> querySorted(@NotNull String sorted, int index, @NotNull Function<T, R> function) {
    return sortedDatabaseComponent.querySorted(sorted, index, function);
  }

  public final Optional<T> updateById(@NotNull Object rawId, @NotNull Consumer<T> update) {
    return indexedDatabaseComponent.updateById(rawId, update);
  }

  public final Optional<T> updateByIndex(@NotNull String index, @NotNull Object rawKey, @NotNull Consumer<T> update) {
    return indexedDatabaseComponent.updateByIndex(index, rawKey, update);
  }

  public final Optional<T> updateSorted(@NotNull String sorted, int index, @NotNull Consumer<T> update) {
    return sortedDatabaseComponent.updateSorted(sorted, index, update);
  }

  public final Set<T> getEntries() {
    return Set.copyOf(entries);
  }

  public final Optional<T> getById(@NotNull Object rawId) {
    return indexedDatabaseComponent.getById(rawId);
  }

  public final Optional<T> getByIndex(@NotNull String index, @NotNull Object rawKey) {
    return indexedDatabaseComponent.getByIndex(index, rawKey);
  }

  public final Optional<T> getSorted(@NotNull String sorted, int index) {
    return sortedDatabaseComponent.getSorted(sorted, index);
  }

  protected abstract Collection<ByteTranslatable> getData();

  private void loadData() {
    Collection<ByteTranslatable> data = getData();
    for (ByteTranslatable bytes : data)
      add(bytes.asSerializable());
  }

  protected abstract void saveData(Collection<ByteTranslatable> data);

  private void save() {
    Collection<ByteTranslatable> data = new HashSet<>(entries.size());
    if (useDirtyable)
      for (T entry : entries) {
        if (((Dirtyable) entry).isDirty())
          data.add(ByteTranslatable.fromSerializable(entry));
      }
    else
      for (T entry : entries)
        data.add(ByteTranslatable.fromSerializable(entry));
    saveData(data);
  }

  public void close() {
    sortedDatabaseComponent.close();
    indexedDatabaseComponent.close();
    autoSaveTask.cancel(false);
    save();
    entries.clear();
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
