package me.jeremiah.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public record ByteTranslatable(byte[] bytes) {

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ByteTranslatable(byte[] bytes1)))
      return false;
    if (bytes.length != bytes1.length) return false;
    return Arrays.equals(bytes, bytes1);
  }

  private static final Map<Class<?>, Function<Object, ByteTranslatable>> mappersByClass = new HashMap<Class<?>, Function<Object, ByteTranslatable>>() {

    @SuppressWarnings("unchecked")
    <T> void putClass(Class<T> tClass, Function<T, ByteTranslatable> mapper) {
      put(tClass, obj -> mapper.apply((T) obj));
    }

    {
      putClass(ByteTranslatable.class, Function.identity());
      putClass(ByteTranslatable[].class, ByteTranslatable::fromByteTranslatables);
      putClass(Boolean.class, ByteTranslatable::fromBoolean);
      putClass(boolean[].class, ByteTranslatable::fromBooleanArray);
      putClass(Byte.class, ByteTranslatable::fromByte);
      putClass(byte[].class, ByteTranslatable::fromByteArray);
      putClass(Short.class, ByteTranslatable::fromShort);
      putClass(short[].class, ByteTranslatable::fromShortArray);
      putClass(Integer.class, ByteTranslatable::fromInt);
      putClass(int[].class, ByteTranslatable::fromIntArray);
      putClass(Long.class, ByteTranslatable::fromLong);
      putClass(long[].class, ByteTranslatable::fromLongArray);
      putClass(BigInteger.class, ByteTranslatable::fromBigInteger);
      putClass(Float.class, ByteTranslatable::fromFloat);
      putClass(float[].class, ByteTranslatable::fromFloatArray);
      putClass(Double.class, ByteTranslatable::fromDouble);
      putClass(double[].class, ByteTranslatable::fromDoubleArray);
      putClass(BigDecimal.class, ByteTranslatable::fromBigDecimal);
      putClass(Character.class, ByteTranslatable::fromChar);
      putClass(char[].class, ByteTranslatable::fromCharArray);
      putClass(String.class, ByteTranslatable::fromString);
      putClass(UUID.class, ByteTranslatable::fromUUID);
      putClass(Location.class, ByteTranslatable::fromLocation);
    }
  };

  public static void register(Class<?> clazz, Function<Object, ByteTranslatable> mapper) {
    mappersByClass.put(clazz, mapper);
  }

  public static Function<Object, ByteTranslatable> getMapper(Class<?> clazz) {
    Function<Object, ByteTranslatable> mapper = mappersByClass.get(clazz);
    if (mapper == null)
      throw new IllegalArgumentException("No mapper found for class " + clazz.getName());
    return mapper;
  }

  public static ByteTranslatable from(@NotNull Object object) {
    return mappersByClass.get(object.getClass()).apply(object);
  }

  public static ByteTranslatable fromByteTranslatables(ByteTranslatable... byteTranslatables) {
    int length = 0;
    for (ByteTranslatable byteTranslatable : byteTranslatables) {
      length += byteTranslatable.bytes.length;
    }
    byte[] bytes = new byte[length];
    int index = 0;
    for (ByteTranslatable byteTranslatable : byteTranslatables) {
      System.arraycopy(byteTranslatable.bytes, 0, bytes, index, byteTranslatable.bytes.length);
      index += byteTranslatable.bytes.length;
    }
    return new ByteTranslatable(bytes);
  }

  public static ByteTranslatable fromBoolean(boolean value) {
    return new ByteTranslatable(new byte[]{(byte) (value ? 1 : 0)});
  }

  public boolean asBoolean() {
    return bytes[0] == 1;
  }

  public static ByteTranslatable fromBooleanArray(boolean[] value) {
    byte[] bytes = new byte[value.length];
    for (int i = 0; i < value.length; i++)
      bytes[i] = (byte) (value[i] ? 1 : 0);
    return new ByteTranslatable(bytes);
  }

  public boolean[] asBooleanArray() {
    boolean[] value = new boolean[bytes.length];
    for (int i = 0; i < bytes.length; i++)
      value[i] = bytes[i] == 1;
    return value;
  }

  public static ByteTranslatable fromByte(byte value) {
    return new ByteTranslatable(new byte[]{value});
  }

  public byte asByte() {
    return bytes[0];
  }

  public static ByteTranslatable fromByteArray(byte[] value) {
    return new ByteTranslatable(value);
  }

  public byte[] asByteArray() {
    return bytes;
  }

  public static ByteTranslatable fromShort(short value) {
    return new ByteTranslatable(ByteBuffer.allocate(Short.BYTES).putShort(value).array());
  }

  public short asShort() {
    return ByteBuffer.wrap(bytes).getShort();
  }

  public static ByteTranslatable fromShortArray(short[] value) {
    ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES * value.length);
    for (short s : value)
      buffer.putShort(s);
    return new ByteTranslatable(buffer.array());
  }

  public short[] asShortArray() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    short[] value = new short[bytes.length / Short.BYTES];
    for (int i = 0; i < value.length; i++)
      value[i] = buffer.getShort();
    return value;
  }

  public static ByteTranslatable fromInt(int value) {
    return new ByteTranslatable(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
  }

  public int asInt() {
    return ByteBuffer.wrap(bytes).getInt();
  }

  public static ByteTranslatable fromIntArray(int[] value) {
    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * value.length);
    for (int i : value)
      buffer.putInt(i);
    return new ByteTranslatable(buffer.array());
  }

  public int[] asIntArray() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    int[] value = new int[bytes.length / Integer.BYTES];
    for (int i = 0; i < value.length; i++)
      value[i] = buffer.getInt();
    return value;
  }

  public static ByteTranslatable fromLong(long value) {
    return new ByteTranslatable(ByteBuffer.allocate(Long.BYTES).putLong(value).array());
  }

  public long asLong() {
    return ByteBuffer.wrap(bytes).getLong();
  }

  public static ByteTranslatable fromLongArray(long[] value) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * value.length);
    for (long l : value)
      buffer.putLong(l);
    return new ByteTranslatable(buffer.array());
  }

  public long[] asLongArray() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    long[] value = new long[bytes.length / Long.BYTES];
    for (int i = 0; i < value.length; i++)
      value[i] = buffer.getLong();
    return value;
  }

  public static ByteTranslatable fromBigInteger(BigInteger value) {
    return new ByteTranslatable(value.toByteArray());
  }

  public BigInteger asBigInteger() {
    return new BigInteger(bytes);
  }

  public static ByteTranslatable fromFloat(float value) {
    return new ByteTranslatable(ByteBuffer.allocate(Float.BYTES).putFloat(value).array());
  }

  public float asFloat() {
    return ByteBuffer.wrap(bytes).getFloat();
  }

  public static ByteTranslatable fromFloatArray(float[] value) {
    ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES * value.length);
    for (float f : value)
      buffer.putFloat(f);
    return new ByteTranslatable(buffer.array());
  }

  public float[] asFloatArray() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    float[] value = new float[bytes.length / Float.BYTES];
    for (int i = 0; i < value.length; i++)
      value[i] = buffer.getFloat();
    return value;
  }

  public static ByteTranslatable fromDouble(double value) {
    return new ByteTranslatable(ByteBuffer.allocate(Double.BYTES).putDouble(value).array());
  }

  public double asDouble() {
    return ByteBuffer.wrap(bytes).getDouble();
  }

  public static ByteTranslatable fromDoubleArray(double[] value) {
    ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES * value.length);
    for (double d : value)
      buffer.putDouble(d);
    return new ByteTranslatable(buffer.array());
  }

  public double[] asDoubleArray() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    double[] value = new double[bytes.length / Double.BYTES];
    for (int i = 0; i < value.length; i++)
      value[i] = buffer.getDouble();
    return value;
  }

  public static ByteTranslatable fromBigDecimal(BigDecimal value) {
    ByteBuffer buffer = ByteBuffer.allocate(value.toBigInteger().toByteArray().length + Integer.BYTES);
    buffer.put(value.toBigInteger().toByteArray());
    buffer.putInt(value.scale());
    return new ByteTranslatable(buffer.array());
  }

  public BigDecimal asBigDecimal() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    byte[] bigIntBytes = new byte[bytes.length - Integer.BYTES];
    buffer.get(bigIntBytes);
    int scale = buffer.getInt();
    return new BigDecimal(new BigInteger(bigIntBytes), scale);
  }

  public static ByteTranslatable fromChar(char value) {
    return new ByteTranslatable(ByteBuffer.allocate(Character.BYTES).putChar(value).array());
  }

  public char asChar() {
    return ByteBuffer.wrap(bytes).getChar();
  }

  public static ByteTranslatable fromCharArray(char[] value) {
    ByteBuffer buffer = ByteBuffer.allocate(Character.BYTES * value.length);
    for (char c : value)
      buffer.putChar(c);
    return new ByteTranslatable(buffer.array());
  }

  public char[] asCharArray() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    char[] value = new char[bytes.length / Character.BYTES];
    for (int i = 0; i < value.length; i++)
      value[i] = buffer.getChar();
    return value;
  }

  public static ByteTranslatable fromString(String value) {
    return new ByteTranslatable(value.getBytes());
  }

  public String asString() {
    return new String(bytes);
  }

  public static ByteTranslatable fromUUID(UUID value) {
    return new ByteTranslatable(
      ByteBuffer.allocate(Long.BYTES * 2)
        .putLong(value.getMostSignificantBits())
        .putLong(value.getLeastSignificantBits())
        .array()
    );
  }

  public UUID asUUID() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    long mostSigBits = buffer.getLong();
    long leastSigBits = buffer.getLong();
    return new UUID(mostSigBits, leastSigBits);
  }

  public static ByteTranslatable fromLocation(Location location) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2 + Double.BYTES * 3 + Float.BYTES * 2);

    UUID worldUUID = location.getWorld().getUID();
    buffer.putLong(worldUUID.getMostSignificantBits());
    buffer.putLong(worldUUID.getLeastSignificantBits());

    buffer.putDouble(location.getX());
    buffer.putDouble(location.getY());
    buffer.putDouble(location.getZ());

    buffer.putFloat(location.getYaw());
    buffer.putFloat(location.getPitch());

    return new ByteTranslatable(buffer.array());
  }

  public Location asLocation() {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    long mostSigBits = buffer.getLong();
    long leastSigBits = buffer.getLong();
    World world = Bukkit.getWorld(new UUID(mostSigBits, leastSigBits));

    double x = buffer.getDouble();
    double y = buffer.getDouble();
    double z = buffer.getDouble();

    float yaw = buffer.getFloat();
    float pitch = buffer.getFloat();

    return new Location(world, x, y, z, yaw, pitch);
  }

  public static ByteTranslatable fromSerializable(Serializable value) {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      objectStream.writeObject(value);
      objectStream.flush();
      return new ByteTranslatable(byteStream.toByteArray());
    } catch (Exception exception) {
      throw new RuntimeException("Failed to serialize object.", exception);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> T asSerializable() {
    try (ObjectInputStream objectStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return (T) objectStream.readObject();
    } catch (Exception exception) {
      throw new RuntimeException("Failed to deserialize object.", exception);
    }
  }

}
