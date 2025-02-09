package me.jeremiah.data.storage.databases;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.TestData;
import me.jeremiah.data.storage.CompleteTestDatabaseObject;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.databases.completebyteoriented.Database;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

public class TestCompleteByteDatabase extends Database<CompleteTestDatabaseObject> {

  private final Collection<ByteTranslatable> fakeSavedEntries = TestData.COMPLETE_TEST_OBJECTS.stream()
    .map(CompleteTestDatabaseObject::serialize)
    .collect(Collectors.toUnmodifiableSet());

  public TestCompleteByteDatabase() {
    super(new DatabaseInfo(null, 0, null, null, null), CompleteTestDatabaseObject.class);
  }

  @Test
  @Order(1)
  public void initialStartup() {
    setup();
    assert TestData.ENTRY_COUNT == getEntries().size() : "Fake saved entries size mismatch";
    int i = 0;
    for (CompleteTestDatabaseObject testObject : TestData.COMPLETE_TEST_OBJECTS) {
      assert getByIndex("id", testObject.getId()).isPresent() : "Failed to find entry by ID";
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
    return TestData.ENTRY_COUNT;
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
