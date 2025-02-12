package me.jeremiah.data.storage.databases;

import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.databases.components.indexing.IndexedDatabaseComponent;
import me.jeremiah.data.storage.databases.components.sorting.SortedDatabaseComponent;
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

  private final IndexedDatabaseComponent<T> indexedDatabaseComponent;
  private final SortedDatabaseComponent<T> sortedDatabaseComponent;

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
    long start = System.currentTimeMillis();
    entries = ConcurrentHashMap.newKeySet(initialCapacity);
    System.out.println("Time 1: " + (System.currentTimeMillis() - start));
    start = System.currentTimeMillis();
    indexedDatabaseComponent.setup(initialCapacity);
    System.out.println("Time 2: " + (System.currentTimeMillis() - start));
    start = System.currentTimeMillis();
    sortedDatabaseComponent.setup(initialCapacity);
    System.out.println("Time 3: " + (System.currentTimeMillis() - start));
    start = System.currentTimeMillis();
    loadData();
    System.out.println("Time 4: " + (System.currentTimeMillis() - start));
    System.out.println("Average add() time: " + averageTime / entries.size());
    start = System.currentTimeMillis();
    sortedDatabaseComponent.update();
    System.out.println("Time 5: " + (System.currentTimeMillis() - start));
    start = System.currentTimeMillis();
    autoSaveTask = scheduler.scheduleAtFixedRate(this::save, info.getAutoSaveInterval(), info.getAutoSaveInterval(), info.getAutoSaveTimeUnit());
    System.out.println("Time 6: " + (System.currentTimeMillis() - start));
  }

  private long averageTime = 0;

  public final void add(T entry) {
    long start = System.nanoTime();
    entries.add(entry);
    indexedDatabaseComponent.add(entry);
    sortedDatabaseComponent.add(entry);
    averageTime += System.nanoTime() - start;
  }

  public final <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object key, @NotNull Function<T, R> function) {
    return indexedDatabaseComponent.queryByIndex(index, key, function);
  }

  public final <R> Optional<R> querySorted(@NotNull String sorted, int index, @NotNull Function<T, R> function) {
    return sortedDatabaseComponent.querySorted(sorted, index, function);
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

  @Override
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
