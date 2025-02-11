package me.jeremiah.data.storage.databases.components.indexing;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.components.AbstractDatabaseComponent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class IndexedDatabaseComponent<T> extends AbstractDatabaseComponent<T> {

  private ScheduledFuture<?> autoRefreshIndexesTask;

  private final List<Index> indexes;
  private Map<String, Map<ByteTranslatable, T>> indexToEntry;

  public IndexedDatabaseComponent(ScheduledExecutorService scheduler, Class<T> entryClass) {
    super(scheduler);
    this.indexes = ReflectionUtils.getIndexes(entryClass);
  }

  @Override
  public void setup(int initialCapacity) {
    indexToEntry = new ConcurrentHashMap<>(indexes.size() + 1, 1);
    for (Index index : indexes) indexToEntry.put(index.getId(), new ConcurrentHashMap<>(initialCapacity));
    autoRefreshIndexesTask = getScheduler().scheduleAtFixedRate(this::update, 5, 5, java.util.concurrent.TimeUnit.MINUTES);
  }

  @Override
  public void update() {
    indexes.parallelStream().filter(index -> !index.isFinal()).forEach(index -> {
      Map<ByteTranslatable, T> originalIndex = indexToEntry.get(index.getId());
      Map<ByteTranslatable, T> newIndex = originalIndex.values().stream()
        .collect(HashMap::new, (map, entry) -> map.put(ReflectionUtils.getIndex(index.getField(), entry), entry), HashMap::putAll);
      indexToEntry.put(index.getId(), newIndex);
    });
  }

  @Override
  public void add(@NotNull T entry) {
    for (Index index : indexes) {
      String indexName = index.getId();
      ByteTranslatable indexKey = ReflectionUtils.getIndex(index.getField(), entry);
      indexToEntry.get(indexName).put(indexKey, entry);
    }
  }

  public <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object rawKey, @NotNull Function<T, R> function) {
    return getByIndex(index, rawKey).map(function);
  }

  public Optional<T> updateByIndex(@NotNull String index, Object indexKey, @NotNull Consumer<T> update) {
    return getByIndex(index, indexKey).map(entry -> {
      update.accept(entry);
      return entry;
    });
  }

  public Optional<T> getByIndex(@NotNull String index, @NotNull Object rawKey) {
    ByteTranslatable indexKey = ByteTranslatable.from(rawKey);
    return Optional.ofNullable(indexToEntry.get(index).get(indexKey));
  }

  @Override
  public void close() {
    autoRefreshIndexesTask.cancel(true);
    indexes.clear();
    indexToEntry.clear();
  }

}
