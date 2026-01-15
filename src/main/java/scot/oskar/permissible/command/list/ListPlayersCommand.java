package scot.oskar.permissible.command.list;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.repository.PermissionRepository;

import java.awt.Color;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ListPlayersCommand extends CommandBase {

  private final RequiredArg<String> groupArg;

  public ListPlayersCommand() {
    super("players", "Lists all players in a group.");
    this.groupArg = this.withRequiredArg("group", "The group name", ArgTypes.STRING);
    this.requirePermission("permissible.list.players");
  }

  @Override
  protected void executeSync(@Nonnull CommandContext context) {
    String group = this.groupArg.get(context);

    PermissionRepository repository = PermissiblePlugin.getInstance().getPermissionRepository();
    Set<UUID> players = repository.getPlayersInGroup(group);

    if (players.isEmpty()) {
      context.sendMessage(Message.raw("No players in group '" + group + "'").color(Color.YELLOW));
      return;
    }

    context.sendMessage(Message.join(
        Message.raw("Players in group ").color(Color.YELLOW),
        Message.raw(group).color(Color.GREEN)
    ));

    for (UUID uuid : players) {
      context.sendMessage(Message.join(
          Message.raw("  ").color(Color.GRAY),
          Message.raw(uuid.toString()).color(Color.GRAY)
      ));
    }

    context.sendMessage(Message.join(
        Message.raw("Total: ").color(Color.GRAY),
        Message.raw(players.size() + " players").color(Color.GREEN)
    ));
  }
}
