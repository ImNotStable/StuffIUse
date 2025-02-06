package me.jeremiah.data.storage.databases;

import java.io.Closeable;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractDatabaseComponent<T> implements Closeable {

  private final ScheduledExecutorService scheduler;

  protected AbstractDatabaseComponent(ScheduledExecutorService scheduler) {
    this.scheduler = scheduler;
  }

  protected ScheduledExecutorService getScheduler() {
    return scheduler;
  }

  abstract void setup(int initialCapacity);

  abstract void update();

  abstract void add(T entry);

}
