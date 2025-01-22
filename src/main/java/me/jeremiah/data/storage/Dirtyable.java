package me.jeremiah.data.storage;

public interface Dirtyable {

  boolean isDirty();

  void markClean();

}
