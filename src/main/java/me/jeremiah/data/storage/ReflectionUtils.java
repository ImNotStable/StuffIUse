package me.jeremiah.data.storage;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.databases.components.indexing.Index;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ReflectionUtils {

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

  public static MethodHandle wrap(Method method) {
    try {
      return LOOKUP.unreflect(method);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static MethodHandle wrap(Field field) {
    try {
      return LOOKUP.unreflectGetter(field);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T serialize(MethodHandle serializeMethod, Object object) {
    try {
      return (T) serializeMethod.bindTo(object).invoke();
    } catch (Throwable exception) {
      throw new RuntimeException("Failed to serialize object", exception);
    }
  }

  public static MethodHandle getSerializeMethod(Class<?> serializerClass) {
    Stream<Method> methodStream = getAnnotatedObjects(Arrays.asList(serializerClass.getDeclaredMethods()), Serializer.class);
    List<Method> methods = methodStream.toList();

    if (!methods.isEmpty()) {
      if (methods.size() > 1)
        Logger.getGlobal().warning("Multiple methods annotated with @Serializer found in %s.class, using first one.".formatted(serializerClass.getName()));
      return wrap(methods.getFirst());
    }

    throw new IllegalArgumentException("Failed to find a method annotated with @Serializer within %s.class".formatted(serializerClass.getName()));
  }

  @SuppressWarnings("unchecked")
  public static <T> T deserialize(MethodHandle deserializeMethod, Object arg) {
    try {
      return (T) deserializeMethod.invoke(arg);
    } catch (Throwable exception) {
      throw new RuntimeException("Failed to deserialize object", exception);
    }
  }

  public static MethodHandle getDeserializeMethod(Class<?> deserializerClass) {
    Stream<Method> methodStream = getAnnotatedObjects(Arrays.asList(deserializerClass.getDeclaredMethods()), Deserializer.class);
    List<Method> methods = methodStream.toList();

    if (!methods.isEmpty()) {
      if (methods.size() > 1)
        Logger.getGlobal().warning("Multiple methods annotated with @Deserializer found in %s.class, using first one.".formatted(deserializerClass.getName()));
      return wrap(methods.getFirst());
    }

    throw new IllegalArgumentException("Failed to find a method annotated with @Deserializer within %s.class".formatted(deserializerClass.getName()));
  }

  public static ByteTranslatable getIndex(MethodHandle field, Object object) {
    try {
      return ByteTranslatable.from(field.bindTo(object).invoke());
    } catch (Throwable exception) {
      throw new RuntimeException("Failed to access index field", exception);
    }
  }

  public static List<Index> getIndexes(Class<?> serializableClass) {
    Stream<Field> annotatedFieldStream = getAnnotatedObjects(Arrays.asList(serializableClass.getDeclaredFields()), Indexable.class);
    return annotatedFieldStream
      .map(field -> new Index(field.getAnnotation(Indexable.class).id(), wrap(field)))
      .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  public static <T> int compareSortedFields(@NotNull MethodHandle field, @NotNull T sorted1, @NotNull T sorted2) {
    Object value1, value2;
    try {
      value1 = field.bindTo(sorted1).invoke();
      value2 = field.bindTo(sorted2).invoke();
    } catch (Throwable exception) {
      throw new RuntimeException("Failed to access field", exception);
    }

    if (value1 == null && value2 == null)
      return 0;
    if (value1 == null)
      return -1;
    if (value2 == null)
      return 1;

    Class<?> type = field.type().returnType();

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

  public static Map<String, MethodHandle> getSortedFields(Class<?> serializableClass) {
    Stream<Field> annotatedFieldStream = getAnnotatedObjects(Arrays.asList(serializableClass.getDeclaredFields()), Sorted.class);
    return annotatedFieldStream.filter(field -> {
        if (!Comparable.class.isAssignableFrom(field.getType()) && !field.getType().isPrimitive()) {
          Logger.getGlobal().severe("Field %s does not implement comparable".formatted(field));
          return false;
        }
        return true;
      })
      .collect(
        Collectors.toMap(field -> field.getAnnotation(Sorted.class).value(), ReflectionUtils::wrap)
      );
  }

  @SuppressWarnings("unchecked")
  private static <T> Stream<T> getAnnotatedObjects(List<? extends AccessibleObject> objects, Class<? extends Annotation> annotationClass) {
    return objects.stream()
      .filter(field -> field.isAnnotationPresent(annotationClass))
      .peek(field -> field.setAccessible(true))
      .map(field -> (T) field);
  }

}
