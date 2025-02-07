package me.jeremiah.data.storage.databases.byteoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.byteoriented.customcomponents.AbstractDatabase;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Database<T> extends AbstractDatabase<T, Map<ByteTranslatable, ByteTranslatable>> {

  private final Method serializeMethod;
  private final Method deserializeMethod;

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
    this.serializeMethod = ReflectionUtils.getSerializeMethod(entryClass);
    this.deserializeMethod = ReflectionUtils.getDeserializeMethod(entryClass);
  }

  protected abstract Map<ByteTranslatable, ByteTranslatable> getData();

  protected void loadData() {
    getData().entrySet().parallelStream()
      .forEach(entry -> add(ReflectionUtils.deserialize(deserializeMethod, entry)));
  }

  protected abstract void saveData(Map<ByteTranslatable, ByteTranslatable> data);

  protected void save() {
    Stream<Map.Entry<ByteTranslatable, T>> stream = indexedDatabaseComponent.getEntrySet().parallelStream();

    if (useDirtyable)
      stream = stream
        .filter(entry -> ((Dirtyable) entry.getValue()).isDirty())
        .peek(entry -> ((Dirtyable) entry.getValue()).markClean());

    Map<ByteTranslatable, ByteTranslatable> data = stream
      .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> ReflectionUtils.serialize(serializeMethod, entry.getValue())));

    saveData(data);
  }

}
