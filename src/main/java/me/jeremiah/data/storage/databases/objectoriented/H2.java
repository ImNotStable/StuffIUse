package me.jeremiah.data.storage.databases.objectoriented;

import com.zaxxer.hikari.HikariConfig;
import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.SQLStatementHandler;
import me.jeremiah.data.storage.databases.byteoriented.AbstractSQLDatabase;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public final class H2<T extends Serializable> extends AbstractSQLDatabase<T> {

  private static final SQLStatementHandler HANDLER = new SQLStatementHandler(
    "CREATE TABLE IF NOT EXISTS entries(entry VARBINARY PRIMARY KEY);",
    "SELECT COUNT(*) FROM entries;",
    "SELECT * FROM entries;",
    "INSERT INTO entries(entry) VALUES(?) ON DUPLICATE KEY UPDATE entry = VALUES(entry);"
  );

  public H2(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(org.h2.Driver.class, HANDLER, info, entryClass);
  }

  @Override
  protected void processConfig(@NotNull HikariConfig hikariConfig, @NotNull DatabaseInfo databaseInfo) {
    hikariConfig.setJdbcUrl("jdbc:h2:./%s;MODE=MariaDB;DATABASE_TO_UPPER=FALSE".formatted(databaseInfo.getName()));
  }

}
