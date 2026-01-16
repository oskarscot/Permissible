package scot.oskar.permissible.internal.group.permission;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.internal.repository.PermissionRepository;

import java.awt.Color;
import javax.annotation.Nonnull;

public class GroupPermissionRemoveCommand extends CommandBase {

  private final RequiredArg<String> groupArg;
  private final RequiredArg<String> permissionArg;

  public GroupPermissionRemoveCommand() {
    super("remove", "Removes a permission from a group.");
    this.groupArg = this.withRequiredArg("group", "The group name", ArgTypes.STRING);
    this.permissionArg = this.withRequiredArg("permission", "The permission node", ArgTypes.STRING);
    this.requirePermission("permissible.group.permission.remove");
  }

  @Override
  protected void executeSync(@Nonnull CommandContext context) {
    String group = this.groupArg.get(context);
    String permission = this.permissionArg.get(context);

    PermissionRepository repository = PermissiblePlugin.getInstance().getInternalPermissionRepository();

    // Remove permission from group in database
    repository.removeGroupPermission(group, permission);

    context.sendMessage(Message.raw("Removed permission '" + permission + "' from group '" + group + "'").color(Color.GREEN));
  }
}
