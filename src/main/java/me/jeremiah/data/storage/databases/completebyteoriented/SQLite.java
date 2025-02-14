package me.jeremiah.data.storage.databases.completebyteoriented;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import org.jetbrains.annotations.NotNull;

public final class SQLite<ENTRY> extends AbstractSQLDatabase<ENTRY> {

  private static final SQLStatementHandler HANDLER = new SQLStatementHandler(
    "CREATE TABLE IF NOT EXISTS entries(entry VARBINARY PRIMARY KEY);",
    "SELECT COUNT(*) FROM entries;",
    "SELECT * FROM entries;",
    "INSERT INTO entries(entry) VALUES(?) ON CONFLICT(entry) DO UPDATE SET entry = EXCLUDED.entry;"
  );

  public SQLite(@NotNull DatabaseInfo info, @NotNull Class<ENTRY> entryClass) {
    super(org.sqlite.JDBC.class, HANDLER, info, entryClass);
  }

  @Override
  protected void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:sqlite:./%s.sqlite".formatted(databaseInfo.getName()));
  }

}
