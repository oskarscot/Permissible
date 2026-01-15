package scot.oskar.permissible;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.zaxxer.hikari.HikariConfig;
import java.io.File;
import java.util.Set;
import javax.annotation.Nonnull;
import scot.oskar.permissible.command.PermissibleCommand;
import scot.oskar.permissible.component.EntityStoreRegistry;
import scot.oskar.permissible.config.ConfigurationFactory;
import scot.oskar.permissible.config.PluginConfiguration;
import scot.oskar.permissible.event.PlayerReadyHandler;
import scot.oskar.permissible.provider.PermissibleProvider;
import scot.oskar.permissible.entity.GroupPermission;
import scot.oskar.permissible.entity.PlayerGroup;
import scot.oskar.permissible.entity.PlayerPermission;
import scot.oskar.permissible.repository.PermissionRepository;
import scot.oskar.permissible.system.PermissionAttachmentSystem;
import scot.oskar.volt.Volt;
import scot.oskar.volt.VoltFactory;

public class PermissiblePlugin extends JavaPlugin {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static PermissiblePlugin instance;

  private Volt volt;
  private PluginConfiguration pluginConfiguration;
  private EntityStoreRegistry componentRegistry;
  private PermissionRepository permissionRepository;

  public PermissiblePlugin(@Nonnull JavaPluginInit init) {
    super(init);
    instance = this;
    this.pluginConfiguration =
        ConfigurationFactory.createPluginConfiguration(
            new File(this.getDataDirectory() + "/config.yml")); // refactor
  }

  public static PermissiblePlugin getInstance() {
    return instance;
  }

  public PermissionRepository getPermissionRepository() {
    return permissionRepository;
  }

  private void setupVolt() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName("org.postgresql.Driver");
    hikariConfig.setJdbcUrl(this.pluginConfiguration.databaseConfig.jdbcUrl);
    hikariConfig.setUsername(this.pluginConfiguration.databaseConfig.username);
    hikariConfig.setPassword(this.pluginConfiguration.databaseConfig.password);
    this.volt = VoltFactory.createVolt(hikariConfig);
    if(!this.volt.testConnection()) {
      LOGGER.atInfo().log("Could not verify the database connection, shutting down...");
      this.shutdown0(true);
    }

    LOGGER.atInfo().log("Registering permission entities...");
    this.volt.registerEntity(PlayerPermission.class);
    this.volt.registerEntity(PlayerGroup.class);
    this.volt.registerEntity(GroupPermission.class);

    this.permissionRepository = new PermissionRepository(this.volt);
    LOGGER.atInfo().log("Permission repository initialized");

    this.initializeDefaultGroups();
  }

  private void initializeDefaultGroups() {
    LOGGER.atInfo().log("Initializing default permission groups...");

    // Create OP group with all permissions
    Set<String> opPermissions = this.permissionRepository.loadGroupPermissions("OP");
    if (opPermissions.isEmpty()) {
      LOGGER.atInfo().log("Creating OP group with wildcard permissions");
      this.permissionRepository.addGroupPermission("OP", "*");
    }

    // Create Default group (no permissions)
    Set<String> defaultPermissions = this.permissionRepository.loadGroupPermissions("Default");
    if (defaultPermissions.isEmpty()) {
      LOGGER.atInfo().log("Creating Default group (no permissions)");
      // Default group intentionally has no permissions
    }

    LOGGER.atInfo().log("Default groups initialized");
  }

  @Override
  protected void setup() {
    this.setupVolt();

    LOGGER.atInfo().log("Replacing permission provider...");
    PermissionsModule permissionsModule = PermissionsModule.get();
    permissionsModule.removeProvider(permissionsModule.getFirstPermissionProvider());
    permissionsModule.addProvider(new PermissibleProvider());
    if (permissionsModule.areProvidersTampered()) {
      LOGGER.atInfo().log("Successfully replaced the standard permissions provider");
      LOGGER.atInfo().log("PermissionProviders: " + permissionsModule.getProviders().stream().map(PermissionProvider::getName).toList());
    } else {
      LOGGER.atInfo().log("Could not replace the standard permissions provider, shutting down...");
      this.shutdown0(true);
    }

    ComponentRegistryProxy<EntityStore> storeRegistry = this.getEntityStoreRegistry();
    this.componentRegistry = EntityStoreRegistry.create(storeRegistry);
    this.componentRegistry.registerSystems(new PermissionAttachmentSystem());

    this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, new PlayerReadyHandler()::onPlayerReady);

    this.getCommandRegistry().registerCommand(new PermissibleCommand());
  }
}
