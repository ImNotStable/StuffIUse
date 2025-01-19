package me.jeremiah;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.Deserializer;
import me.jeremiah.data.storage.ID;
import me.jeremiah.data.storage.Indexable;
import me.jeremiah.data.storage.Serializer;
import me.jeremiah.data.storage.Sorted;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDatabaseObject {

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
  private String name;
  @Sorted("age")
  private int age;
  private boolean isCool;

  public TestDatabaseObject() {
    this(new UUID(RANDOM.nextLong(), RANDOM.nextLong()), "Test_Username", RANDOM.nextInt(0, 120), RANDOM.nextBoolean());
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public boolean isCool() {
    return isCool;
  }

  public void setCool(boolean cool) {
    isCool = cool;
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

}
