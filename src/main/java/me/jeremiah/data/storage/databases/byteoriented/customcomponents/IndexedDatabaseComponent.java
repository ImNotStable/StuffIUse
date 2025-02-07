package me.jeremiah.data.storage.databases.byteoriented.customcomponents;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.AbstractDatabaseComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class IndexedDatabaseComponent<T> extends AbstractDatabaseComponent<T> {

  private ScheduledFuture<?> autoRefreshIndexesTask;

  private final Field idField;
  private Map<ByteTranslatable, T> entryById;

  private final Map<String, Field> indexes;
  private Map<String, Map<ByteTranslatable, T>> indexToEntry;

  public IndexedDatabaseComponent(ScheduledExecutorService scheduler, Class<T> entryClass) {
    super(scheduler);
    this.idField = ReflectionUtils.getIdField(entryClass);
    this.indexes = ReflectionUtils.getIndexes(entryClass);
  }

  public void setup(int initialCapacity) {
    entryById = new ConcurrentHashMap<>(initialCapacity);
    indexToEntry = new ConcurrentHashMap<>(indexes.size() + 1, 1);
    for (String index : indexes.keySet()) indexToEntry.put(index, new ConcurrentHashMap<>(initialCapacity));
    autoRefreshIndexesTask = getScheduler().scheduleAtFixedRate(this::update, 5, 5, java.util.concurrent.TimeUnit.MINUTES);
  }

  @Override
  public void update() {
    // ID is not required as it is assumed to be final
    indexes.entrySet().parallelStream().forEach(index -> {
      Map<ByteTranslatable, T> newIndex = new ConcurrentHashMap<>(entryById.size());
      for (T entry : entryById.values())
        newIndex.put(ReflectionUtils.getIndex(index.getValue(), entry), entry);
      indexToEntry.put(index.getKey(), newIndex);
    });
  }

  public Set<Map.Entry<ByteTranslatable, T>> getEntrySet() {
    return entryById.entrySet();
  }

  @Override
  public void add(@NotNull T entry) {
    ByteTranslatable id = ByteTranslatable.from(ReflectionUtils.getId(idField, entry));
    entryById.put(id, entry);
    for (Map.Entry<String, Field> index : indexes.entrySet()) {
      String indexName = index.getKey();
      ByteTranslatable indexKey = ReflectionUtils.getIndex(index.getValue(), entry);
      indexToEntry.get(indexName).put(indexKey, entry);
    }
  }

  public <R> Optional<R> queryById(@NotNull Object rawId, @NotNull Function<T, R> function) {
    return getById(rawId).map(function);
  }

  public <R> Optional<R> queryByIndex(@NotNull String index, @NotNull Object rawKey, @NotNull Function<T, R> function) {
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
    indexes.clear();
    indexToEntry.clear();
  }

}
