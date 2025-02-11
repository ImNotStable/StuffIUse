package me.jeremiah.data.storage.databases.components.indexing;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;

public final class Index {

  private final String id;
  private final boolean isFinal;
  private final MethodHandle field;

  public Index(String id, MethodHandle field) {
    this.id = id;
    this.isFinal = Modifier.isFinal(field.type().returnType().getModifiers());
    this.field = field;
  }

  public String getId() {
    return id;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public MethodHandle getField() {
    return field;
  }

}
