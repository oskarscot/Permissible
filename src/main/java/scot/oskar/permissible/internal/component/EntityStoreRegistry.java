package scot.oskar.permissible.internal.component;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;

public class EntityStoreRegistry {

  private static EntityStoreRegistry instance;
  private ComponentRegistryProxy<EntityStore> registry;
  private ComponentType<EntityStore, PermissionAttachment>  permissionAttachmentComponentType;

  private EntityStoreRegistry(ComponentRegistryProxy<EntityStore> storeRegistry) {
    this.registry = storeRegistry;
    instance = this;

    this.registerComponents();
  }

  public static EntityStoreRegistry create(ComponentRegistryProxy<EntityStore> storeRegistry) {
    return new EntityStoreRegistry(storeRegistry);
  }


  private void registerComponents() {
    this.permissionAttachmentComponentType = this.registry.registerComponent(
        PermissionAttachment.class, PermissionAttachment::new);
  }

  @SafeVarargs
  public final void registerSystems(ISystem<EntityStore>... systems) {
    Arrays.stream(systems).forEach(this.registry::registerSystem);
  }

  public static EntityStoreRegistry get() {
    return instance;
  }

  public ComponentType<EntityStore, PermissionAttachment> getPermissionAttachmentComponentType() {
    return permissionAttachmentComponentType;
  }
}
