package me.jeremiah;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.Deserializer;
import me.jeremiah.data.storage.ID;
import me.jeremiah.data.storage.Indexable;
import me.jeremiah.data.storage.Serializer;
import me.jeremiah.data.storage.Sorted;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDatabaseObject implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

  @Deserializer
  public static TestDatabaseObject deserialize(Map.Entry<ByteTranslatable, byte[]> entry) {
    ByteBuffer buffer = ByteBuffer.wrap(entry.getValue());
    UUID id = entry.getKey().asUUID();


    byte[] nameBytes = new byte[buffer.getInt()];
    buffer.get(nameBytes);
    String name = new String(nameBytes);
    int age = buffer.getInt();
    boolean isCool = buffer.get() == 1;

    return new TestDatabaseObject(id, name, age, isCool);
  }

  @ID
  private final UUID id;

  @Indexable("name")
  private final String name;
  @Sorted("age")
  private final int age;
  private final boolean isCool;

  public TestDatabaseObject(int i) {
    this(new UUID(RANDOM.nextLong(), RANDOM.nextLong()), "Test_Username_" + i, RANDOM.nextInt(0, 120), RANDOM.nextBoolean());
  }

  public TestDatabaseObject(UUID id, String name, int age, boolean isCool) {
    this.id = id;
    this.name = name;
    this.age = age;
    this.isCool = isCool;
  }

  public UUID getId() {
    return id;
  }

  @Serializer
  public byte[] serialize() {
    byte[] nameBytes = name.getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + nameBytes.length + Integer.BYTES + Byte.BYTES);
    buffer.putInt(nameBytes.length);
    buffer.put(nameBytes);
    buffer.putInt(age);
    buffer.put((byte) (isCool ? 1 : 0));
    return buffer.array();
  }

  @Override
  public boolean equals(Object raw) {
    if (!(raw instanceof TestDatabaseObject entry))
      return false;
    if (!id.equals(entry.id))
      return false;
    if (!name.equals(entry.name))
      return false;
    if (age != entry.age)
      return false;
    return isCool == entry.isCool;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + age;
    result = 31 * result + (isCool ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TestDatabaseObject{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", age=" + age +
      ", isCool=" + isCool +
      '}';
  }

}
