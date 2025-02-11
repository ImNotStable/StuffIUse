package me.jeremiah.data.storage.databases.byteoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.Pair;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.AbstractDatabase;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Database<T> extends AbstractDatabase<T, Map<ByteTranslatable, ByteTranslatable>> {

  private final MethodHandle serializeMethod;
  private final MethodHandle deserializeMethod;

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
    this.serializeMethod = ReflectionUtils.getSerializeMethod(entryClass);
    this.deserializeMethod = ReflectionUtils.getDeserializeMethod(entryClass);
  }

  @Override
  protected void loadData() {
    getData().entrySet().parallelStream()
      .map(Pair::of)
      .forEach(entry -> add(ReflectionUtils.deserialize(deserializeMethod, entry)));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void save() {
    Stream<T> stream = entries.parallelStream();

    if (useDirtyable)
      stream = stream
        .filter(entry -> ((Dirtyable) entry).isDirty())
        .peek(entry -> ((Dirtyable) entry).markClean());

    Map<ByteTranslatable, ByteTranslatable> data = stream
      .map(entry -> (Pair<ByteTranslatable, ByteTranslatable>) ReflectionUtils.serialize(serializeMethod, entry))
      .collect(HashMap::new, (map, entry) -> entry.putInto(map), HashMap::putAll);

    saveData(data);
  }

}
