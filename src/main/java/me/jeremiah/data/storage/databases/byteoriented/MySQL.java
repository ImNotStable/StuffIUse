package me.jeremiah.data.storage.databases.byteoriented;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import org.jetbrains.annotations.NotNull;

public final class MySQL<T> extends AbstractSQLDatabase<T> {

  private static final SQLStatementHandler HANDLER = new SQLStatementHandler(
    "CREATE TABLE IF NOT EXISTS entries(entry_id VARBINARY PRIMARY KEY, entry_data VARBINARY);",
    "SELECT COUNT(*) FROM entries;",
    "SELECT * FROM entries;",
    "INSERT INTO entries(entry_id, entry_data) VALUES(?, ?) ON DUPLICATE KEY UPDATE entry_data = VALUES(entry_data);"
  );

  public MySQL(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(com.mysql.cj.jdbc.Driver.class, HANDLER, info, entryClass);
  }

  @Override
  protected void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:mysql://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

}
