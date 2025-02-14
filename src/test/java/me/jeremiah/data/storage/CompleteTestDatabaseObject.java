package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CompleteTestDatabaseObject implements Dirtyable, Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Deserializer
  public static CompleteTestDatabaseObject deserialize(ByteTranslatable data) {
    byte[] bytes = data.asByteArray();

    long mostSigBits = 0;
    mostSigBits |= (bytes[0] & 0xFFL) << 56;
    mostSigBits |= (bytes[1] & 0xFFL) << 48;
    mostSigBits |= (bytes[2] & 0xFFL) << 40;
    mostSigBits |= (bytes[3] & 0xFFL) << 32;
    mostSigBits |= (bytes[4] & 0xFFL) << 24;
    mostSigBits |= (bytes[5] & 0xFFL) << 16;
    mostSigBits |= (bytes[6] & 0xFFL) << 8;
    mostSigBits |= (bytes[7] & 0xFFL);

    long leastSigBits = 0;
    leastSigBits |= (bytes[8] & 0xFFL) << 56;
    leastSigBits |= (bytes[9] & 0xFFL) << 48;
    leastSigBits |= (bytes[10] & 0xFFL) << 40;
    leastSigBits |= (bytes[11] & 0xFFL) << 32;
    leastSigBits |= (bytes[12] & 0xFFL) << 24;
    leastSigBits |= (bytes[13] & 0xFFL) << 16;
    leastSigBits |= (bytes[14] & 0xFFL) << 8;
    leastSigBits |= (bytes[15] & 0xFFL);

    UUID id = new UUID(mostSigBits, leastSigBits);
    int nameLength = 0;
    nameLength |= (bytes[16] & 0xFF) << 24;
    nameLength |= (bytes[17] & 0xFF) << 16;
    nameLength |= (bytes[18] & 0xFF) << 8;
    nameLength |= (bytes[19] & 0xFF);

    byte[] nameBytes = new byte[nameLength];
    System.arraycopy(bytes, 20, nameBytes, 0, nameLength);
    String name = new String(nameBytes);

    byte age = bytes[20 + nameLength];

    boolean isCool = bytes[21 + nameLength] == 1;

    return new CompleteTestDatabaseObject(id, name, age, isCool);
  }

  @Indexable(id = "id")
  private final UUID id;
  @Indexable(id = "name")
  private final String name;
  @Sorted("age")
  private final byte age;
  private final boolean isCool;

  public CompleteTestDatabaseObject(int i) {
    this(new UUID(i, i), "Test_Username_" + i, (byte) (i % 120), (i % 2) == 0);
  }

  public CompleteTestDatabaseObject(UUID id, String name, byte age, boolean isCool) {
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
    byte[] bytes = new byte[Long.BYTES * 2 + Integer.BYTES + name.length() + Byte.BYTES + Byte.BYTES];

    long mostSigBits = id.getMostSignificantBits();
    bytes[0] = (byte) (mostSigBits >>> 56);
    bytes[1] = (byte) (mostSigBits >>> 48);
    bytes[2] = (byte) (mostSigBits >>> 40);
    bytes[3] = (byte) (mostSigBits >>> 32);
    bytes[4] = (byte) (mostSigBits >>> 24);
    bytes[5] = (byte) (mostSigBits >>> 16);
    bytes[6] = (byte) (mostSigBits >>> 8);
    bytes[7] = (byte) mostSigBits;

    long leastSigBits = id.getLeastSignificantBits();
    bytes[8] = (byte) (leastSigBits >>> 56);
    bytes[9] = (byte) (leastSigBits >>> 48);
    bytes[10] = (byte) (leastSigBits >>> 40);
    bytes[11] = (byte) (leastSigBits >>> 32);
    bytes[12] = (byte) (leastSigBits >>> 24);
    bytes[13] = (byte) (leastSigBits >>> 16);
    bytes[14] = (byte) (leastSigBits >>> 8);
    bytes[15] = (byte) leastSigBits;

    int nameLength = name.length();
    bytes[16] = (byte) (nameLength >>> 24);
    bytes[17] = (byte) (nameLength >>> 16);
    bytes[18] = (byte) (nameLength >>> 8);
    bytes[19] = (byte) nameLength;

    int i = 0;
    for (byte nameByte : name.getBytes())
      bytes[20 + i++] = nameByte;

    bytes[20 + nameLength] = age;

    bytes[21 + nameLength] = (byte) (isCool ? 1 : 0);

    return new ByteTranslatable(bytes);
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
    return Objects.hash(id, name, age, isCool);
  }

  @Override
  public String toString() {
    return String.format("TestDatabaseObject{id=%s, name='%s', age=%d, isCool=%b}", id, name, age, isCool);
  }

  @Override
  public boolean isDirty() {
    return true;
  }

  @Override
  public void markClean() {
  }

}
