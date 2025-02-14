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

public final class IndexedDatabaseComponent<ENTRY> extends AbstractDatabaseComponent<ENTRY> {

  private ScheduledFuture<?> autoRefreshIndexesTask;

  private final List<Index> indexes;
  private Map<String, Map<ByteTranslatable, ENTRY>> indexToEntry;

  public IndexedDatabaseComponent(ScheduledExecutorService scheduler, Class<ENTRY> entryClass) {
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
      Map<ByteTranslatable, ENTRY> originalIndex = indexToEntry.get(index.getId());
      Map<ByteTranslatable, ENTRY> newIndex = originalIndex.values().stream()
        .collect(HashMap::new, (map, entry) -> map.put(ReflectionUtils.getIndex(index.getField(), entry), entry), HashMap::putAll);
      indexToEntry.put(index.getId(), newIndex);
    });
  }

  @Override
  public void add(@NotNull ENTRY entry) {
    for (Index index : indexes) {
      String indexName = index.getId();
      ByteTranslatable indexKey = ReflectionUtils.getIndex(index.getField(), entry);
      indexToEntry.get(indexName).put(indexKey, entry);
    }
  }

  public <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object rawKey, @NotNull Function<ENTRY, R> function) {
    return getByIndex(index, rawKey).map(function);
  }

  public Optional<ENTRY> updateByIndex(@NotNull String index, Object indexKey, @NotNull Consumer<ENTRY> update) {
    return getByIndex(index, indexKey).map(entry -> {
      update.accept(entry);
      return entry;
    });
  }

  public Optional<ENTRY> getByIndex(@NotNull String index, @NotNull Object rawKey) {
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
