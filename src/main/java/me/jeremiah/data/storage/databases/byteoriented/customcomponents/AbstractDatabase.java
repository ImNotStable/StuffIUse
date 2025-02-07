package me.jeremiah.data.storage.databases.byteoriented.customcomponents;

import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.databases.SortedDatabaseComponent;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDatabase<T, D> implements Closeable {

  private final ScheduledExecutorService scheduler;

  private final DatabaseInfo info;
  protected final boolean useDirtyable;
  private ScheduledFuture<?> autoSaveTask;

  protected Set<T> entries;

  protected final IndexedDatabaseComponent<T> indexedDatabaseComponent;
  protected final SortedDatabaseComponent<T> sortedDatabaseComponent;

  protected AbstractDatabase(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
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
    sortedDatabaseComponent.update();
    autoSaveTask = scheduler.scheduleAtFixedRate(this::save, info.getAutoSaveInterval(), info.getAutoSaveInterval(), info.getAutoSaveTimeUnit());
  }

  public final void add(T entry) {
    entries.add(entry);
    indexedDatabaseComponent.add(entry);
    sortedDatabaseComponent.add(entry);
  }

  public final <R> Optional<R> queryById(@NotNull Object id, @NotNull Function<T, R> function) {
    return indexedDatabaseComponent.queryById(id, function);
  }

  public final <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object key, @NotNull Function<T, R> function) {
    return indexedDatabaseComponent.queryByIndex(index, key, function);
  }

  public final <R> Optional<R> querySorted(@NotNull String sorted, int index, @NotNull Function<T, R> function) {
    return sortedDatabaseComponent.querySorted(sorted, index, function);
  }

  public final Optional<T> updateById(@NotNull Object id, @NotNull Consumer<T> update) {
    return indexedDatabaseComponent.updateById(id, update);
  }

  public final Optional<T> updateByIndex(@NotNull String index, @NotNull Object indexKey, @NotNull Consumer<T> update) {
    return indexedDatabaseComponent.updateByIndex(index, indexKey, update);
  }

  public final Optional<T> updateSorted(@NotNull String sorted, int index, @NotNull Consumer<T> update) {
    return sortedDatabaseComponent.updateSorted(sorted, index, update);
  }

  public final Set<T> getEntries() {
    return entries;
  }

  public final Optional<T> getById(@NotNull Object rawId) {
    return indexedDatabaseComponent.getById(rawId);
  }

  public final Optional<T> getByIndex(@NotNull String index, @NotNull Object rawIndexKey) {
    return indexedDatabaseComponent.getByIndex(index, rawIndexKey);
  }

  public final Optional<T> getSorted(@NotNull String sorted, int index) {
    return sortedDatabaseComponent.getSorted(sorted, index);
  }

  protected abstract D getData();

  protected abstract void loadData();

  protected abstract void saveData(D data);

  protected abstract void save();

  public void close() {
    sortedDatabaseComponent.close();
    autoSaveTask.cancel(false);
    save();
    entries.clear();
    indexedDatabaseComponent.close();
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
        scheduler.shutdownNow();
    } catch (InterruptedException exception) {
      // TODO Figure log shit out
      Logger.getGlobal().log(Level.SEVERE, "Failed to shutdown scheduler", exception);
      scheduler.shutdownNow();
    }
  }

}
