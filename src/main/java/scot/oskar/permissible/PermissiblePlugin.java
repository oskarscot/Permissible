package scot.oskar.permissible;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.zaxxer.hikari.HikariConfig;
import java.awt.Color;
import java.io.File;
import java.util.Set;
import javax.annotation.Nonnull;
import scot.oskar.permissible.api.PermissibleApi;
import scot.oskar.permissible.internal.PermissibleApiImpl;
import scot.oskar.permissible.internal.PermissibleCommand;
import scot.oskar.permissible.internal.component.EntityStoreRegistry;
import scot.oskar.permissible.internal.config.ConfigurationFactory;
import scot.oskar.permissible.internal.config.PluginConfiguration;
import scot.oskar.permissible.internal.event.PlayerReadyHandler;
import scot.oskar.permissible.internal.provider.PermissibleProvider;
import scot.oskar.permissible.internal.entity.GroupMetadata;
import scot.oskar.permissible.internal.entity.GroupPermission;
import scot.oskar.permissible.internal.entity.PlayerGroup;
import scot.oskar.permissible.internal.entity.PlayerPermission;
import scot.oskar.permissible.internal.repository.PermissionRepository;
import scot.oskar.permissible.internal.system.PermissionAttachmentSystem;
import scot.oskar.volt.Volt;
import scot.oskar.volt.VoltFactory;

public class PermissiblePlugin extends JavaPlugin {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
  private static PermissiblePlugin instance;

  private Volt volt;
  private PluginConfiguration pluginConfiguration;
  private EntityStoreRegistry componentRegistry;
  private PermissionRepository permissionRepository;
  private PermissibleApi api;

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

  public PermissibleApi getApi() {
    return api;
  }

  public PermissionRepository getPermissionRepository() {
    return permissionRepository;
  }

  public scot.oskar.permissible.internal.repository.PermissionRepository getInternalPermissionRepository() {
    return permissionRepository;
  }

  public PluginConfiguration getPluginConfiguration() {
    return pluginConfiguration;
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
    this.volt.registerEntity(GroupMetadata.class);

    this.permissionRepository = new PermissionRepository(this.volt);
    this.api = new PermissibleApiImpl(this.permissionRepository);
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
    this.permissionRepository.ensureGroupMetadata("OP");

    // Create Default group (no permissions)
    Set<String> defaultPermissions = this.permissionRepository.loadGroupPermissions("Default");
    if (defaultPermissions.isEmpty()) {
      LOGGER.atInfo().log("Creating Default group (no permissions)");
      // Default group intentionally has no permissions
    }
    this.permissionRepository.ensureGroupMetadata("Default");

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

    LOGGER.atInfo().log("Permissible UI available via /permissible openui");

  }
}
