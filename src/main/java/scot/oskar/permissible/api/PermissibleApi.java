package scot.oskar.permissible.api;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PermissibleApi {
  Set<String> getPlayerGroups(UUID playerUuid);

  Set<String> getPlayerPermissions(UUID playerUuid);

  Optional<String> getPrimaryGroup(UUID playerUuid);

  Optional<PermissibleGroupInfo> getGroupInfo(String groupName);

  Map<String, PermissibleGroupInfo> getAllGroupInfo();

  void setGroupMetadata(String groupName, GroupMetadataUpdate update);

  void addPlayerPermission(UUID playerUuid, String permission);

  void removePlayerPermission(UUID playerUuid, String permission);

  void setPlayerPermissions(UUID playerUuid, Set<String> permissions);

  void addPlayerToGroup(UUID playerUuid, String groupName);

  void removePlayerFromGroup(UUID playerUuid, String groupName);

  void setPlayerGroups(UUID playerUuid, Set<String> groups);
}
