package me.jeremiah.data.storage.databases.singlearrayobjectoriented;

import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.databases.IndexedDatabaseComponent;
import me.jeremiah.data.storage.databases.SortedDatabaseComponent;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
    return entries;
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

  protected abstract byte[] getData();

  @SuppressWarnings("unchecked")
  private void loadData() {
    byte[] data = getData();
    if (data == null || data.length == 0) return;

    try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
      while (true) {
        T entry = (T) inputStream.readObject();
        add(entry);
      }
    } catch (EOFException ignored) {
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract void saveData(byte[] data);

  private void save() {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      for (T entry : entries)
        objectStream.writeObject(entry);
      objectStream.flush();
      saveData(byteStream.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    sortedDatabaseComponent.close();
    indexedDatabaseComponent.close();
    autoSaveTask.cancel(false);
    save();
    entries.clear();
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
