package scot.oskar.permissible.command.list;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.repository.PermissionRepository;

import java.awt.Color;
import java.util.Set;
import javax.annotation.Nonnull;

public class ListGroupsCommand extends CommandBase {

  public ListGroupsCommand() {
    super("groups", "Lists all permission groups.");
    this.requirePermission("permissible.list.groups");
  }

  @Override
  protected void executeSync(@Nonnull CommandContext context) {
    PermissionRepository repository = PermissiblePlugin.getInstance().getPermissionRepository();
    Set<String> groups = repository.getAllGroups();

    if (groups.isEmpty()) {
      context.sendMessage(Message.raw("No groups found.").color(Color.YELLOW));
      return;
    }

    context.sendMessage(Message.raw("Permission Groups: ").color(Color.YELLOW));
    for (String group : groups) {
      Set<String> permissions = repository.loadGroupPermissions(group);
      context.sendMessage(Message.join(
          Message.raw("  ").color(Color.GRAY),
          Message.raw(group).color(Color.GREEN),
          Message.raw(" (").color(Color.GRAY),
          Message.raw(permissions.size() + " permissions").color(Color.GRAY),
          Message.raw(")").color(Color.GRAY)
      ));
    }
  }
}
