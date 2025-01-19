package me.jeremiah.data.storage.databases;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.data.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import org.jetbrains.annotations.NotNull;

public final class SQLite<T> extends AbstractSQLDatabase<T> {

  private static final SQLStatementHandler HANDLER = new SQLStatementHandler(
    "CREATE TABLE IF NOT EXISTS entries(entry_id VARBINARY PRIMARY KEY, entry_data VARBINARY);",
    "SELECT COUNT(*) FROM entries;",
    "SELECT * FROM entries;",
    "INSERT INTO entries(entry_id, entry_data) VALUES(?, ?) ON CONFLICT(entry_id) DO UPDATE SET entry_data = EXCLUDED.entry_data;"
  );

  public SQLite(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(org.sqlite.JDBC.class, HANDLER, info, entryClass);
  }

  @Override
  protected void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:sqlite:./%s.sqlite".formatted(databaseInfo.getName()));
  }

}
