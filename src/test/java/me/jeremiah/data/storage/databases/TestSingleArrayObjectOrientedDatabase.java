package me.jeremiah.data.storage.databases;

import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.TestDatabaseObject;
import me.jeremiah.data.storage.databases.singlearrayobjectoriented.Database;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestSingleArrayObjectOrientedDatabase extends Database<TestDatabaseObject> {

  private static final int entryCount = 10_000;
  private final Collection<TestDatabaseObject> testObjects = IntStream.range(0, entryCount)
    .mapToObj(TestDatabaseObject::new)
    .collect(Collectors.toSet());
  private final byte[] fakeSavedEntries;

  {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      for (TestDatabaseObject testObject : testObjects)
        objectStream.writeObject(testObject);
      objectStream.flush();
      fakeSavedEntries = byteStream.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public TestSingleArrayObjectOrientedDatabase() {
    super(new DatabaseInfo(null, 0, null, null, null), TestDatabaseObject.class);
  }

  @Test
  @Order(1)
  public void initialStartup() {
    setup();
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
  protected byte[] getData() {
    return fakeSavedEntries;
  }

  @Override
  protected void saveData(byte[] data) {
    assert data.length == fakeSavedEntries.length : "Data size mismatch";

    Collection<TestDatabaseObject> realObjects = new HashSet<>(entryCount + 1, 1);

    try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data));
         ObjectInputStream inputStream = new ObjectInputStream(bis)) {
      for (int i = 0; i < entryCount; i++)
        realObjects.add((TestDatabaseObject) inputStream.readObject());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    assert testObjects.equals(realObjects) : "Data mismatch";
  }

}
