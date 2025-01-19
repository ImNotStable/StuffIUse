package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ReflectionUtils {

  public static byte[] serialize(Method serializeMethod, Object object) {
    try {
      return (byte[]) serializeMethod.invoke(object);
    } catch (ReflectiveOperationException exception) {
      throw new RuntimeException("Failed to serialize object", exception);
    }
  }

  public static Method getSerializeMethod(Class<?> serializerClass) {
    return getAnnotatedObject(Arrays.asList(serializerClass.getDeclaredMethods()), Serializer.class)
      .map(method -> (Method) method)
      .orElseThrow(() -> new IllegalArgumentException("Class %s does not have a serialize method".formatted(serializerClass.getName())));
  }

  public static Object deserialize(Method deserializeMethod, Object object) {
    try {
      return deserializeMethod.invoke(null, object);
    } catch (ReflectiveOperationException exception) {
      throw new RuntimeException("Failed to deserialize object", exception);
    }
  }

  public static Method getDeserializeMethod(Class<?> deserializerClass) {
    return getAnnotatedObject(Arrays.asList(deserializerClass.getDeclaredMethods()), Deserializer.class)
      .map(method -> (Method) method)
      .orElseThrow(() -> new IllegalArgumentException("Class %s does not have a deserialize method".formatted(deserializerClass.getName())));
  }

  public static ByteTranslatable getId(Field field, Object object) {
    try {
      return ByteTranslatable.from(field.get(object));
    } catch (IllegalAccessException exception) {
      throw new RuntimeException("Failed to access ID field", exception);
    }
  }

  public static Field getIdField(Class<?> serializableClass) {
    return getAnnotatedObject(Arrays.asList(serializableClass.getDeclaredFields()), ID.class)
      .map(field -> (Field) field)
      .orElseThrow(() -> new IllegalArgumentException("Class %s does not have an ID field".formatted(serializableClass.getName())));
  }

  private static Optional<? extends AccessibleObject> getAnnotatedObject(List<? extends AccessibleObject> objects, Class<? extends Annotation> annotationClass) {
    return objects.stream()
      .filter(field -> field.isAnnotationPresent(annotationClass))
      .peek(field -> field.setAccessible(true))
      .findFirst();
  }

  public static ByteTranslatable getIndex(Field field, Object object) {
    try {
      return ByteTranslatable.from(field.get(object));
    } catch (IllegalAccessException exception) {
      throw new RuntimeException("Failed to access index field", exception);
    }
  }

  public static Map<String, Field> getIndexes(Class<?> serializableClass) {
    return Arrays.stream(serializableClass.getDeclaredFields())
      .filter(field -> field.isAnnotationPresent(Indexable.class) && !field.isAnnotationPresent(ID.class))
      .peek(field -> field.setAccessible(true))
      .collect(
        Collectors.toUnmodifiableMap(field -> field.getAnnotation(Indexable.class).value(), field -> field)
      );
  }

  @SuppressWarnings("unchecked")
  public static <T> int compareSortedFields(@NotNull Field field, @NotNull T sorted1, @NotNull T sorted2) {
    Object value1, value2;
    try {
      value1 = field.get(sorted1);
      value2 = field.get(sorted2);
    } catch (IllegalAccessException exception) {
      throw new RuntimeException("Failed to access field", exception);
    }

    if (value1 == null && value2 == null)
      return 0;
    if (value1 == null)
      return -1;
    if (value2 == null)
      return 1;

    Class<?> type = field.getType();

    if (type.isPrimitive())
      return compareAsPrimitive(type, value1, value2) * -1;

    if (Comparable.class.isAssignableFrom(type)) {
      Comparable<Object> comparable1 = (Comparable<Object>) value1;
      return comparable1.compareTo(value2) * -1;
    }

    throw new IllegalArgumentException("Field type " + type.getName() + " must be primitive or implement Comparable");
  }

  public static Map<String, Field> getSortedFields(Class<?> serializableClass) {
    return Arrays.stream(serializableClass.getDeclaredFields())
      .filter(field -> field.isAnnotationPresent(Sorted.class))
      .filter(field -> {
        if (!Comparable.class.isAssignableFrom(field.getType()) && !field.getType().isPrimitive()) {
          Logger.getGlobal().log(Level.SEVERE, "Field %s does not implement comparable".formatted(field));
          return false;
        }
        return true;
      })
      .peek(field -> field.setAccessible(true))
      .collect(
        Collectors.toUnmodifiableMap(field -> field.getAnnotation(Sorted.class).value(), field -> field)
      );
  }

  private static int compareAsPrimitive(Class<?> type, Object value1, Object value2) {
    if (type == int.class)
      return Integer.compare((int) value1, (int) value2);
    if (type == long.class)
      return Long.compare((long) value1, (long) value2);
    if (type == double.class)
      return Double.compare((double) value1, (double) value2);
    if (type == float.class)
      return Float.compare((float) value1, (float) value2);
    if (type == boolean.class)
      return Boolean.compare((boolean) value1, (boolean) value2);
    if (type == byte.class)
      return Byte.compare((byte) value1, (byte) value2);
    if (type == short.class)
      return Short.compare((short) value1, (short) value2);
    if (type == char.class)
      return Character.compare((char) value1, (char) value2);
    throw new IllegalArgumentException("Unsupported primitive type: " + type.getName());
  }

}
