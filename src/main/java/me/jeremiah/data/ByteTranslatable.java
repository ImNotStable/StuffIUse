package me.jeremiah.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    return getMapper(object.getClass()).apply(object);
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
    byte[] bytes = new byte[Short.BYTES];
    bytes[0] = (byte) (value >> 8);
    bytes[1] = (byte) value;
    return new ByteTranslatable(bytes);
  }

  public short asShort() {
    short value = 0;
    value |= (short) (bytes[0] << 8);
    value |= (short) (bytes[1] & 0xFF);
    return value;
  }

  public static ByteTranslatable fromShortArray(short[] value) {
    byte[] bytes = new byte[Short.BYTES * value.length];
    for (int i = 0; i < value.length; i++) {
      bytes[i * Short.BYTES] = (byte) (value[i] >> 8);
      bytes[i * Short.BYTES + 1] = (byte) value[i];
    }
    return new ByteTranslatable(bytes);
  }

  public short[] asShortArray() {
    short[] value = new short[bytes.length / Short.BYTES];
    for (int i = 0; i < value.length; i++) {
      value[i] = 0;
      value[i] |= (short) (bytes[i * Short.BYTES] << 8);
      value[i] |= (short) (bytes[i * Short.BYTES + 1] & 0xFF);
    }
    return value;
  }

  public static ByteTranslatable fromInt(int value) {
    byte[] bytes = new byte[Integer.BYTES];
    bytes[0] = (byte) (value >> 24);
    bytes[1] = (byte) (value >> 16);
    bytes[2] = (byte) (value >> 8);
    bytes[3] = (byte) value;
    return new ByteTranslatable(bytes);
  }

  public int asInt() {
    int value = 0;
    value |= (bytes[0] & 0xFF) << 24;
    value |= (bytes[1] & 0xFF) << 16;
    value |= (bytes[2] & 0xFF) << 8;
    value |= bytes[3] & 0xFF;
    return value;
  }

  public static ByteTranslatable fromIntArray(int[] value) {
    byte[] bytes = new byte[Integer.BYTES * value.length];
    for (int i = 0; i < value.length; i++) {
      bytes[i * Integer.BYTES] = (byte) (value[i] >> 24);
      bytes[i * Integer.BYTES + 1] = (byte) (value[i] >> 16);
      bytes[i * Integer.BYTES + 2] = (byte) (value[i] >> 8);
      bytes[i * Integer.BYTES + 3] = (byte) value[i];
    }
    return new ByteTranslatable(bytes);
  }

  public int[] asIntArray() {
    int[] value = new int[bytes.length / Integer.BYTES];
    for (int i = 0; i < value.length; i++) {
      value[i] = 0;
      value[i] |= (bytes[i * Integer.BYTES] & 0xFF) << 24;
      value[i] |= (bytes[i * Integer.BYTES + 1] & 0xFF) << 16;
      value[i] |= (bytes[i * Integer.BYTES + 2] & 0xFF) << 8;
      value[i] |= bytes[i * Integer.BYTES + 3] & 0xFF;
    }
    return value;
  }

  public static ByteTranslatable fromLong(long value) {
    byte[] bytes = new byte[Long.BYTES];
    bytes[0] = (byte) (value >> 56);
    bytes[1] = (byte) (value >> 48);
    bytes[2] = (byte) (value >> 40);
    bytes[3] = (byte) (value >> 32);
    bytes[4] = (byte) (value >> 24);
    bytes[5] = (byte) (value >> 16);
    bytes[6] = (byte) (value >> 8);
    bytes[7] = (byte) value;
    return new ByteTranslatable(bytes);
  }

  public long asLong() {
    long value = 0;
    value |= (long) (bytes[0] & 0xFF) << 56;
    value |= (long) (bytes[1] & 0xFF) << 48;
    value |= (long) (bytes[2] & 0xFF) << 40;
    value |= (long) (bytes[3] & 0xFF) << 32;
    value |= (long) (bytes[4] & 0xFF) << 24;
    value |= (long) (bytes[5] & 0xFF) << 16;
    value |= (long) (bytes[6] & 0xFF) << 8;
    value |= bytes[7] & 0xFF;
    return value;
  }

  public static ByteTranslatable fromLongArray(long[] value) {
    byte[] bytes = new byte[Long.BYTES * value.length];
    for (int i = 0; i < value.length; i++) {
      bytes[i * Long.BYTES] = (byte) (value[i] >> 56);
      bytes[i * Long.BYTES + 1] = (byte) (value[i] >> 48);
      bytes[i * Long.BYTES + 2] = (byte) (value[i] >> 40);
      bytes[i * Long.BYTES + 3] = (byte) (value[i] >> 32);
      bytes[i * Long.BYTES + 4] = (byte) (value[i] >> 24);
      bytes[i * Long.BYTES + 5] = (byte) (value[i] >> 16);
      bytes[i * Long.BYTES + 6] = (byte) (value[i] >> 8);
      bytes[i * Long.BYTES + 7] = (byte) value[i];
    }
    return new ByteTranslatable(bytes);
  }

  public long[] asLongArray() {
    long[] value = new long[bytes.length / Long.BYTES];
    for (int i = 0; i < value.length; i++) {
      value[i] = 0;
      value[i] |= (long) (bytes[i * Long.BYTES] & 0xFF) << 56;
      value[i] |= (long) (bytes[i * Long.BYTES + 1] & 0xFF) << 48;
      value[i] |= (long) (bytes[i * Long.BYTES + 2] & 0xFF) << 40;
      value[i] |= (long) (bytes[i * Long.BYTES + 3] & 0xFF) << 32;
      value[i] |= (long) (bytes[i * Long.BYTES + 4] & 0xFF) << 24;
      value[i] |= (long) (bytes[i * Long.BYTES + 5] & 0xFF) << 16;
      value[i] |= (long) (bytes[i * Long.BYTES + 6] & 0xFF) << 8;
      value[i] |= bytes[i * Long.BYTES + 7] & 0xFF;
    }
    return value;
  }

  public static ByteTranslatable fromBigInteger(BigInteger value) {
    return new ByteTranslatable(value.toByteArray());
  }

  public BigInteger asBigInteger() {
    return new BigInteger(bytes);
  }

  public static ByteTranslatable fromFloat(float value) {
    int bits = Float.floatToIntBits(value);
    byte[] bytes = new byte[Float.BYTES];
    bytes[0] = (byte) (bits >> 24);
    bytes[1] = (byte) (bits >> 16);
    bytes[2] = (byte) (bits >> 8);
    bytes[3] = (byte) bits;
    return new ByteTranslatable(bytes);
  }

  public float asFloat() {
    int bits = 0;
    bits |= (bytes[0] & 0xFF) << 24;
    bits |= (bytes[1] & 0xFF) << 16;
    bits |= (bytes[2] & 0xFF) << 8;
    bits |= bytes[3] & 0xFF;
    return Float.intBitsToFloat(bits);
  }

  public static ByteTranslatable fromFloatArray(float[] value) {
    byte[] bytes = new byte[Float.BYTES * value.length];
    for (int i = 0; i < value.length; i++) {
      int bits = Float.floatToIntBits(value[i]);
      bytes[i * Float.BYTES] = (byte) (bits >> 24);
      bytes[i * Float.BYTES + 1] = (byte) (bits >> 16);
      bytes[i * Float.BYTES + 2] = (byte) (bits >> 8);
      bytes[i * Float.BYTES + 3] = (byte) bits;
    }
    return new ByteTranslatable(bytes);
  }

  public float[] asFloatArray() {
    float[] value = new float[bytes.length / Float.BYTES];
    for (int i = 0; i < value.length; i++) {
      int bits = 0;
      bits |= (bytes[i * Float.BYTES] & 0xFF) << 24;
      bits |= (bytes[i * Float.BYTES + 1] & 0xFF) << 16;
      bits |= (bytes[i * Float.BYTES + 2] & 0xFF) << 8;
      bits |= bytes[i * Float.BYTES + 3] & 0xFF;
      value[i] = Float.intBitsToFloat(bits);
    }
    return value;
  }

  public static ByteTranslatable fromDouble(double value) {
    long bits = Double.doubleToLongBits(value);
    byte[] bytes = new byte[Double.BYTES];
    bytes[0] = (byte) (bits >> 56);
    bytes[1] = (byte) (bits >> 48);
    bytes[2] = (byte) (bits >> 40);
    bytes[3] = (byte) (bits >> 32);
    bytes[4] = (byte) (bits >> 24);
    bytes[5] = (byte) (bits >> 16);
    bytes[6] = (byte) (bits >> 8);
    bytes[7] = (byte) bits;
    return new ByteTranslatable(bytes);
  }

  public double asDouble() {
    long bits = 0;
    bits |= (long) (bytes[0] & 0xFF) << 56;
    bits |= (long) (bytes[1] & 0xFF) << 48;
    bits |= (long) (bytes[2] & 0xFF) << 40;
    bits |= (long) (bytes[3] & 0xFF) << 32;
    bits |= (long) (bytes[4] & 0xFF) << 24;
    bits |= (long) (bytes[5] & 0xFF) << 16;
    bits |= (long) (bytes[6] & 0xFF) << 8;
    bits |= bytes[7] & 0xFF;
    return Double.longBitsToDouble(bits);
  }

  public static ByteTranslatable fromDoubleArray(double[] value) {
    byte[] bytes = new byte[Double.BYTES * value.length];
    for (int i = 0; i < value.length; i++) {
      long bits = Double.doubleToLongBits(value[i]);
      bytes[i * Double.BYTES] = (byte) (bits >> 56);
      bytes[i * Double.BYTES + 1] = (byte) (bits >> 48);
      bytes[i * Double.BYTES + 2] = (byte) (bits >> 40);
      bytes[i * Double.BYTES + 3] = (byte) (bits >> 32);
      bytes[i * Double.BYTES + 4] = (byte) (bits >> 24);
      bytes[i * Double.BYTES + 5] = (byte) (bits >> 16);
      bytes[i * Double.BYTES + 6] = (byte) (bits >> 8);
      bytes[i * Double.BYTES + 7] = (byte) bits;
    }
    return new ByteTranslatable(bytes);
  }

  public double[] asDoubleArray() {
    double[] value = new double[bytes.length / Double.BYTES];
    for (int i = 0; i < value.length; i++) {
      long bits = 0;
      bits |= (long) (bytes[i * Double.BYTES] & 0xFF) << 56;
      bits |= (long) (bytes[i * Double.BYTES + 1] & 0xFF) << 48;
      bits |= (long) (bytes[i * Double.BYTES + 2] & 0xFF) << 40;
      bits |= (long) (bytes[i * Double.BYTES + 3] & 0xFF) << 32;
      bits |= (long) (bytes[i * Double.BYTES + 4] & 0xFF) << 24;
      bits |= (long) (bytes[i * Double.BYTES + 5] & 0xFF) << 16;
      bits |= (long) (bytes[i * Double.BYTES + 6] & 0xFF) << 8;
      bits |= bytes[i * Double.BYTES + 7] & 0xFF;
      value[i] = Double.longBitsToDouble(bits);
    }
    return value;
  }

  public static ByteTranslatable fromBigDecimal(BigDecimal value) {
    byte[] bytes = new byte[value.toBigInteger().toByteArray().length + Integer.BYTES];
    byte[] bigIntBytes = value.toBigInteger().toByteArray();
    System.arraycopy(bigIntBytes, 0, bytes, 0, bigIntBytes.length);
    bytes[bigIntBytes.length] = (byte) value.scale();
    return new ByteTranslatable(bytes);
  }

  public BigDecimal asBigDecimal() {
    byte[] bigIntBytes = new byte[bytes.length - Integer.BYTES];
    System.arraycopy(bytes, 0, bigIntBytes, 0, bigIntBytes.length);
    BigInteger bigInt = new BigInteger(bigIntBytes);
    int scale = bytes[bigIntBytes.length];
    return new BigDecimal(bigInt, scale);
  }

  public static ByteTranslatable fromChar(char value) {
    return fromByte((byte) value);
  }

  public char asChar() {
    return (char) asByte();
  }

  public static ByteTranslatable fromCharArray(char[] value) {
    byte[] bytes = new byte[Character.BYTES * value.length];
    for (int i = 0; i < value.length; i++)
      bytes[i] = (byte) value[i];
    return new ByteTranslatable(bytes);
  }

  public char[] asCharArray() {
    char[] value = new char[bytes.length / Character.BYTES];
    for (int i = 0; i < value.length; i++)
      value[i] = (char) bytes[i];
    return value;
  }

  public static ByteTranslatable fromString(String value) {
    return new ByteTranslatable(value.getBytes());
  }

  public String asString() {
    return new String(bytes);
  }

  public static ByteTranslatable fromUUID(UUID value) {
    byte[] bytes = new byte[Long.BYTES * 2];
    bytes[0] = (byte) (value.getMostSignificantBits() >> 56);
    bytes[1] = (byte) (value.getMostSignificantBits() >> 48);
    bytes[2] = (byte) (value.getMostSignificantBits() >> 40);
    bytes[3] = (byte) (value.getMostSignificantBits() >> 32);
    bytes[4] = (byte) (value.getMostSignificantBits() >> 24);
    bytes[5] = (byte) (value.getMostSignificantBits() >> 16);
    bytes[6] = (byte) (value.getMostSignificantBits() >> 8);
    bytes[7] = (byte) value.getMostSignificantBits();
    bytes[8] = (byte) (value.getLeastSignificantBits() >> 56);
    bytes[9] = (byte) (value.getLeastSignificantBits() >> 48);
    bytes[10] = (byte) (value.getLeastSignificantBits() >> 40);
    bytes[11] = (byte) (value.getLeastSignificantBits() >> 32);
    bytes[12] = (byte) (value.getLeastSignificantBits() >> 24);
    bytes[13] = (byte) (value.getLeastSignificantBits() >> 16);
    bytes[14] = (byte) (value.getLeastSignificantBits() >> 8);
    bytes[15] = (byte) value.getLeastSignificantBits();
    return new ByteTranslatable(bytes);
  }

  public UUID asUUID() {
    long mostSigBits = 0, leastSigBits = 0;
    mostSigBits |= (long) (bytes[0] & 0xFF) << 56;
    mostSigBits |= (long) (bytes[1] & 0xFF) << 48;
    mostSigBits |= (long) (bytes[2] & 0xFF) << 40;
    mostSigBits |= (long) (bytes[3] & 0xFF) << 32;
    mostSigBits |= (long) (bytes[4] & 0xFF) << 24;
    mostSigBits |= (long) (bytes[5] & 0xFF) << 16;
    mostSigBits |= (long) (bytes[6] & 0xFF) << 8;
    mostSigBits |= bytes[7] & 0xFF;
    leastSigBits |= (long) (bytes[8] & 0xFF) << 56;
    leastSigBits |= (long) (bytes[9] & 0xFF) << 48;
    leastSigBits |= (long) (bytes[10] & 0xFF) << 40;
    leastSigBits |= (long) (bytes[11] & 0xFF) << 32;
    leastSigBits |= (long) (bytes[12] & 0xFF) << 24;
    leastSigBits |= (long) (bytes[13] & 0xFF) << 16;
    leastSigBits |= (long) (bytes[14] & 0xFF) << 8;
    leastSigBits |= bytes[15] & 0xFF;
    return new UUID(mostSigBits, leastSigBits);
  }

  public static ByteTranslatable fromLocation(Location location) {
    byte[] bytes = new byte[Long.BYTES * 2 + Double.BYTES * 3 + Float.BYTES * 2];

    long mostSigBits = location.getWorld().getUID().getMostSignificantBits();
    bytes[0] = (byte) (mostSigBits >> 56);
    bytes[1] = (byte) (mostSigBits >> 48);
    bytes[2] = (byte) (mostSigBits >> 40);
    bytes[3] = (byte) (mostSigBits >> 32);
    bytes[4] = (byte) (mostSigBits >> 24);
    bytes[5] = (byte) (mostSigBits >> 16);
    bytes[6] = (byte) (mostSigBits >> 8);
    bytes[7] = (byte) mostSigBits;

    long leastSigBits = location.getWorld().getUID().getLeastSignificantBits();
    bytes[8] = (byte) (leastSigBits >> 56);
    bytes[9] = (byte) (leastSigBits >> 48);
    bytes[10] = (byte) (leastSigBits >> 40);
    bytes[11] = (byte) (leastSigBits >> 32);
    bytes[12] = (byte) (leastSigBits >> 24);
    bytes[13] = (byte) (leastSigBits >> 16);
    bytes[14] = (byte) (leastSigBits >> 8);
    bytes[15] = (byte) leastSigBits;

    long x = Double.doubleToLongBits(location.getX());
    bytes[16] = (byte) (x >> 56);
    bytes[17] = (byte) (x >> 48);
    bytes[18] = (byte) (x >> 40);
    bytes[19] = (byte) (x >> 32);
    bytes[20] = (byte) (x >> 24);
    bytes[21] = (byte) (x >> 16);
    bytes[22] = (byte) (x >> 8);
    bytes[23] = (byte) x;

    long y = Double.doubleToLongBits(location.getY());
    bytes[24] = (byte) (y >> 56);
    bytes[25] = (byte) (y >> 48);
    bytes[26] = (byte) (y >> 40);
    bytes[27] = (byte) (y >> 32);
    bytes[28] = (byte) (y >> 24);
    bytes[29] = (byte) (y >> 16);
    bytes[30] = (byte) (y >> 8);
    bytes[31] = (byte) y;

    long z = Double.doubleToLongBits(location.getZ());
    bytes[32] = (byte) (z >> 56);
    bytes[33] = (byte) (z >> 48);
    bytes[34] = (byte) (z >> 40);
    bytes[35] = (byte) (z >> 32);
    bytes[36] = (byte) (z >> 24);
    bytes[37] = (byte) (z >> 16);
    bytes[38] = (byte) (z >> 8);
    bytes[39] = (byte) z;

    int yaw = Float.floatToIntBits(location.getYaw());
    bytes[40] = (byte) (yaw >> 24);
    bytes[41] = (byte) (yaw >> 16);
    bytes[42] = (byte) (yaw >> 8);
    bytes[43] = (byte) yaw;

    int pitch = Float.floatToIntBits(location.getPitch());
    bytes[44] = (byte) (pitch >> 24);
    bytes[45] = (byte) (pitch >> 16);
    bytes[46] = (byte) (pitch >> 8);
    bytes[47] = (byte) pitch;

    return new ByteTranslatable(bytes);
  }

  public Location asLocation() {
    long mostSigBits = 0;
    mostSigBits |= (long) (bytes[0] & 0xFF) << 56;
    mostSigBits |= (long) (bytes[1] & 0xFF) << 48;
    mostSigBits |= (long) (bytes[2] & 0xFF) << 40;
    mostSigBits |= (long) (bytes[3] & 0xFF) << 32;
    mostSigBits |= (long) (bytes[4] & 0xFF) << 24;
    mostSigBits |= (long) (bytes[5] & 0xFF) << 16;
    mostSigBits |= (long) (bytes[6] & 0xFF) << 8;
    mostSigBits |= bytes[7] & 0xFF;

    long leastSigBits = 0;
    leastSigBits |= (long) (bytes[8] & 0xFF) << 56;
    leastSigBits |= (long) (bytes[9] & 0xFF) << 48;
    leastSigBits |= (long) (bytes[10] & 0xFF) << 40;
    leastSigBits |= (long) (bytes[11] & 0xFF) << 32;
    leastSigBits |= (long) (bytes[12] & 0xFF) << 24;
    leastSigBits |= (long) (bytes[13] & 0xFF) << 16;
    leastSigBits |= (long) (bytes[14] & 0xFF) << 8;
    leastSigBits |= bytes[15] & 0xFF;

    long x = 0;
    x |= (long) (bytes[16] & 0xFF) << 56;
    x |= (long) (bytes[17] & 0xFF) << 48;
    x |= (long) (bytes[18] & 0xFF) << 40;
    x |= (long) (bytes[19] & 0xFF) << 32;
    x |= (long) (bytes[20] & 0xFF) << 24;
    x |= (long) (bytes[21] & 0xFF) << 16;
    x |= (long) (bytes[22] & 0xFF) << 8;
    x |= bytes[23] & 0xFF;

    long y = 0;
    y |= (long) (bytes[24] & 0xFF) << 56;
    y |= (long) (bytes[25] & 0xFF) << 48;
    y |= (long) (bytes[26] & 0xFF) << 40;
    y |= (long) (bytes[27] & 0xFF) << 32;
    y |= (long) (bytes[28] & 0xFF) << 24;
    y |= (long) (bytes[29] & 0xFF) << 16;
    y |= (long) (bytes[30] & 0xFF) << 8;
    y |= bytes[31] & 0xFF;

    long z = 0;
    z |= (long) (bytes[32] & 0xFF) << 56;
    z |= (long) (bytes[33] & 0xFF) << 48;
    z |= (long) (bytes[34] & 0xFF) << 40;
    z |= (long) (bytes[35] & 0xFF) << 32;
    z |= (long) (bytes[36] & 0xFF) << 24;
    z |= (long) (bytes[37] & 0xFF) << 16;
    z |= (long) (bytes[38] & 0xFF) << 8;
    z |= bytes[39] & 0xFF;

    int yaw = 0;
    yaw |= (bytes[40] & 0xFF) << 24;
    yaw |= (bytes[41] & 0xFF) << 16;
    yaw |= (bytes[42] & 0xFF) << 8;
    yaw |= bytes[43] & 0xFF;

    int pitch = 0;
    pitch |= (bytes[44] & 0xFF) << 24;
    pitch |= (bytes[45] & 0xFF) << 16;
    pitch |= (bytes[46] & 0xFF) << 8;
    pitch |= bytes[47] & 0xFF;

    return new Location(
      Bukkit.getWorld(new UUID(mostSigBits, leastSigBits)),
      Double.longBitsToDouble(x),
      Double.longBitsToDouble(y),
      Double.longBitsToDouble(z),
      Float.intBitsToFloat(yaw),
      Float.intBitsToFloat(pitch));
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
