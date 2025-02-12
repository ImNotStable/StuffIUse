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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public abstract class Database<T> extends AbstractDatabase<T, Collection<ByteTranslatable>> {

  private final Method serializeMethod;
  private final Method deserializeMethod;

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
    this.serializeMethod = ReflectionUtils.getSerializeMethod(entryClass);
    this.deserializeMethod = ReflectionUtils.getDeserializeMethod(entryClass);
  }

  @Override
  protected void loadData() {
    AtomicLong average = new AtomicLong();
    getData().parallelStream()
      .forEach(translatable -> {
        long start = System.nanoTime();
        T r = ReflectionUtils.deserialize(deserializeMethod, translatable);
        average.addAndGet(System.nanoTime() - start);
        add(r);
      });
    System.out.println("Average deserialize time: " + average.get() / entries.size());
  }

  @Override
  protected void save() {
    Stream<T> stream = entries.parallelStream();

    if (useDirtyable)
      stream = stream
        .filter(entry -> ((Dirtyable) entry).isDirty())
        .peek(entry -> ((Dirtyable) entry).markClean());

    AtomicLong average = new AtomicLong();
    Collection<ByteTranslatable> data = stream
      .collect(HashSet::new, (set, entry) -> {
        long start = System.nanoTime();
        ByteTranslatable r = ReflectionUtils.serialize(serializeMethod, entry);
        average.addAndGet(System.nanoTime() - start);
        set.add(r);
      }, HashSet::addAll);
    System.out.println("Average serialize time: " + average.get() / data.size());

    saveData(data);
  }

}
