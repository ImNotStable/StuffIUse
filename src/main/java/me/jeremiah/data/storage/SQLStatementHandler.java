package me.jeremiah.data.storage;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record SQLStatementHandler(@NotNull String createTableStatement,
                                  @NotNull String entryCountLookupStatement,
                                  @NotNull String loadEntryStatement,
                                  @NotNull String saveEntryStatement) {

  public void executeCreateTableStatement(@NotNull Connection connection) throws SQLException {
    connection.createStatement().execute(createTableStatement);
  }

  public int handleEntryCountLookup(@NotNull Connection connection) throws SQLException {
    try (ResultSet resultSet = connection.createStatement().executeQuery(entryCountLookupStatement)) {
      return resultSet.next() ? resultSet.getInt(1) : 1024;
    }
  }

  public ResultSet handleLoadEntryStatement(@NotNull Connection connection) throws SQLException {
    return connection.prepareStatement(loadEntryStatement).executeQuery();
  }

  public PreparedStatement getSaveEntryStatement(@NotNull Connection connection) throws SQLException {
    return connection.prepareStatement(saveEntryStatement);
  }

}
