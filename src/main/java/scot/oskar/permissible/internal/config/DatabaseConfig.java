package scot.oskar.permissible.internal.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class DatabaseConfig extends OkaeriConfig {

  @Comment("")
  @Comment("Currently only PostgreSQL is supported")
  @Comment("JDBC url follows the following format: jdbc:postgresql://<host>:<port>/<database_name>")
  public String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";

  @Comment("")
  @Comment("Username to use to connect to the database")
  public String username = "postgres";

  @Comment("Password to use with database authentication")
  public String password = "test";

}
