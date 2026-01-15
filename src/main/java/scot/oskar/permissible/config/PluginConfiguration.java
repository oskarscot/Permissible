package scot.oskar.permissible.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

@Header("Permissible v0.0.1")
@Header("For more details visit https://oskar.scot")
@Header(" ")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class PluginConfiguration extends OkaeriConfig {

  public DatabaseConfig databaseConfig = new DatabaseConfig();

}
