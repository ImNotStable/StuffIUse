package me.jeremiah.data.storage.databases.objectoriented;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public final class MariaDB<ENTRY extends Serializable> extends AbstractSQLDatabase<ENTRY> {

  private static final SQLStatementHandler HANDLER = new SQLStatementHandler(
    "CREATE TABLE IF NOT EXISTS entries(entry VARBINARY PRIMARY KEY);",
    "SELECT COUNT(*) FROM entries;",
    "SELECT * FROM entries;",
    "INSERT INTO entries(entry) VALUES(?) ON DUPLICATE KEY UPDATE entry = VALUES(entry);"
  );

  public MariaDB(@NotNull DatabaseInfo info, @NotNull Class<ENTRY> entryClass) {
    super(org.mariadb.jdbc.Driver.class, HANDLER, info, entryClass);
  }

  @Override
  protected void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:mariadb://%s/%s".formatted(databaseInfo.getUrl(), databaseInfo.getName()));
    hikariConfig.setUsername(databaseInfo.getUsername());
    hikariConfig.setPassword(databaseInfo.getPassword());
  }

}
