package me.jeremiah.data;

import java.util.concurrent.TimeUnit;

public class DatabaseInfo {

  private final String address;
  private final int port;

  private final String name;

  private final String username;
  private final String password;

  private long autoSaveInterval = 300;
  private TimeUnit autoSaveTimeUnit = TimeUnit.SECONDS;

  public DatabaseInfo(String databaseAddress, int databasePort, String name, String username, String password) {
    this.address = databaseAddress;
    this.port = databasePort;
    this.name = name;
    this.username = username;
    this.password = password;
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  public String getUrl() {
    return "%s:%d".formatted(address, port);
  }

  public String getName() {
    return name;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setAutoSaveInterval(long autoSaveInterval) {
    this.autoSaveInterval = autoSaveInterval;
  }

  public long getAutoSaveInterval() {
    return autoSaveInterval;
  }

  public void setAutoSaveTimeUnit(TimeUnit autoSaveTimeUnit) {
    this.autoSaveTimeUnit = autoSaveTimeUnit;
  }

  public TimeUnit getAutoSaveTimeUnit() {
    return autoSaveTimeUnit;
  }

}
