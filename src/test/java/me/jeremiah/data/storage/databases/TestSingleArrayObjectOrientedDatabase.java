package me.jeremiah.data.storage.databases;

import me.jeremiah.data.TestData;
import me.jeremiah.data.storage.CompleteTestDatabaseObject;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.databases.singlearrayobjectoriented.Database;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;

public class TestSingleArrayObjectOrientedDatabase extends Database<CompleteTestDatabaseObject> {

  private final byte[] fakeSavedEntries;

  {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      for (CompleteTestDatabaseObject testObject : TestData.COMPLETE_TEST_OBJECTS)
        objectStream.writeObject(testObject);
      objectStream.flush();
      fakeSavedEntries = byteStream.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public TestSingleArrayObjectOrientedDatabase() {
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
  protected byte[] getData() {
    return fakeSavedEntries;
  }

  @Override
  protected void saveData(byte[] data) {
    assert data.length == fakeSavedEntries.length : "Data size mismatch";

    Collection<CompleteTestDatabaseObject> realObjects = new HashSet<>(TestData.ENTRY_COUNT + 1, 1);

    try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
      for (int i = 0; i < TestData.ENTRY_COUNT; i++)
        realObjects.add((CompleteTestDatabaseObject) inputStream.readObject());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    assert TestData.COMPLETE_TEST_OBJECTS.equals(realObjects) : "Data mismatch";
  }

}
