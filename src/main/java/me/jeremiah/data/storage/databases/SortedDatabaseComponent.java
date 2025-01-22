package me.jeremiah.data.storage.databases;

import me.jeremiah.data.storage.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SortedDatabaseComponent<T> implements Closeable {

  private final ScheduledExecutorService scheduler;
  private ScheduledFuture<?> autoSortTask;

  private final Map<String, Field> sortedFields;
  private Map<String, List<T>> sortedEntries;

  private boolean operating = false;

  public SortedDatabaseComponent(ScheduledExecutorService scheduler, Class<T> entryClass) {
    this.scheduler = scheduler;
    this.sortedFields = ReflectionUtils.getSortedFields(entryClass);
    if (!sortedFields.isEmpty())
      operating = true;
  }

  public void setup(int doubledEntryCount) {
    if (!operating)
      return;
    sortedEntries = new ConcurrentHashMap<>(sortedFields.size() + 1, 1);
    for (String field : sortedFields.keySet())
      sortedEntries.put(field, new ArrayList<>(doubledEntryCount));
    autoSortTask = scheduler.scheduleAtFixedRate(this::sort, 5, 5, TimeUnit.MINUTES);
  }

  public void sort() {
    if (!operating)
      return;
    for (Map.Entry<String, List<T>> sortedEntries : sortedEntries.entrySet()) {
      sortedEntries.getValue().sort((entry1, entry2) ->
        ReflectionUtils.compareSortedFields(sortedFields.get(sortedEntries.getKey()), entry1, entry2)
      );
    }
  }

  public void add(@NotNull T entry) {
    if (!operating)
      return;
    for (Map.Entry<String, Field> sortedField : sortedFields.entrySet())
      sortedEntries.get(sortedField.getKey()).add(entry);
  }

  public <R> Optional<R> querySorted(@NotNull String sorted, int index, @NotNull Function<T, R> function) {
    if (!operating)
      return Optional.empty();
    return Optional.ofNullable(sortedEntries.get(sorted).get(index)).map(function);
  }

  public Optional<T> updateSorted(@NotNull String sorted, int index, @NotNull Consumer<T> update) {
    if (!operating)
      return Optional.empty();
    return Optional.ofNullable(sortedEntries.get(sorted).get(index)).map(entry -> {
      update.accept(entry);
      return entry;
    });
  }

  public Optional<T> getSorted(@NotNull String sorted, int index) {
    if (!operating)
      return Optional.empty();
    return Optional.ofNullable(sortedEntries.get(sorted).get(index));
  }

  @Override
  public void close() {
    if (!operating)
      return;
    operating = false;
    autoSortTask.cancel(true);
    sortedEntries.clear();
    sortedFields.clear();
  }

}
