package scot.oskar.permissible.internal.provider;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.internal.repository.PermissionRepository;

public class PermissibleProvider implements PermissionProvider {

  private final Map<UUID, Set<String>> userPermissionsCache = new ConcurrentHashMap<>();
  private final Map<UUID, Set<String>> userGroupsCache = new ConcurrentHashMap<>();

  @Nonnull
  @Override
  public String getName() {
    return "PermissibleProvider";
  }

  public void updateCache(UUID uuid, Set<String> permissions, Set<String> groups) {
    userPermissionsCache.put(uuid, Set.copyOf(permissions));
    userGroupsCache.put(uuid, Set.copyOf(groups));
  }

  public void removeFromCache(UUID uuid) {
    userPermissionsCache.remove(uuid);
    userGroupsCache.remove(uuid);
  }

  @Override
  public void addUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
  }

  @Override
  public void removeUserPermissions(@Nonnull UUID uuid, @Nonnull Set<String> permissions) {
  }

  @Override
  public Set<String> getUserPermissions(@Nonnull UUID uuid) {
    Set<String> permissions = userPermissionsCache.getOrDefault(uuid, Set.of());
    return permissions;
  }

  @Override
  public void addGroupPermissions(@Nonnull String group, @Nonnull Set<String> permissions) {
  }

  @Override
  public void removeGroupPermissions(@Nonnull String group, @Nonnull Set<String> permissions) {

  }

  @Override
  public Set<String> getGroupPermissions(@Nonnull String group) {
    PermissionRepository repository = PermissiblePlugin.getInstance().getInternalPermissionRepository();
    Set<String> permissions = repository.loadGroupPermissions(group);
    return permissions;
  }

  @Override
  public void addUserToGroup(@Nonnull UUID uuid, @Nonnull String group) {
    // Hytale's systems (like gamemode changes) try to add players to groups
    // We ignore these calls to prevent Hytale from overriding permissions
  }

  @Override
  public void removeUserFromGroup(@Nonnull UUID uuid, @Nonnull String group) {
  }

  @Override
  public Set<String> getGroupsForUser(@Nonnull UUID uuid) {
    Set<String> groups = userGroupsCache.getOrDefault(uuid, Set.of());
    return groups;
  }
}