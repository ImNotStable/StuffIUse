package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.Pair;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public class TestDatabaseObject implements Dirtyable, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Deserializer
  public static TestDatabaseObject deserialize(Pair<ByteTranslatable, ByteTranslatable> entry) {
    UUID id = entry.left().asUUID();

    byte[] bytes = entry.right().asByteArray();

    int nameLength = 0;
    nameLength |= (bytes[0] & 0xFF) << 24;
    nameLength |= (bytes[1] & 0xFF) << 16;
    nameLength |= (bytes[2] & 0xFF) << 8;
    nameLength |= (bytes[3] & 0xFF);

    byte[] nameBytes = new byte[nameLength];
    System.arraycopy(bytes, 4, nameBytes, 0, nameLength);
    String name = new String(nameBytes);

    byte age = bytes[4 + nameLength];

    boolean isCool = bytes[5 + nameLength] == 1;

    return new TestDatabaseObject(id, name, age, isCool);
  }

  @Indexable(id = "id")
  private final UUID id;
  @Indexable(id = "name")
  private final String name;
  @Sorted("age")
  private final byte age;
  private final boolean isCool;

  public TestDatabaseObject(int i) {
    this(new UUID(i, i), "Test_Username_" + i, (byte) (i % 120), (i % 2) == 0);
  }

  public TestDatabaseObject(UUID id, String name, byte age, boolean isCool) {
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

    byte[] bytes = new byte[Integer.BYTES + name.length() + Byte.BYTES + Byte.BYTES];

    int nameLength = name.length();
    bytes[0] = (byte) (nameLength >>> 24);
    bytes[1] = (byte) (nameLength >>> 16);
    bytes[2] = (byte) (nameLength >>> 8);
    bytes[3] = (byte) nameLength;

    int i = 0;
    for (byte b : name.getBytes())
      bytes[4 + i++] = b;

    bytes[4 + nameLength] = age;

    bytes[5 + nameLength] = (byte) (isCool ? 1 : 0);

    return new Pair<>(id, ByteTranslatable.fromByteArray(bytes));
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
