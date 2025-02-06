package me.jeremiah.data.storage.databases.objectoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.databases.AbstractDatabase;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Database<T extends Serializable> extends AbstractDatabase<T, Collection<ByteTranslatable>> {

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
  }

  protected abstract Collection<ByteTranslatable> getData();

  protected void loadData() {
    getData().parallelStream().forEach(bytes -> add(bytes.asSerializable()));
  }

  protected abstract void saveData(Collection<ByteTranslatable> data);

  protected void save() {
    Stream<T> stream = entries.parallelStream();

    if (useDirtyable) {
      stream = stream
        .filter(entry -> ((Dirtyable) entry).isDirty())
        .peek(entry -> ((Dirtyable) entry).markClean());
    }

    Collection<ByteTranslatable> data = stream.map(ByteTranslatable::fromSerializable)
        .collect(Collectors.toUnmodifiableSet());

    saveData(data);
  }

}
