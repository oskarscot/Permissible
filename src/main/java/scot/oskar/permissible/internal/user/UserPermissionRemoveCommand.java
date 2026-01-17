package scot.oskar.permissible.internal.user;

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
import scot.oskar.permissible.internal.component.EntityStoreRegistry;
import scot.oskar.permissible.internal.component.PermissionAttachment;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class UserPermissionRemoveCommand extends AbstractWorldCommand {

  private final RequiredArg<UUID> playerArg;
  private final RequiredArg<String> permissionArg;

  public UserPermissionRemoveCommand() {
    super("remove", "Removes a permission from a user.");
    this.playerArg = this.withRequiredArg("player", "The player UUID", ArgTypes.PLAYER_UUID);
    this.permissionArg = this.withRequiredArg("permission", "The permission node",  ArgTypes.STRING);
    this.requirePermission("permissible.user.permission.remove");
  }

  @Override
  protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
    UUID playerUuid = this.playerArg.get(context);
    String permission = this.permissionArg.get(context);
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

    if (!oldAttachment.getPermissions().contains(permission)) {
      context.sendMessage(Message.raw("Player does not have permission: " + permission).color(Color.RED));
      return;
    }

    Set<String> newPermissions = new HashSet<>(oldAttachment.getPermissions());
    newPermissions.remove(permission);
    PermissionAttachment newAttachment = new PermissionAttachment(newPermissions, oldAttachment.getGroups());

    store.replaceComponent(
        playerRef,
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        newAttachment
    );

    context.sendMessage(Message.raw("Removed permission '" + permission + "' from player").color(Color.GREEN));
  }
}
