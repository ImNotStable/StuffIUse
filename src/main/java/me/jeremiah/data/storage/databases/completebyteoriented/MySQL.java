package me.jeremiah.data.storage.databases.completebyteoriented;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import org.jetbrains.annotations.NotNull;

public final class MySQL<T> extends AbstractSQLDatabase<T> {

  private static final SQLStatementHandler HANDLER = new SQLStatementHandler(
    "CREATE TABLE IF NOT EXISTS entries(entry VARBINARY PRIMARY KEY);",
    "SELECT COUNT(*) FROM entries;",
    "SELECT * FROM entries;",
    "INSERT INTO entries(entry) VALUES(?) ON DUPLICATE KEY UPDATE entry = VALUES(entry);"
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
