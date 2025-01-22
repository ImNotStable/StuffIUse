package me.jeremiah.databases;

import me.jeremiah.TestDatabaseObject;
import me.jeremiah.data.ByteTranslatable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SerializationTest {

  private final int entryCount = 100_000;
  private final Set<TestDatabaseObject> testObjects = IntStream.range(0, entryCount).mapToObj(TestDatabaseObject::new).collect(Collectors.toSet());

  @Test
  public void testNativeSerialization() throws IOException, ClassNotFoundException {
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
      for (int i = 0; i < entryCount; i++) {
        TestDatabaseObject testObject = (TestDatabaseObject) objectStream.readObject();
        assert testObjects.contains(testObject);
      }
    }
  }

  @Test
  public void testSerialization() {
    HashMap<ByteTranslatable, byte[]> serializedObjects = new HashMap<>(entryCount + 1, 1);

    for (TestDatabaseObject testObject : testObjects) {
      ByteTranslatable key = ByteTranslatable.from(testObject.getId());
      byte[] value = testObject.serialize();
      serializedObjects.put(key, value);
    }

    for (Map.Entry<ByteTranslatable, byte[]> entry : serializedObjects.entrySet()) {
      TestDatabaseObject testObject = TestDatabaseObject.deserialize(entry);
      assert testObjects.contains(testObject);
    }
  }

}
