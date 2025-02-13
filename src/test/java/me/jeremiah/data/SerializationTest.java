package me.jeremiah.data;

import me.jeremiah.data.storage.CompleteTestDatabaseObject;
import me.jeremiah.data.storage.TestDatabaseObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SerializationTest {

  @Test
  public void testNativeSerialization() {
    Collection<ByteTranslatable> serializedObjects = new HashSet<>(TestData.ENTRY_COUNT + 1, 1);

    for (CompleteTestDatabaseObject testObject : TestData.COMPLETE_TEST_OBJECTS) {
      ByteTranslatable byteTranslatable = ByteTranslatable.fromSerializable(testObject);
      serializedObjects.add(byteTranslatable);
    }

    for (ByteTranslatable byteTranslatable : serializedObjects) {
      CompleteTestDatabaseObject testObject = byteTranslatable.asSerializable();
      assert TestData.COMPLETE_TEST_OBJECTS.contains(testObject);
    }
  }

  @Test
  public void testSingleArrayNativeSerialization() throws IOException, ClassNotFoundException {
    byte[] bytes;

    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      for (CompleteTestDatabaseObject testObject : TestData.COMPLETE_TEST_OBJECTS)
        objectStream.writeObject(testObject);
      byteStream.flush();
      objectStream.flush();
      bytes = byteStream.toByteArray();
    }

    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
         ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
      for (int i = 0; i < TestData.ENTRY_COUNT; i++) {
        CompleteTestDatabaseObject testObject = (CompleteTestDatabaseObject) objectStream.readObject();
        assert TestData.COMPLETE_TEST_OBJECTS.contains(testObject);
      }
    }
  }

  @Test
  public void testSerialization() {
    HashMap<ByteTranslatable, ByteTranslatable> serializedObjects = new HashMap<>(TestData.ENTRY_COUNT + 1, 1);

    for (TestDatabaseObject testObject : TestData.TEST_OBJECTS) {
      Pair<ByteTranslatable, ByteTranslatable> entry = testObject.serialize();
      entry.putInto(serializedObjects);
    }

    for (Map.Entry<ByteTranslatable, ByteTranslatable> entry : serializedObjects.entrySet()) {
      TestDatabaseObject testObject = TestDatabaseObject.deserialize(Pair.of(entry));
      assert TestData.TEST_OBJECTS.contains(testObject);
    }
  }

  @Test
  public void testCompleteSerialization() {
    Collection<ByteTranslatable> serializedObjects = new HashSet<>(TestData.ENTRY_COUNT + 1, 1);

    for (CompleteTestDatabaseObject testObject : TestData.COMPLETE_TEST_OBJECTS) {
      ByteTranslatable value = testObject.serialize();
      serializedObjects.add(value);
    }

    for (ByteTranslatable entry : serializedObjects) {
      CompleteTestDatabaseObject testObject = CompleteTestDatabaseObject.deserialize(entry);
      assert TestData.COMPLETE_TEST_OBJECTS.contains(testObject);
    }
  }

}
