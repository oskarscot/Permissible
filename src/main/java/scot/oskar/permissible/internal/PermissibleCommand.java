package scot.oskar.permissible.internal;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import scot.oskar.permissible.internal.group.PermissibleGroupCommand;
import scot.oskar.permissible.internal.list.PermissibleListCommand;
import scot.oskar.permissible.internal.user.PermissibleUserCommand;

public class PermissibleCommand extends AbstractCommandCollection {

  public PermissibleCommand() {
    super("permissible", "Permission management system.");
    this.addSubCommand(new PermissibleGroupCommand());
    this.addSubCommand(new PermissibleUserCommand());
    this.addSubCommand(new PermissibleListCommand());
    this.addSubCommand(new OpenUiCommand());
  }
}
