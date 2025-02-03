package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ReflectionUtils {

  public static ByteTranslatable serialize(Method serializeMethod, Object object) {
    try {
      return (ByteTranslatable) serializeMethod.invoke(object);
    } catch (ReflectiveOperationException exception) {
      throw new RuntimeException("Failed to serialize object", exception);
    }
  }

  public static Method getSerializeMethod(Class<?> serializerClass) {
    Stream<Method> methodStream = getAnnotatedObjects(Arrays.asList(serializerClass.getDeclaredMethods()), Serializer.class);
    List<Method> methods = methodStream.toList();

    if (!methods.isEmpty()) {
      if (methods.size() > 1)
        Logger.getGlobal().log(Level.WARNING, "Multiple methods annotated with @Serializer found in %s.class, using first one.".formatted(serializerClass.getName()));
      return methods.getFirst();
    }

    throw new IllegalArgumentException("Failed to find a method annotated with @Serializer within %s.class".formatted(serializerClass.getName()));
  }

  @SuppressWarnings("unchecked")
  public static <T> T deserialize(Method deserializeMethod, Object arg) {
    try {
      return (T) deserializeMethod.invoke(null, arg);
    } catch (ReflectiveOperationException exception) {
      throw new RuntimeException("Failed to deserialize object", exception);
    }
  }

  public static Method getDeserializeMethod(Class<?> deserializerClass) {
    Stream<Method> methodStream = getAnnotatedObjects(Arrays.asList(deserializerClass.getDeclaredMethods()), Deserializer.class);
    List<Method> methods = methodStream.toList();

    if (!methods.isEmpty()) {
      if (methods.size() > 1)
        Logger.getGlobal().log(Level.WARNING, "Multiple methods annotated with @Deserializer found in %s.class, using first one.".formatted(deserializerClass.getName()));
      return methods.getFirst();
    }

    throw new IllegalArgumentException("Failed to find a method annotated with @Deserializer within %s.class".formatted(deserializerClass.getName()));
  }

  public static Field getIdField(Class<?> serializableClass) {
    Stream<Field> annotatedFieldStream = getAnnotatedObjects(Arrays.asList(serializableClass.getDeclaredFields()), ID.class);
    List<Field> annotatedFields = annotatedFieldStream.toList();

    if (!annotatedFields.isEmpty()) {
      if (annotatedFields.size() > 1)
        Logger.getGlobal().log(Level.WARNING, "Multiple fields annotated with @ID found in %s.class, using first one.".formatted(serializableClass.getName()));
      return annotatedFields.getFirst();
    }

    Field[] finalFields = getFinalFields(serializableClass);

    if (finalFields.length > 0) {
      if (finalFields.length > 1)
        Logger.getGlobal().log(Level.WARNING, "Multiple final field candidates found for @ID in %s.class, using first one.".formatted(serializableClass.getName()));
      return finalFields[0];
    }

    throw new IllegalArgumentException("Failed to find a usable ID field within %s.class".formatted(serializableClass.getName()));
  }

  public static Field[] getFinalFields(Class<?> serializableClass) {
    return Arrays.stream(serializableClass.getDeclaredFields())
      .filter(field -> Modifier.isFinal(field.getModifiers()))
      .peek(field -> field.setAccessible(true))
      .toArray(Field[]::new);
  }

  public static Map<String, Field> getSortedFields(Class<?> serializableClass) {
    Stream<Field> annotatedFieldStream = getAnnotatedObjects(Arrays.asList(serializableClass.getDeclaredFields()), Sorted.class);
    return annotatedFieldStream.filter(field -> {
        if (!Comparable.class.isAssignableFrom(field.getType()) && !field.getType().isPrimitive()) {
          Logger.getGlobal().log(Level.SEVERE, "Field %s does not implement comparable".formatted(field));
          return false;
        }
        return true;
      })
      .collect(
        Collectors.toMap(field -> field.getAnnotation(Sorted.class).value(), field -> field)
      );
  }

  public static Map<String, Field> getIndexes(Class<?> serializableClass) {
    Stream<Field> annotatedFieldStream = getAnnotatedObjects(Arrays.asList(serializableClass.getDeclaredFields()), Indexable.class);
    return annotatedFieldStream
      .filter(field -> !field.isAnnotationPresent(ID.class))
      .collect(
        Collectors.toMap(field -> field.getAnnotation(Indexable.class).value(), field -> field)
      );
  }

  @SuppressWarnings("unchecked")
  private static <T> Stream<T> getAnnotatedObjects(List<? extends AccessibleObject> objects, Class<? extends Annotation> annotationClass) {
    return objects.stream()
      .filter(field -> field.isAnnotationPresent(annotationClass))
      .peek(field -> field.setAccessible(true))
      .map(field -> (T) field);
  }

  public static ByteTranslatable getId(Field field, Object object) {
    try {
      return ByteTranslatable.from(field.get(object));
    } catch (IllegalAccessException exception) {
      throw new RuntimeException("Failed to access ID field", exception);
    }
  }

  public static ByteTranslatable getIndex(Field field, Object object) {
    try {
      return ByteTranslatable.from(field.get(object));
    } catch (IllegalAccessException exception) {
      throw new RuntimeException("Failed to access index field", exception);
    }
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

  private static int compareAsPrimitive(Class<?> type, Object value1, Object value2) {
    if (type == boolean.class)
      return Boolean.compare((boolean) value1, (boolean) value2);
    if (type == byte.class)
      return Byte.compare((byte) value1, (byte) value2);
    if (type == short.class)
      return Short.compare((short) value1, (short) value2);
    if (type == int.class)
      return Integer.compare((int) value1, (int) value2);
    if (type == long.class)
      return Long.compare((long) value1, (long) value2);
    if (type == float.class)
      return Float.compare((float) value1, (float) value2);
    if (type == double.class)
      return Double.compare((double) value1, (double) value2);
    if (type == char.class)
      return Character.compare((char) value1, (char) value2);
    throw new IllegalArgumentException("Unsupported primitive type: " + type.getName());
  }

}
