package scot.oskar.permissible.internal.user;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PermissibleUserCommand extends AbstractCommandCollection {

  public PermissibleUserCommand() {
    super("user", "Manage user permissions");
    this.addSubCommand(new UserPermissionAddCommand());
    this.addSubCommand(new UserPermissionRemoveCommand());
  }
}
