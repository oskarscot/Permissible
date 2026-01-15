package scot.oskar.permissible.command.group;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import scot.oskar.permissible.command.group.permission.PermissibleGroupPermissionCommand;

public class PermissibleGroupCommand extends AbstractCommandCollection {

  public PermissibleGroupCommand() {
    super("group", "Manage permission groups.");
    this.addSubCommand(new GroupAddCommand());
    this.addSubCommand(new GroupRemoveCommand());
    this.addSubCommand(new PermissibleGroupPermissionCommand());
  }
}
