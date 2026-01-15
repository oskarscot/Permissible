package scot.oskar.permissible.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import java.io.File;

public class ConfigurationFactory {

  public static PluginConfiguration createPluginConfiguration(File pluginConfigurationFile) {
    return ConfigManager.create(PluginConfiguration.class, (it) -> {
      it.configure(opt -> {
        opt.configurer(new YamlSnakeYamlConfigurer(), new SerdesCommons());
        opt.validator(new OkaeriValidator(true));
        opt.bindFile(pluginConfigurationFile);
        opt.errorComments(true);
      });
      it.saveDefaults();
      it.load(true);
    });
  }
}
