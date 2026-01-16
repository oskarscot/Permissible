package scot.oskar.permissible.internal.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.internal.component.EntityStoreRegistry;
import scot.oskar.permissible.internal.component.PermissionAttachment;
import scot.oskar.permissible.internal.provider.PermissibleProvider;
import scot.oskar.permissible.internal.repository.PermissionRepository;

import java.util.UUID;

public class PermissionAttachmentSystem extends RefChangeSystem<EntityStore, PermissionAttachment> {

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  @Nonnull
  @Override
  public ComponentType<EntityStore, PermissionAttachment> componentType() {
    return EntityStoreRegistry.get().getPermissionAttachmentComponentType();
  }

  private PermissibleProvider getProvider() {
    for (PermissionProvider provider : PermissionsModule.get().getProviders()) {
      if (provider instanceof PermissibleProvider) {
        return (PermissibleProvider) provider;
      }
    }
    LOGGER.atWarning().log("PermissibleProvider not found in PermissionsModule!");
    return null;
  }

  @Override
  public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull PermissionAttachment permissionAttachment, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    UUIDComponent component = store.getComponent(ref, UUIDComponent.getComponentType());
    UUID playerUuid = component.getUuid();

    PermissibleProvider provider = getProvider();
    if (provider != null) {
      provider.updateCache(playerUuid, permissionAttachment.getPermissions(), permissionAttachment.getGroups());
    }
  }

  @Override
  public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable PermissionAttachment oldAttachment, @Nonnull PermissionAttachment newAttachment, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    UUIDComponent component = store.getComponent(ref, UUIDComponent.getComponentType());
    UUID playerUuid = component.getUuid();

    PermissionRepository repository = PermissiblePlugin.getInstance().getInternalPermissionRepository();
    repository.savePlayerPermissions(playerUuid, newAttachment.getPermissions());
    repository.savePlayerGroups(playerUuid, newAttachment.getGroups());

    PermissibleProvider provider = getProvider();
    if (provider != null) {
      provider.updateCache(playerUuid, newAttachment.getPermissions(), newAttachment.getGroups());
    }
  }

  @Override
  public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull PermissionAttachment permissionAttachment, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
    UUIDComponent component = store.getComponent(ref, UUIDComponent.getComponentType());
    UUID playerUuid = component.getUuid();

    PermissibleProvider provider = getProvider();
    if (provider != null) {
      provider.removeFromCache(playerUuid);
    }
  }

  @Nullable
  @Override
  public Query<EntityStore> getQuery() {
    return EntityStoreRegistry.get().getPermissionAttachmentComponentType();
  }
}
