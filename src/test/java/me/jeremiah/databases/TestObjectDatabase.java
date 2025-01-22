package me.jeremiah.databases;

import me.jeremiah.TestDatabaseObject;
import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.databases.objectoriented.Database;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestObjectDatabase extends Database<TestDatabaseObject> {

  private static final int entryCount = 100_000;
  private final Collection<ByteTranslatable> fakeSavedEntries = IntStream.range(0, entryCount)
    .mapToObj(TestDatabaseObject::new)
    .map(ByteTranslatable::fromSerializable)
    .collect(Collectors.toSet());

  public TestObjectDatabase() {
    super(new DatabaseInfo(null, 0, null, null, null), TestDatabaseObject.class);
    setup();
  }

  @Test
  @Order(1)
  public void initialStartup() throws InterruptedException {
    Thread.sleep(1000);
    for (int i = 0; i < 10; i++)
      assert getSorted("age", i).isPresent() : "Entry not found";
    close();
    Thread.sleep(1000);
  }

  @Test
  @Order(2)
  public void secondStartup() {
    setup();
    close();
  }

  @Override
  protected int lookupEntryCount() {
    return entryCount;
  }

  @Override
  protected Collection<ByteTranslatable> getData() {
    return fakeSavedEntries;
  }

  @Override
  protected void saveData(Collection<ByteTranslatable> data) {
    for (ByteTranslatable bytes : data)
      assert fakeSavedEntries.contains(bytes) : "Entry not found in fake saved entries";
  }

}
