package scot.oskar.permissible.internal.list;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PermissibleListCommand extends AbstractCommandCollection {

  public PermissibleListCommand() {
    super("list", "List permission information");
    this.addSubCommand(new ListGroupsCommand());
    this.addSubCommand(new ListPlayersCommand());
  }
}
