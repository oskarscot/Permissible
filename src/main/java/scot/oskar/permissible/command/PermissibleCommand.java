package scot.oskar.permissible.command;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import scot.oskar.permissible.command.group.PermissibleGroupCommand;
import scot.oskar.permissible.command.list.PermissibleListCommand;
import scot.oskar.permissible.command.user.PermissibleUserCommand;

public class PermissibleCommand extends AbstractCommandCollection {

  public PermissibleCommand() {
    super("permissible", "Permission management system.");
    this.addSubCommand(new PermissibleGroupCommand());
    this.addSubCommand(new PermissibleUserCommand());
    this.addSubCommand(new PermissibleListCommand());
  }
}
