package me.jeremiah.data.storage.databases.completebyteoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.AbstractDatabase;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

public abstract class Database<T> extends AbstractDatabase<T, Collection<ByteTranslatable>> {

  private final MethodHandle serializeMethod;
  private final MethodHandle deserializeMethod;

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
    this.serializeMethod = ReflectionUtils.getSerializeMethod(entryClass);
    this.deserializeMethod = ReflectionUtils.getDeserializeMethod(entryClass);
  }

  @Override
  protected void loadData() {
    getData().parallelStream()
      .forEach(translatable -> add(ReflectionUtils.deserialize(deserializeMethod, translatable)));
  }

  @Override
  protected void save() {
    Stream<T> stream = entries.parallelStream();

    if (useDirtyable)
      stream = stream
        .filter(entry -> ((Dirtyable) entry).isDirty())
        .peek(entry -> ((Dirtyable) entry).markClean());

    Collection<ByteTranslatable> data = stream
      .collect(HashSet::new, (set, entry) -> set.add(ReflectionUtils.serialize(serializeMethod, entry)), HashSet::addAll);

    saveData(data);
  }

}
