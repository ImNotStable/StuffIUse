package me.jeremiah.data;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

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

  public static ByteTranslatable from(@NotNull Object object) {
    return switch (object) {
      case ByteTranslatable byteTranslatable -> byteTranslatable;
      case byte[] b -> fromBytes(b);
      case Boolean b -> fromBoolean(b);
      case Byte b -> fromBytes(b);
      case Short i -> fromShort(i);
      case Integer i -> fromInt(i);
      case Long l -> fromLong(l);
      case Float v -> fromFloat(v);
      case Double v -> fromDouble(v);
      case Character c -> fromChar(c);
      case String string -> fromString(string);
      case UUID uuid -> fromUUID(uuid);
      default -> throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
    };
  }

  public static ByteTranslatable fromBoolean(boolean value) {
    return new ByteTranslatable(new byte[]{(byte) (value ? 1 : 0)});
  }

  public boolean asBoolean() {
    return bytes[0] == 1;
  }

  public static ByteTranslatable fromBytes(byte... value) {
    return new ByteTranslatable(value);
  }

  public byte asByte() {
    return bytes[0];
  }

  public static ByteTranslatable fromShort(short value) {
    return new ByteTranslatable(ByteBuffer.allocate(Short.BYTES).putShort(value).array());
  }

  public short asShort() {
    return ByteBuffer.wrap(bytes).getShort();
  }

  public static ByteTranslatable fromInt(int value) {
    return new ByteTranslatable(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
  }

  public int asInt() {
    return ByteBuffer.wrap(bytes).getInt();
  }

  public static ByteTranslatable fromLong(long value) {
    return new ByteTranslatable(ByteBuffer.allocate(Long.BYTES).putLong(value).array());
  }

  public long asLong() {
    return ByteBuffer.wrap(bytes).getLong();
  }

  public static ByteTranslatable fromFloat(float value) {
    return new ByteTranslatable(ByteBuffer.allocate(Float.BYTES).putFloat(value).array());
  }

  public float asFloat() {
    return ByteBuffer.wrap(bytes).getFloat();
  }

  public static ByteTranslatable fromDouble(double value) {
    return new ByteTranslatable(ByteBuffer.allocate(Double.BYTES).putDouble(value).array());
  }

  public double asDouble() {
    return ByteBuffer.wrap(bytes).getDouble();
  }

  public static ByteTranslatable fromChar(char value) {
    return new ByteTranslatable(ByteBuffer.allocate(Character.BYTES).putChar(value).array());
  }

  public char asChar() {
    return ByteBuffer.wrap(bytes).getChar();
  }

  public static ByteTranslatable fromString(String value) {
    return new ByteTranslatable(value.getBytes(StandardCharsets.UTF_8));
  }

  public String asString() {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static final int UUID_BYTE_SIZE = Long.BYTES * 2;

  public static ByteTranslatable fromUUID(UUID value) {
    return new ByteTranslatable(
      ByteBuffer.allocate(UUID_BYTE_SIZE)
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

}
