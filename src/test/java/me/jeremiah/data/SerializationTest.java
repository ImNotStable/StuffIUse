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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SerializationTest {

  private final Set<TestDatabaseObject> testObjects = IntStream.range(0, TestData.ENTRY_COUNT).mapToObj(TestDatabaseObject::new).collect(Collectors.toSet());
  private final Set<CompleteTestDatabaseObject> completeTestObjects = IntStream.range(0, TestData.ENTRY_COUNT).mapToObj(CompleteTestDatabaseObject::new).collect(Collectors.toSet());

  @Test
  public void testNativeSerialization() {
    Collection<ByteTranslatable> serializedObjects = new HashSet<>(TestData.ENTRY_COUNT + 1, 1);

    for (TestDatabaseObject testObject : testObjects) {
      ByteTranslatable byteTranslatable = ByteTranslatable.fromSerializable(testObject);
      serializedObjects.add(byteTranslatable);
    }

    for (ByteTranslatable byteTranslatable : serializedObjects) {
      TestDatabaseObject testObject = byteTranslatable.asSerializable();
      assert testObjects.contains(testObject);
    }
  }

  @Test
  public void testSingleArrayNativeSerialization() throws IOException, ClassNotFoundException {
    byte[] bytes;

    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      for (TestDatabaseObject testObject : testObjects)
        objectStream.writeObject(testObject);
      byteStream.flush();
      objectStream.flush();
      bytes = byteStream.toByteArray();
    }

    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
         ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
      for (int i = 0; i < TestData.ENTRY_COUNT; i++) {
        TestDatabaseObject testObject = (TestDatabaseObject) objectStream.readObject();
        assert testObjects.contains(testObject);
      }
    }
  }

  @Test
  public void testSerialization() {
    HashMap<ByteTranslatable, ByteTranslatable> serializedObjects = new HashMap<>(TestData.ENTRY_COUNT + 1, 1);

    for (TestDatabaseObject testObject : testObjects) {
      Pair<ByteTranslatable, ByteTranslatable> entry = testObject.serialize();
      entry.putInto(serializedObjects);
    }

    for (Map.Entry<ByteTranslatable, ByteTranslatable> entry : serializedObjects.entrySet()) {
      TestDatabaseObject testObject = TestDatabaseObject.deserialize(Pair.of(entry));
      assert testObjects.contains(testObject);
    }
  }

  @Test
  public void testCompleteSerialization() {
    Collection<ByteTranslatable> serializedObjects = new HashSet<>(TestData.ENTRY_COUNT + 1, 1);

    for (CompleteTestDatabaseObject testObject : completeTestObjects) {
      ByteTranslatable value = testObject.serialize();
      serializedObjects.add(value);
    }

    for (ByteTranslatable entry : serializedObjects) {
      CompleteTestDatabaseObject testObject = CompleteTestDatabaseObject.deserialize(entry);
      assert completeTestObjects.contains(testObject);
    }
  }

}
