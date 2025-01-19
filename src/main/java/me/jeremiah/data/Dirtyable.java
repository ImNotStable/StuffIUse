package me.jeremiah.data;

public interface Dirtyable {

  boolean isDirty();

  void markClean();

}
