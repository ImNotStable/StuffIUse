package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

public class TestByteTranslatable {

  @Test
  public void testBoolean() {
    final boolean originalValue = true;
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromBoolean(originalValue);
    final boolean value = byteTranslatable.asBoolean();
    assert originalValue == value;
  }

  @Test
  public void testBooleanArray() {
    final boolean[] originalValue = new boolean[] {true, false, true};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromBooleanArray(originalValue);
    final boolean[] value = byteTranslatable.asBooleanArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testByte() {
    final byte originalValue = 1;
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromByte(originalValue);
    final byte value = byteTranslatable.asByte();
    assert originalValue == value;
  }

  @Test
  public void testByteArray() {
    final byte[] originalValue = new byte[] {1, 2, 3};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromByteArray(originalValue);
    final byte[] value = byteTranslatable.asByteArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testShort() {
    final short originalValue = 1;
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromShort(originalValue);
    final short value = byteTranslatable.asShort();
    assert originalValue == value;
  }

  @Test
  public void testShortArray() {
    final short[] originalValue = new short[] {1, 2, 3};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromShortArray(originalValue);
    final short[] value = byteTranslatable.asShortArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testInt() {
    final int originalValue = 1;
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromInt(originalValue);
    final int value = byteTranslatable.asInt();
    assert originalValue == value;
  }

  @Test
  public void testIntArray() {
    final int[] originalValue = new int[] {1, 2, 3};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromIntArray(originalValue);
    final int[] value = byteTranslatable.asIntArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testLong() {
    final long originalValue = 1;
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromLong(originalValue);
    final long value = byteTranslatable.asLong();
    assert originalValue == value;
  }

  @Test
  public void testLongArray() {
    final long[] originalValue = new long[] {1, 2, 3};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromLongArray(originalValue);
    final long[] value = byteTranslatable.asLongArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testBigInteger() {
    final BigInteger originalValue = BigInteger.valueOf(1L);
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromBigInteger(originalValue);
    final BigInteger value = byteTranslatable.asBigInteger();
    assert originalValue.equals(value);
  }

  @Test
  public void testFloat() {
    final float originalValue = 1;
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromFloat(originalValue);
    final float value = byteTranslatable.asFloat();
    assert originalValue == value;
  }

  @Test
  public void testFloatArray() {
    final float[] originalValue = new float[] {1, 2, 3};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromFloatArray(originalValue);
    final float[] value = byteTranslatable.asFloatArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testDouble() {
    final double originalValue = 1;
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromDouble(originalValue);
    final double value = byteTranslatable.asDouble();
    assert originalValue == value;
  }

  @Test
  public void testDoubleArray() {
    final double[] originalValue = new double[] {1, 2, 3};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromDoubleArray(originalValue);
    final double[] value = byteTranslatable.asDoubleArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testBigDecimal() {
    final BigDecimal originalValue = BigDecimal.valueOf(1);
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromBigDecimal(originalValue);
    final BigDecimal value = byteTranslatable.asBigDecimal();
    assert originalValue.equals(value);
  }

  @Test
  public void testChar() {
    final char originalValue = 'a';
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromChar(originalValue);
    final char value = byteTranslatable.asChar();
    assert originalValue == value;
  }

  @Test
  public void testCharArray() {
    final char[] originalValue = new char[] {'a', 'b', 'c'};
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromCharArray(originalValue);
    final char[] value = byteTranslatable.asCharArray();
    assert Arrays.equals(originalValue, value);
  }

  @Test
  public void testString() {
    final String originalValue = "Hello, World!";
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromString(originalValue);
    final String value = byteTranslatable.asString();
    assert originalValue.equals(value);
  }

  @Test
  public void testUUID() {
    final UUID originalValue = UUID.randomUUID();
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromUUID(originalValue);
    final UUID value = byteTranslatable.asUUID();
    assert originalValue.equals(value);
  }

  @Test
  public void testSerializable() {
    final TestDatabaseObject originalValue = new TestDatabaseObject(0);
    final ByteTranslatable byteTranslatable = ByteTranslatable.fromSerializable(originalValue);
    final TestDatabaseObject value = byteTranslatable.asSerializable();
    assert originalValue.equals(value);
  }

}
