package me.jeremiah.data.storage.databases.components.sorting;

import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.components.AbstractDatabaseComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SortedDatabaseComponent<T> extends AbstractDatabaseComponent<T> {

  private ScheduledFuture<?> autoSortTask;

  private final Map<String, Field> sortedFields;
  private Map<String, List<T>> sortedEntries;

  private boolean operating = false;

  public SortedDatabaseComponent(ScheduledExecutorService scheduler, Class<T> entryClass) {
    super(scheduler);
    this.sortedFields = ReflectionUtils.getSortedFields(entryClass);
    if (!sortedFields.isEmpty())
      operating = true;
  }

  public void setup(int doubledEntryCount) {
    if (!operating)
      return;
    sortedEntries = new ConcurrentHashMap<>(sortedFields.size() + 1, 1);
    for (String field : sortedFields.keySet())
      sortedEntries.put(field, Collections.synchronizedList(new ArrayList<>(doubledEntryCount)));
    autoSortTask = getScheduler().scheduleAtFixedRate(this::update, 5, 5, TimeUnit.MINUTES);
  }

  @Override
  public void update() {
    if (!operating)
      return;
    sortedEntries.entrySet().parallelStream().forEach(sortedEntries -> {
        Field field = sortedFields.get(sortedEntries.getKey());
        sortedEntries.getValue().sort((entry1, entry2) ->
          ReflectionUtils.compareSortedFields(field, entry1, entry2)
        );
      }
    );
  }

  @Override
  public void add(@NotNull T entry) {
    if (!operating)
      return;
    sortedFields.keySet().forEach(sortedKey -> sortedEntries.get(sortedKey).add(entry));
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
