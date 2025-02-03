package me.jeremiah.data.storage.databases;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.TestDatabaseObject;
import me.jeremiah.data.storage.databases.objectoriented.Database;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestObjectDatabase extends Database<TestDatabaseObject> {

  private static final int entryCount = 10_000;
  private final Collection<TestDatabaseObject> testObjects = IntStream.range(0, entryCount)
    .mapToObj(TestDatabaseObject::new)
    .collect(Collectors.toSet());
  private final Collection<ByteTranslatable> fakeSavedEntries = testObjects.stream()
    .map(ByteTranslatable::fromSerializable)
    .collect(Collectors.toUnmodifiableSet());

  public TestObjectDatabase() {
    super(new DatabaseInfo(null, 0, null, null, null), TestDatabaseObject.class);
    setup();
  }

  @Test
  @Order(1)
  public void initialStartup() {
    assert entryCount == getEntries().size() : "Fake saved entries size mismatch";
    int i = 0;
    for (TestDatabaseObject testObject : testObjects) {
      assert getById(testObject.getId()).isPresent() : "Failed to find entry by ID";
      assert getByIndex("name", testObject.getName()).isPresent() : "Failed to find entry by name";
      assert getSorted("age", i++).isPresent() : "Failed to find entry by age position";
    }
    close();
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
    assert data.size() == fakeSavedEntries.size() : "Data size mismatch";
    assert fakeSavedEntries.containsAll(data) : "Entry not found in fake saved entries";
  }

}
