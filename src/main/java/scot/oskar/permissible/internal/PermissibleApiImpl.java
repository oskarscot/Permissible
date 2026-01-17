package scot.oskar.permissible.internal;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import scot.oskar.permissible.api.GroupMetadataUpdate;
import scot.oskar.permissible.api.PermissibleApi;
import scot.oskar.permissible.api.PermissibleGroupInfo;
import scot.oskar.permissible.internal.component.EntityStoreRegistry;
import scot.oskar.permissible.internal.component.PermissionAttachment;
import scot.oskar.permissible.internal.entity.GroupMetadata;
import scot.oskar.permissible.internal.repository.PermissionRepository;

public class PermissibleApiImpl implements PermissibleApi {
  private final PermissionRepository repository;

  public PermissibleApiImpl(PermissionRepository repository) {
    this.repository = repository;
  }

  @Override
  public Set<String> getPlayerGroups(UUID playerUuid) {
    return repository.loadPlayerGroups(playerUuid);
  }

  @Override
  public Set<String> getPlayerPermissions(UUID playerUuid) {
    return repository.loadPlayerPermissions(playerUuid);
  }

  @Override
  public Optional<String> getPrimaryGroup(UUID playerUuid) {
    return Optional.ofNullable(repository.getPrimaryGroupForPlayer(playerUuid));
  }

  @Override
  public Optional<PermissibleGroupInfo> getGroupInfo(String groupName) {
    GroupMetadata metadata = repository.getGroupMetadata(groupName);
    if (metadata == null) {
      metadata = repository.ensureGroupMetadata(groupName);
    }
    if (metadata == null) {
      return Optional.empty();
    }
    return Optional.of(toGroupInfo(metadata));
  }

  @Override
  public Map<String, PermissibleGroupInfo> getAllGroupInfo() {
    return repository.getAllGroups().stream()
        .map(repository::ensureGroupMetadata)
        .filter(metadata -> metadata != null)
        .collect(Collectors.toMap(GroupMetadata::getGroupName, this::toGroupInfo));
  }

  @Override
  public void setGroupMetadata(String groupName, GroupMetadataUpdate update) {
    repository.updateGroupMetadata(
        groupName,
        update.weight(),
        update.prefix(),
        update.suffix(),
        update.displayName());
  }

  @Override
  public void addPlayerPermission(UUID playerUuid, String permission) {
    applyPermissionUpdate(
        playerUuid,
        attachment -> {
          Set<String> newPermissions = new HashSet<>(attachment.getPermissions());
          newPermissions.add(permission);
          return new PermissionAttachment(newPermissions, attachment.getGroups());
        },
        () -> repository.addPlayerPermission(playerUuid, permission));
  }

  @Override
  public void removePlayerPermission(UUID playerUuid, String permission) {
    applyPermissionUpdate(
        playerUuid,
        attachment -> {
          Set<String> newPermissions = new HashSet<>(attachment.getPermissions());
          newPermissions.remove(permission);
          return new PermissionAttachment(newPermissions, attachment.getGroups());
        },
        () -> repository.removePlayerPermission(playerUuid, permission));
  }

  @Override
  public void setPlayerPermissions(UUID playerUuid, Set<String> permissions) {
    applyPermissionUpdate(
        playerUuid,
        attachment -> new PermissionAttachment(new HashSet<>(permissions), attachment.getGroups()),
        () -> repository.savePlayerPermissions(playerUuid, permissions));
  }

  @Override
  public void addPlayerToGroup(UUID playerUuid, String groupName) {
    applyPermissionUpdate(
        playerUuid,
        attachment -> {
          Set<String> newGroups = new HashSet<>(attachment.getGroups());
          newGroups.add(groupName);
          return new PermissionAttachment(attachment.getPermissions(), newGroups);
        },
        () -> repository.addPlayerToGroup(playerUuid, groupName));
  }

  @Override
  public void removePlayerFromGroup(UUID playerUuid, String groupName) {
    applyPermissionUpdate(
        playerUuid,
        attachment -> {
          Set<String> newGroups = new HashSet<>(attachment.getGroups());
          newGroups.remove(groupName);
          return new PermissionAttachment(attachment.getPermissions(), newGroups);
        },
        () -> repository.removePlayerFromGroup(playerUuid, groupName));
  }

  @Override
  public void setPlayerGroups(UUID playerUuid, Set<String> groups) {
    applyPermissionUpdate(
        playerUuid,
        attachment -> new PermissionAttachment(attachment.getPermissions(), new HashSet<>(groups)),
        () -> repository.savePlayerGroups(playerUuid, groups));
  }

  private void applyPermissionUpdate(
      UUID playerUuid,
      Function<PermissionAttachment, PermissionAttachment> updater,
      Runnable offlineUpdate) {
    PlayerRef playerRef = Universe.get().getPlayer(playerUuid);
    if (playerRef == null) {
      offlineUpdate.run();
      return;
    }
    Store<EntityStore> store = playerRef.getReference().getStore();
    Ref<EntityStore> ref = playerRef.getReference();
    PermissionAttachment attachment = store.getComponent(
        ref,
        EntityStoreRegistry.get().getPermissionAttachmentComponentType());
    if (attachment == null) {
      offlineUpdate.run();
      return;
    }
    PermissionAttachment updated = updater.apply(attachment);
    store.replaceComponent(
        ref,
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        updated);
  }

  private PermissibleGroupInfo toGroupInfo(GroupMetadata metadata) {
    return new PermissibleGroupInfo(
        metadata.getGroupName(),
        metadata.getWeight(),
        metadata.getPrefix(),
        metadata.getSuffix(),
        metadata.getDisplayName());
  }
}
