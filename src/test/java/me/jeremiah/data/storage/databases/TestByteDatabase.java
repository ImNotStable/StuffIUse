package me.jeremiah.data.storage.databases;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.TestData;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.TestDatabaseObject;
import me.jeremiah.data.storage.databases.byteoriented.Database;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestByteDatabase extends Database<TestDatabaseObject> {

  private final Map<ByteTranslatable, ByteTranslatable> fakeSavedEntries = TestData.TEST_OBJECTS.stream()
    .map(TestDatabaseObject::serialize)
    .collect(
      HashMap::new,
      (map, entry) -> entry.putInto(map),
      HashMap::putAll
    );

  public TestByteDatabase() {
    super(new DatabaseInfo(null, 0, null, null, null), TestDatabaseObject.class);
  }

  @Test
  @Order(1)
  public void initialStartup() {
    setup();
    assert TestData.ENTRY_COUNT == entries.size() : "Fake saved entries size mismatch";
    int i = 0;
    for (TestDatabaseObject testObject : TestData.TEST_OBJECTS) {
      assert TestData.TEST_OBJECTS.contains(getByIndex("id", testObject.getId()).orElseThrow()) : "Failed to find entry by ID";
      assert TestData.TEST_OBJECTS.contains(getByIndex("name", testObject.getName()).orElseThrow()) : "Failed to find entry by name";
      assert TestData.TEST_OBJECTS.contains(getSorted("age", i++).orElseThrow()) : "Failed to find entry by age position";
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
  protected Map<ByteTranslatable, ByteTranslatable> getData() {
    return fakeSavedEntries;
  }

  @Override
  protected void saveData(Map<ByteTranslatable, ByteTranslatable> data) {
    assert data.size() == fakeSavedEntries.size() : "Data size mismatch";
    for (Map.Entry<ByteTranslatable, ByteTranslatable> entry : data.entrySet()) {
      assert fakeSavedEntries.containsKey(entry.getKey()) : "Entry key not found in fake saved entries";
      //assert fakeSavedEntries.containsValue(entry.getValue()) : "Entry value not found in fake saved entries";
    }
  }

}
