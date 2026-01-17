package scot.oskar.permissible.internal.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.internal.entity.GroupMetadata;
import scot.oskar.permissible.internal.repository.PermissionRepository;

public class PermissibleGroupsGui
    extends InteractiveCustomUIPage<PermissibleGroupsGui.GroupGuiData> {

  private final Map<String, Set<String>> groupPermissions = new LinkedHashMap<>();
  private String groupNameField;
  private String permissionField;

  public PermissibleGroupsGui(@Nonnull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, GroupGuiData.CODEC);
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder uiCommandBuilder,
      @Nonnull UIEventBuilder uiEventBuilder,
      @Nonnull Store<EntityStore> store) {
    uiCommandBuilder.append("Pages/GroupsPage.ui");
    PermissibleNavBar.setup(ref, uiCommandBuilder, uiEventBuilder, store, "groups");
    uiCommandBuilder.set("#GroupNameField.Value", groupNameField == null ? "" : groupNameField);
    uiCommandBuilder.set(
        "#GroupPermissionField.Value", permissionField == null ? "" : permissionField);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#GroupNameField",
        EventData.of("@GroupName", "#GroupNameField.Value"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#GroupPermissionField",
        EventData.of("@PermissionField", "#GroupPermissionField.Value"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#AddGroupButton",
        EventData.of("Button", "AddGroup"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#AddGroupPermissionButton",
        EventData.of("Button", "AddPermission"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#RemoveGroupPermissionButton",
        EventData.of("Button", "RemovePermission"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#DeleteGroupButton",
        EventData.of("Button", "DeleteGroup"),
        false);
    buildGroupList(uiCommandBuilder, uiEventBuilder);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull GroupGuiData data) {
    super.handleDataEvent(ref, store, data);
    if (PermissibleNavBar.handleData(ref, store, data.navbar, () -> {})) {
      return;
    }

    if (data.groupName != null) {
      groupNameField = data.groupName.trim();
    }
    if (data.permissionField != null) {
      permissionField = data.permissionField.trim();
    }

    if (data.button != null) {
      handleButton(store, data.button, data.targetGroup, data.permission);
    }

    UICommandBuilder commandBuilder = new UICommandBuilder();
    UIEventBuilder eventBuilder = new UIEventBuilder();
    buildGroupList(commandBuilder, eventBuilder);
    sendUpdate(commandBuilder, eventBuilder, false);
  }

  private void handleButton(
      Store<EntityStore> store, String action, String targetGroup, String permissionKey) {
    PermissionRepository repository = PermissiblePlugin.getInstance().getInternalPermissionRepository();
    Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());

    if (action.equals("AddGroup")) {
      String groupName = validateGroupInput(player);
      if (groupName == null) {
        return;
      }
      if (repository.ensureGroupExists(groupName)) {
        player.sendMessage(Message.raw("Added group: " + groupName).color(Color.GREEN));
      } else {
        player.sendMessage(Message.raw("Group already exists: " + groupName).color(Color.RED));
      }
      return;
    }

    if (action.equals("AddPermission")) {
      String groupName = validateGroupInput(player);
      String permissionValue = validatePermissionInput(player);
      if (groupName == null || permissionValue == null) {
        return;
      }
      repository.addGroupPermission(groupName, permissionValue);
      player.sendMessage(
          Message.raw("Added permission to group: " + groupName).color(Color.GREEN));
      return;
    }

    if (action.equals("RemovePermission")) {
      String groupName = validateGroupInput(player);
      String permissionToRemove = validatePermissionInput(player);
      if (groupName == null || permissionToRemove == null) {
        return;
      }
      repository.removeGroupPermission(groupName, permissionToRemove);
      player.sendMessage(
          Message.raw("Removed permission from group: " + groupName).color(Color.GREEN));
      return;
    }

    if (action.equals("DeleteGroup")) {
      String groupName = targetGroup;
      if (groupName == null || groupName.isBlank()) {
        groupName = validateGroupInput(player);
        if (groupName == null) {
          return;
        }
      }
      if (!repository.getPlayersInGroup(groupName).isEmpty()) {
        player.sendMessage(
            Message.raw("Cannot delete group with members").color(Color.RED));
        return;
      }
      repository.deleteGroup(groupName);
      player.sendMessage(Message.raw("Deleted group: " + groupName).color(Color.GREEN));
      return;
    }

    if (action.equals("RemoveGroupPermission")) {
      if (targetGroup == null || permissionKey == null) {
        return;
      }
      repository.removeGroupPermission(targetGroup, permissionKey);
      player.sendMessage(
          Message.raw("Removed permission from group: " + targetGroup).color(Color.GREEN));
      return;
    }
  }

  private String validateGroupInput(Player player) {
    if (groupNameField == null || groupNameField.isBlank()) {
      player.sendMessage(Message.raw("Group name is required").color(Color.RED));
      return null;
    }
    return groupNameField.trim();
  }

  private String validatePermissionInput(Player player) {
    if (permissionField == null || permissionField.isBlank()) {
      player.sendMessage(Message.raw("Permission is required").color(Color.RED));
      return null;
    }
    return permissionField.trim();
  }

  private void buildGroupList(UICommandBuilder uiCommandBuilder, UIEventBuilder eventBuilder) {
    uiCommandBuilder.clear("#GroupCards");
    groupPermissions.clear();

    PermissionRepository repository = PermissiblePlugin.getInstance().getInternalPermissionRepository();
    List<String> groups = new ArrayList<>(repository.getAllGroups());
    groups.sort(String::compareToIgnoreCase);

    for (String group : groups) {
      Set<String> permissions = repository.loadGroupPermissions(group);
      groupPermissions.put(group, permissions);
    }

    int index = 0;
    for (Map.Entry<String, Set<String>> entry : groupPermissions.entrySet()) {
      uiCommandBuilder.append("#GroupCards", "Pages/GroupEntry.ui");
      uiCommandBuilder.set("#GroupCards[" + index + "] #GroupName.Text", entry.getKey());
      GroupMetadata metadata = repository.ensureGroupMetadata(entry.getKey());
      if (metadata != null) {
        String displayName = metadata.getDisplayName() == null ? entry.getKey() : metadata.getDisplayName();
        uiCommandBuilder.set(
            "#GroupCards[" + index + "] #GroupDisplayName.Text",
            displayName);
        uiCommandBuilder.set(
            "#GroupCards[" + index + "] #GroupWeight.Text",
            String.valueOf(metadata.getWeight()));
        uiCommandBuilder.set(
            "#GroupCards[" + index + "] #GroupPrefix.Text",
            metadata.getPrefix() == null ? "" : metadata.getPrefix());
        uiCommandBuilder.set(
            "#GroupCards[" + index + "] #GroupSuffix.Text",
            metadata.getSuffix() == null ? "" : metadata.getSuffix());
      }

      String permissionsListId = "#GroupCards[" + index + "] #PermissionsList";
      uiCommandBuilder.clear(permissionsListId);

      List<String> sortedPermissions = new ArrayList<>(entry.getValue());
      sortedPermissions.sort(String::compareToIgnoreCase);

      int permissionIndex = 0;
      for (String permission : sortedPermissions) {
        uiCommandBuilder.append(permissionsListId, "Pages/PermissionEntry.ui");
        String permissionBase = permissionsListId + "[" + permissionIndex + "]";
        uiCommandBuilder.set(permissionBase + " #PermissionName.Text", permission);
        eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            permissionBase + " #RemovePermissionButton",
            EventData.of("Button", "RemoveGroupPermission")
                .append("TargetGroup", entry.getKey())
                .append("Permission", permission),
            false);
        permissionIndex++;
      }
      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          "#GroupCards[" + index + "] #DeleteGroupButton",
          EventData.of("Button", "DeleteGroup").append("TargetGroup", entry.getKey()),
          false);
      index++;
    }
  }

  public static class GroupGuiData {
    static final String KEY_BUTTON = "Button";
    static final String KEY_GROUP_NAME = "@GroupName";
    static final String KEY_PERMISSION_FIELD = "@PermissionField";
    static final String KEY_NAVBAR = "NavBar";
    static final String KEY_TARGET_GROUP = "TargetGroup";
    static final String KEY_PERMISSION = "Permission";

    public static final BuilderCodec<GroupGuiData> CODEC =
        BuilderCodec.<GroupGuiData>builder(GroupGuiData.class, GroupGuiData::new)
            .addField(
                new KeyedCodec<>(KEY_GROUP_NAME, Codec.STRING),
                (guiData, value) -> guiData.groupName = value,
                guiData -> guiData.groupName)
            .addField(
                new KeyedCodec<>(KEY_PERMISSION_FIELD, Codec.STRING),
                (guiData, value) -> guiData.permissionField = value,
                guiData -> guiData.permissionField)
            .addField(
                new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                (guiData, value) -> guiData.button = value,
                guiData -> guiData.button)
            .addField(
                new KeyedCodec<>(KEY_TARGET_GROUP, Codec.STRING),
                (guiData, value) -> guiData.targetGroup = value,
                guiData -> guiData.targetGroup)
            .addField(
                new KeyedCodec<>(KEY_PERMISSION, Codec.STRING),
                (guiData, value) -> guiData.permission = value,
                guiData -> guiData.permission)
            .addField(
                new KeyedCodec<>(KEY_NAVBAR, Codec.STRING),
                (guiData, value) -> guiData.navbar = value,
                guiData -> guiData.navbar)
            .build();

    private String button;
    private String groupName;
    private String permissionField;
    private String navbar;
    private String targetGroup;
    private String permission;
  }
}
