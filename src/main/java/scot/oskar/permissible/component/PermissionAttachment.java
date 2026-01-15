package scot.oskar.permissible.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

public class PermissionAttachment implements Component<EntityStore> {

  private Set<String> permissions;
  private Set<String> groups;

  public PermissionAttachment(PermissionAttachment other) {
    this.permissions = new HashSet<>(other.permissions);
    this.groups = new HashSet<>(other.groups);
  }

  @Nullable
  @Override
  public Component<EntityStore> clone() {
    return new PermissionAttachment(this);
  }

  public PermissionAttachment() {
    this.permissions = new HashSet<>();
    this.groups = new HashSet<>();
  }

  public PermissionAttachment(Set<String> permissions, Set<String> groups) {
    this.permissions = new HashSet<>(permissions);
    this.groups = new HashSet<>(groups);
  }

  public void addPermission(String permission) {
    permissions.add(permission);
  }

  public void removePermission(String permission) {
    permissions.remove(permission);
  }

  public void addGroup(String group) {
    groups.add(group);
  }

  public void removeGroup(String group) {
    groups.remove(group);
  }

  public Set<String> getGroups() {
    return Set.copyOf(groups);
  }

  public Set<String> getPermissions() {
    return Set.copyOf(permissions);
  }
}
