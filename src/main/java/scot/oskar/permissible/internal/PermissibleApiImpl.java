package scot.oskar.permissible.internal;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import scot.oskar.permissible.api.GroupMetadataUpdate;
import scot.oskar.permissible.api.PermissibleApi;
import scot.oskar.permissible.api.PermissibleGroupInfo;
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

  private PermissibleGroupInfo toGroupInfo(GroupMetadata metadata) {
    return new PermissibleGroupInfo(
        metadata.getGroupName(),
        metadata.getWeight(),
        metadata.getPrefix(),
        metadata.getSuffix(),
        metadata.getDisplayName());
  }
}
