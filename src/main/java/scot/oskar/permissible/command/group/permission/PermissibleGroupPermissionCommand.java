package scot.oskar.permissible.command.group.permission;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PermissibleGroupPermissionCommand extends AbstractCommandCollection {

  public PermissibleGroupPermissionCommand() {
    super("permission", "Manage group permissions");
    this.addSubCommand(new GroupPermissionAddCommand());
    this.addSubCommand(new GroupPermissionRemoveCommand());
  }
}
