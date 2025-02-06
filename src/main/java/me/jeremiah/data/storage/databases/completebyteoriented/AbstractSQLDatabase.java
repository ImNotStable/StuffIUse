package me.jeremiah.data.storage.databases.completebyteoriented;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractSQLDatabase<T> extends Database<T> {

  private final SQLStatementHandler statementHandler;

  private final HikariDataSource dataSource;

  protected AbstractSQLDatabase(@NotNull Class<? extends Driver> driver, SQLStatementHandler statementHandler, @NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
    if (DriverManager.drivers().noneMatch(driver::isInstance))
      try {
        DriverManager.registerDriver(driver.getDeclaredConstructor().newInstance());
      } catch (Exception exception) {
        throw new RuntimeException("Failed to register SQL driver: " + driver.getName(), exception);
      }
    this.statementHandler = statementHandler;

    HikariConfig hikariConfig = new HikariConfig();

    processConfig(hikariConfig, info);

    dataSource = new HikariDataSource(hikariConfig);

    setup();
  }

  protected abstract void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo);

  @Override
  protected int lookupEntryCount() {
    try (Connection connection = dataSource.getConnection()) {
      return statementHandler.handleEntryCountLookup(connection);
    } catch (SQLException exception) {
      throw new RuntimeException("Failed to lookup entry count from SQL database.", exception);
    }
  }

  @Override
  protected void setup() {
    try (Connection connection = dataSource.getConnection()) {
      statementHandler.executeCreateTableStatement(connection);
    } catch (SQLException exception) {
      throw new RuntimeException("Failed to setup SQL database.", exception);
    }
    super.setup();
  }

  @Override
  protected Collection<ByteTranslatable> getData() {
    try (Connection connection = dataSource.getConnection();
         ResultSet rawEntries = statementHandler.handleLoadEntryStatement(connection)) {
      Collection<ByteTranslatable> data = new HashSet<>();

      while (rawEntries.next()) {
        final ByteTranslatable bytes = ByteTranslatable.fromByteArray(rawEntries.getBytes("entry"));
        data.add(bytes);
      }

      return data;
    } catch (SQLException exception) {
      throw new RuntimeException("Failed to load data from SQL database.", exception);
    }
  }

  @Override
  protected void saveData(Collection<ByteTranslatable> data) {
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try (PreparedStatement saveStatement = statementHandler.getSaveEntryStatement(connection)) {

        for (ByteTranslatable entry : data) {
          saveStatement.setBytes(1, entry.bytes());
          saveStatement.addBatch();
        }

        saveStatement.executeBatch();
        connection.commit();
      } catch (SQLException exception) {
        connection.rollback();
        throw exception;
      }
    } catch (SQLException exception) {
      throw new RuntimeException("Failed to save data to SQL database.", exception);
    }
  }

  @Override
  public void close() {
    super.close();
    dataSource.close();
  }

}
