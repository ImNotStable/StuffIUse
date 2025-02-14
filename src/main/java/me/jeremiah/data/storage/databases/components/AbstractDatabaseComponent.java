package me.jeremiah.data.storage.databases.components;

import java.io.Closeable;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractDatabaseComponent<ENTRY> implements Closeable {

  private final ScheduledExecutorService scheduler;

  protected AbstractDatabaseComponent(ScheduledExecutorService scheduler) {
    this.scheduler = scheduler;
  }

  protected ScheduledExecutorService getScheduler() {
    return scheduler;
  }

  protected abstract void setup(int initialCapacity);

  protected abstract void update();

  protected abstract void add(ENTRY entry);

}
