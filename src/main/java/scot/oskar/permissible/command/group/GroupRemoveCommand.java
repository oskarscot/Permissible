package scot.oskar.permissible.command.group;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import scot.oskar.permissible.component.EntityStoreRegistry;
import scot.oskar.permissible.component.PermissionAttachment;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class GroupRemoveCommand extends AbstractWorldCommand {

  private final RequiredArg<UUID> playerArg;
  private final RequiredArg<String> groupArg;

  public GroupRemoveCommand() {
    super("remove", "Removes a group from a user.");
    this.playerArg = this.withRequiredArg("player", "The player to remove the group from", ArgTypes.PLAYER_UUID);
    this.groupArg = this.withRequiredArg("group", "The group name",  ArgTypes.STRING);
    this.requirePermission("permissible.group.remove");
  }

  @Override
  protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
    UUID playerUuid = this.playerArg.get(context);
    String group = this.groupArg.get(context);
    PlayerRef player = Universe.get().getPlayer(playerUuid);

    if(player == null) {
      context.sendMessage(Message.raw("Player is null").color(Color.RED));
      return;
    }

    Ref<EntityStore> playerRef = player.getReference();

    PermissionAttachment oldAttachment = store.getComponent(
        playerRef,
        EntityStoreRegistry.get().getPermissionAttachmentComponentType()
    );

    if (oldAttachment == null) {
      context.sendMessage(Message.raw("Player has no permission attachment").color(Color.RED));
      return;
    }

    if (!oldAttachment.getGroups().contains(group)) {
      context.sendMessage(Message.raw("Player is not in group: " + group).color(Color.RED));
      return;
    }

    Set<String> newGroups = new HashSet<>(oldAttachment.getGroups());
    newGroups.remove(group);
    PermissionAttachment newAttachment = new PermissionAttachment(oldAttachment.getPermissions(), newGroups);

    store.replaceComponent(
        playerRef,
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        newAttachment
    );

    context.sendMessage(Message.raw("Removed player from group: " + group).color(Color.GREEN));
  }
}
