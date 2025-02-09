package me.jeremiah.data.storage.databases.components.indexing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class Index {

  private final String id;
  private final boolean isFinal;
  private final Field field;

  public Index(String id, Field field) {
    this.id = id;
    this.isFinal = Modifier.isFinal(field.getModifiers());
    this.field = field;
  }

  public String getId() {
    return id;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public Field getField() {
    return field;
  }

}
