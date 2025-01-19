package me.jeremiah.databases;

import me.jeremiah.TestDatabaseObject;
import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.DatabaseInfo;
import me.jeremiah.data.storage.databases.Database;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class TestDatabase extends Database<TestDatabaseObject> {

  private static final int entryCount = 100_000;
  private final Map<ByteTranslatable, byte[]> fakeSavedEntries = new HashMap<>(entryCount + 1 , 1);

  public TestDatabase() {
    super(new DatabaseInfo(null, 0, null, null, null), TestDatabaseObject.class);
    IntStream.range(0, entryCount).forEach(i -> {
      TestDatabaseObject object = new TestDatabaseObject();
      fakeSavedEntries.put(ByteTranslatable.from(object.getId()), object.serialize());
    });
    setup();
  }

  @Test
  @Order(1)
  public void initialStartup() throws InterruptedException {
    Thread.sleep(1000);
    for (int i = 0; i < 10; i++) {
      Optional<TestDatabaseObject> optional = getSorted("age", i);
      assert optional.isPresent() : "Entry not found";
      TestDatabaseObject entry = optional.get();
      System.out.println("UUID: " + entry.getId() + ", Name: " + entry.getName() + ", Age: " + entry.getAge() + ", Cool: " + entry.isCool());
    }
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
  protected Map<ByteTranslatable, byte[]> getData() {
    return fakeSavedEntries;
  }

  @Override
  protected void saveData(Map<ByteTranslatable, byte[]> data) {
    for (Map.Entry<ByteTranslatable, byte[]> entry : data.entrySet())
      assert fakeSavedEntries.containsKey(entry.getKey()) : "Entry not found in fake saved entries";
  }

}
