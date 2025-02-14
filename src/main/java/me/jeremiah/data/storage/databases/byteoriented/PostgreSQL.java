package me.jeremiah.data.storage.databases.byteoriented;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import org.jetbrains.annotations.NotNull;

public final class PostgreSQL<ENTRY> extends AbstractSQLDatabase<ENTRY> {

  private static final SQLStatementHandler HANDLER = new SQLStatementHandler(
    "CREATE TABLE IF NOT EXISTS entries(entry_id VARBINARY PRIMARY KEY, entry_data VARBINARY);",
    "SELECT COUNT(*) FROM entries;",
    "SELECT * FROM entries;",
    "INSERT INTO entries(entry_id, entry_data) VALUES(?, ?) ON CONFLICT(entry_id) DO UPDATE SET entry_data = EXCLUDED.entry_data;"
  );

  public PostgreSQL(@NotNull DatabaseInfo info, @NotNull Class<ENTRY> entryClass) {
    super(org.postgresql.Driver.class, HANDLER, info, entryClass);
  }

  @Override
  protected void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:postgresql://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

}
