package me.jeremiah.data.storage.databases.byteoriented;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.Pair;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.Dirtyable;
import me.jeremiah.data.storage.ReflectionUtils;
import me.jeremiah.data.storage.databases.AbstractDatabase;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public abstract class Database<T> extends AbstractDatabase<T, Map<ByteTranslatable, ByteTranslatable>> {

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
    getData().entrySet().parallelStream()
      .map(Pair::of)
      .forEach(entry -> {
        long start = System.nanoTime();
        T r = ReflectionUtils.deserialize(deserializeMethod, entry);
        average.set((average.get() + (System.nanoTime() - start)) / 2);
        add(r);
      });
    System.out.println("Average deserialize time: " + average.get());
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void save() {
    Stream<T> stream = entries.parallelStream();

    if (useDirtyable)
      stream = stream
        .filter(entry -> ((Dirtyable) entry).isDirty())
        .peek(entry -> ((Dirtyable) entry).markClean());

    AtomicLong average = new AtomicLong();
    Map<ByteTranslatable, ByteTranslatable> data = stream
      .map(entry -> {
        long start = System.nanoTime();
        Pair<ByteTranslatable, ByteTranslatable> r = ReflectionUtils.serialize(serializeMethod, entry);
        average.set((average.get() + (System.nanoTime() - start)) / 2);
        return r;
      })
      .collect(HashMap::new, (map, entry) -> entry.putInto(map), HashMap::putAll);
    System.out.println("Average serialize time: " + average.get());

    saveData(data);
  }

}
