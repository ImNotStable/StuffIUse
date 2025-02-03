package me.jeremiah.data;

import me.jeremiah.data.storage.CompleteTestDatabaseObject;
import me.jeremiah.data.storage.TestDatabaseObject;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SerializationTest {

  private final int entryCount = 100_000;
  private final Set<TestDatabaseObject> testObjects = IntStream.range(0, entryCount).mapToObj(TestDatabaseObject::new).collect(Collectors.toSet());
  private final Set<CompleteTestDatabaseObject> completeTestObjects = IntStream.range(0, entryCount).mapToObj(CompleteTestDatabaseObject::new).collect(Collectors.toSet());

  @Test
  public void testNativeSerialization() {
    Collection<ByteTranslatable> serializedObjects = new HashSet<>(entryCount + 1, 1);

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
  public void testSerialization() {
    HashMap<ByteTranslatable, ByteTranslatable> serializedObjects = new HashMap<>(entryCount + 1, 1);

    for (TestDatabaseObject testObject : testObjects) {
      ByteTranslatable key = ByteTranslatable.from(testObject.getId());
      ByteTranslatable value = testObject.serialize();
      serializedObjects.put(key, value);
    }

    for (Map.Entry<ByteTranslatable, ByteTranslatable> entry : serializedObjects.entrySet()) {
      TestDatabaseObject testObject = TestDatabaseObject.deserialize(entry);
      assert testObjects.contains(testObject);
    }
  }

  @Test
  public void testCompleteSerialization() {
    Collection<ByteTranslatable> serializedObjects = new HashSet<>(entryCount + 1, 1);

    for (CompleteTestDatabaseObject testObject : completeTestObjects) {
      ByteTranslatable value = testObject.serialize();
      serializedObjects.add(value);
    }

    for (ByteTranslatable entry : serializedObjects) {
      CompleteTestDatabaseObject testObject = CompleteTestDatabaseObject.deserialize(entry.asByteArray());
      assert completeTestObjects.contains(testObject);
    }
  }

}
