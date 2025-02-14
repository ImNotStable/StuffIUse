package me.jeremiah.data.storage.databases.completebyteoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.AbstractDatabase;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

public abstract class Database<ENTRY> extends AbstractDatabase<ENTRY, Collection<ByteTranslatable>> {

  private final Method serializeMethod;
  private final Method deserializeMethod;

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<ENTRY> entryClass) {
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
    Stream<ENTRY> stream = entries.parallelStream();

    if (useDirtyable)
      stream = stream
        .filter(entry -> ((Dirtyable) entry).isDirty())
        .peek(entry -> ((Dirtyable) entry).markClean());

    Collection<ByteTranslatable> data = stream
      .map(entry -> (ByteTranslatable) ReflectionUtils.serialize(serializeMethod, entry))
      .collect(HashSet::new, HashSet::add, HashSet::addAll);

    saveData(data);
  }

}
