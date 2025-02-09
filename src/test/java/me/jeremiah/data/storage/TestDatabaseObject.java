package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.Pair;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

import static me.jeremiah.data.TestData.RANDOM;

public class TestDatabaseObject implements Dirtyable, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Deserializer
  public static TestDatabaseObject deserialize(Pair<ByteTranslatable, ByteTranslatable> entry) {
    ByteBuffer buffer = ByteBuffer.wrap(entry.right().asByteArray());

    UUID id = entry.left().asUUID();

    byte[] nameBytes = new byte[buffer.getInt()];
    buffer.get(nameBytes);
    String name = new String(nameBytes);
    short age = buffer.getShort();
    boolean isCool = buffer.get() == 1;

    return new TestDatabaseObject(id, name, age, isCool);
  }

  @Indexable(id = "id")
  private final UUID id;
  @Indexable(id = "name")
  private final String name;
  @Sorted("age")
  private final short age;
  private final boolean isCool;

  public TestDatabaseObject(int i) {
    this(new UUID(RANDOM.nextLong(), RANDOM.nextLong()), "Test_Username_" + i, (short) RANDOM.nextInt(0, 120), RANDOM.nextBoolean());
  }

  public TestDatabaseObject(UUID id, String name, short age, boolean isCool) {
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
  public Pair<ByteTranslatable, ByteTranslatable> serialize() {
    ByteTranslatable id = ByteTranslatable.fromUUID(this.id);

    byte[] nameBytes = name.getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + nameBytes.length + Short.BYTES + Byte.BYTES);
    buffer.putInt(nameBytes.length);
    buffer.put(nameBytes);
    buffer.putShort(age);
    buffer.put((byte) (isCool ? 1 : 0));
    return new Pair<>(id, ByteTranslatable.fromByteArray(buffer.array()));
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

  @Override
  public boolean isDirty() {
    return true;
  }

  @Override
  public void markClean() {
  }

}
