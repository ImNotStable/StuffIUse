package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CompleteTestDatabaseObject implements Dirtyable, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

  @Deserializer
  public static CompleteTestDatabaseObject deserialize(byte[] data) {
    ByteBuffer buffer = ByteBuffer.wrap(data);

    UUID id = new UUID(buffer.getLong(), buffer.getLong());
    byte[] nameBytes = new byte[buffer.getInt()];
    buffer.get(nameBytes);
    String name = new String(nameBytes);
    short age = buffer.getShort();
    boolean isCool = buffer.get() == 1;

    return new CompleteTestDatabaseObject(id, name, age, isCool);
  }

  @ID
  private final UUID id;

  @Indexable("name")
  private final String name;
  @Sorted("age")
  private final short age;
  private final boolean isCool;

  private transient boolean dirty = true;

  public CompleteTestDatabaseObject(int i) {
    this(new UUID(RANDOM.nextLong(), RANDOM.nextLong()), "Test_Username_" + i, (short) RANDOM.nextInt(0, 120), RANDOM.nextBoolean());
  }

  public CompleteTestDatabaseObject(UUID id, String name, short age, boolean isCool) {
    this.id = id;
    this.name = name;
    this.age = age;
    this.isCool = isCool;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Serializer
  public ByteTranslatable serialize() {
    byte[] nameBytes = name.getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2 + Integer.BYTES + nameBytes.length + Short.BYTES + Byte.BYTES);
    buffer.putLong(id.getMostSignificantBits());
    buffer.putLong(id.getLeastSignificantBits());
    buffer.putInt(nameBytes.length);
    buffer.put(nameBytes);
    buffer.putShort(age);
    buffer.put((byte) (isCool ? 1 : 0));
    return new ByteTranslatable(buffer.array());
  }

  @Override
  public boolean equals(Object raw) {
    if (!(raw instanceof CompleteTestDatabaseObject entry))
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

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void markClean() {
    dirty = false;
  }

}
