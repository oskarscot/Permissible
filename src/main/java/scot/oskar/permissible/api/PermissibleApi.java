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
}
