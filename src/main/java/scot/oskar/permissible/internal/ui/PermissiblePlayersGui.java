package scot.oskar.permissible.internal.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import scot.oskar.permissible.internal.component.EntityStoreRegistry;
import scot.oskar.permissible.internal.component.PermissionAttachment;
import scot.oskar.permissible.internal.repository.PermissionRepository;

public class PermissiblePlayersGui
    extends InteractiveCustomUIPage<PermissiblePlayersGui.PlayerGuiData> {

  private String playerNameField;
  private String permissionField;
  private String playerGroupNameField;
  private String groupField;

  public PermissiblePlayersGui(@Nonnull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, PlayerGuiData.CODEC);
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder uiCommandBuilder,
      @Nonnull UIEventBuilder uiEventBuilder,
      @Nonnull Store<EntityStore> store) {
    uiCommandBuilder.append("Pages/PlayersPage.ui");
    PermissibleNavBar.setup(ref, uiCommandBuilder, uiEventBuilder, store, "players");
    uiCommandBuilder.set("#PlayerNameField.Value", playerNameField == null ? "" : playerNameField);
    uiCommandBuilder.set(
        "#PlayerPermissionField.Value", permissionField == null ? "" : permissionField);
    uiCommandBuilder.set(
        "#PlayerGroupNameField.Value",
        playerGroupNameField == null ? "" : playerGroupNameField);
    uiCommandBuilder.set("#PlayerGroupField.Value", groupField == null ? "" : groupField);

    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#PlayerNameField",
        EventData.of("@PlayerName", "#PlayerNameField.Value"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#PlayerPermissionField",
        EventData.of("@PlayerPermission", "#PlayerPermissionField.Value"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#PlayerGroupNameField",
        EventData.of("@PlayerGroupName", "#PlayerGroupNameField.Value"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#PlayerGroupField",
        EventData.of("@PlayerGroup", "#PlayerGroupField.Value"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#AddPlayerPermissionButton",
        EventData.of("Button", "AddPlayerPermission"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#RemovePlayerPermissionButton",
        EventData.of("Button", "RemovePlayerPermission"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#AddPlayerGroupButton",
        EventData.of("Button", "AddPlayerGroup"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#RemovePlayerGroupButton",
        EventData.of("Button", "RemovePlayerGroup"),
        false);
    buildPlayerList(uiCommandBuilder, uiEventBuilder, store);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull PlayerGuiData data) {
    super.handleDataEvent(ref, store, data);
    if (PermissibleNavBar.handleData(ref, store, data.navbar, () -> {})) {
      return;
    }

    if (data.playerName != null) {
      playerNameField = data.playerName.trim();
    }
    if (data.permissionField != null) {
      permissionField = data.permissionField.trim();
    }
    if (data.groupNameField != null) {
      groupField = data.groupNameField.trim();
    }
    if (data.playerGroupNameField != null) {
      playerGroupNameField = data.playerGroupNameField.trim();
    }
    if (data.button != null) {
      handleButton(store, data.button, data.targetPlayer);
    }

    UICommandBuilder commandBuilder = new UICommandBuilder();
    UIEventBuilder eventBuilder = new UIEventBuilder();
    buildPlayerList(commandBuilder, eventBuilder, store);
    sendUpdate(commandBuilder, eventBuilder, false);
  }

  private void handleButton(Store<EntityStore> store, String action, String targetPlayer) {
    Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
    if (action.equals("RefreshPlayer")) {
      if (targetPlayer == null) {
        return;
      }
      playerNameField = targetPlayer;
      playerGroupNameField = targetPlayer;
      return;
    }

    if (action.equals("AddPlayerPermission") || action.equals("RemovePlayerPermission")) {
      handlePermissionAction(store, player, action);
      return;
    }

    if (action.equals("AddPlayerGroup") || action.equals("RemovePlayerGroup")) {
      handleGroupAction(store, player, action);
    }
  }

  private void handlePermissionAction(Store<EntityStore> store, Player player, String action) {
    if (playerNameField == null || playerNameField.isBlank()) {
      player.sendMessage(Message.raw("Player name is required").color(Color.RED));
      return;
    }
    if (permissionField == null || permissionField.isBlank()) {
      player.sendMessage(Message.raw("Permission is required").color(Color.RED));
      return;
    }
    UUID playerUuid = resolvePlayerUuid(player, playerNameField);
    if (playerUuid == null) {
      return;
    }
    if (action.equals("AddPlayerPermission")) {
      if (addPermission(store, playerUuid, permissionField)) {
        player.sendMessage(Message.raw("Added permission to player").color(Color.GREEN));
      } else {
        player.sendMessage(Message.raw("Player already has permission").color(Color.RED));
      }
      return;
    }

    if (removePermission(store, playerUuid, permissionField)) {
      player.sendMessage(Message.raw("Removed permission from player").color(Color.GREEN));
    } else {
      player.sendMessage(Message.raw("Player does not have permission").color(Color.RED));
    }
  }

  private void handleGroupAction(Store<EntityStore> store, Player player, String action) {
    if (playerGroupNameField == null || playerGroupNameField.isBlank()) {
      player.sendMessage(Message.raw("Player name is required").color(Color.RED));
      return;
    }
    if (groupField == null || groupField.isBlank()) {
      player.sendMessage(Message.raw("Group name is required").color(Color.RED));
      return;
    }
    UUID playerUuid = resolvePlayerUuid(player, playerGroupNameField);
    if (playerUuid == null) {
      return;
    }
    if (action.equals("AddPlayerGroup")) {
      if (addGroup(store, playerUuid, groupField)) {
        player.sendMessage(Message.raw("Added player to group").color(Color.GREEN));
      } else {
        player.sendMessage(Message.raw("Player already in group").color(Color.RED));
      }
      return;
    }

    if (removeGroup(store, playerUuid, groupField)) {
      player.sendMessage(Message.raw("Removed player from group").color(Color.GREEN));
    } else {
      player.sendMessage(Message.raw("Player not in group").color(Color.RED));
    }
  }

  private UUID resolvePlayerUuid(Player player, String playerName) {
    PlayerRef target = Universe.get().getPlayer(playerName, NameMatching.EXACT);
    if (target == null) {
      player.sendMessage(Message.raw("Player not online: " + playerName).color(Color.RED));
      return null;
    }
    return target.getUuid();
  }

  private boolean addPermission(Store<EntityStore> store, UUID playerUuid, String permission) {
    PermissionAttachment attachment = getAttachment(store, playerUuid);
    if (attachment == null) {
      return false;
    }
    Set<String> newPermissions = new java.util.HashSet<>(attachment.getPermissions());
    if (!newPermissions.add(permission)) {
      return false;
    }
    PermissionAttachment newAttachment =
        new PermissionAttachment(newPermissions, attachment.getGroups());
    store.replaceComponent(
        Universe.get().getPlayer(playerUuid).getReference(),
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        newAttachment);
    return true;
  }

  private boolean removePermission(Store<EntityStore> store, UUID playerUuid, String permission) {
    PermissionAttachment attachment = getAttachment(store, playerUuid);
    if (attachment == null) {
      return false;
    }
    Set<String> newPermissions = new java.util.HashSet<>(attachment.getPermissions());
    if (!newPermissions.remove(permission)) {
      return false;
    }
    PermissionAttachment newAttachment =
        new PermissionAttachment(newPermissions, attachment.getGroups());
    store.replaceComponent(
        Universe.get().getPlayer(playerUuid).getReference(),
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        newAttachment);
    return true;
  }

  private boolean addGroup(Store<EntityStore> store, UUID playerUuid, String group) {
    PermissionAttachment attachment = getAttachment(store, playerUuid);
    if (attachment == null) {
      return false;
    }
    Set<String> newGroups = new java.util.HashSet<>(attachment.getGroups());
    if (!newGroups.add(group)) {
      return false;
    }
    PermissionAttachment newAttachment =
        new PermissionAttachment(attachment.getPermissions(), newGroups);
    store.replaceComponent(
        Universe.get().getPlayer(playerUuid).getReference(),
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        newAttachment);
    return true;
  }

  private boolean removeGroup(Store<EntityStore> store, UUID playerUuid, String group) {
    PermissionAttachment attachment = getAttachment(store, playerUuid);
    if (attachment == null) {
      return false;
    }
    Set<String> newGroups = new java.util.HashSet<>(attachment.getGroups());
    if (!newGroups.remove(group)) {
      return false;
    }
    PermissionAttachment newAttachment =
        new PermissionAttachment(attachment.getPermissions(), newGroups);
    store.replaceComponent(
        Universe.get().getPlayer(playerUuid).getReference(),
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        newAttachment);
    return true;
  }

  private PermissionAttachment getAttachment(Store<EntityStore> store, UUID playerUuid) {
    PlayerRef target = Universe.get().getPlayer(playerUuid);
    if (target == null) {
      return null;
    }
    return store.getComponent(
        target.getReference(),
        EntityStoreRegistry.get().getPermissionAttachmentComponentType());
  }

  private void buildPlayerList(
      UICommandBuilder uiCommandBuilder, UIEventBuilder eventBuilder, Store<EntityStore> store) {
    uiCommandBuilder.clear("#PlayerCards");
    PermissionRepository repository =
        scot.oskar.permissible.PermissiblePlugin.getInstance().getInternalPermissionRepository();
    List<PermissiblePlayerSnapshot> players = new ArrayList<>();
    for (PlayerRef playerRef : Universe.get().getPlayers()) {
      UUID playerUuid = playerRef.getUuid();
      players.add(
          new PermissiblePlayerSnapshot(
              playerUuid,
              playerRef.getUsername(),
              repository.loadPlayerPermissions(playerUuid),
              repository.loadPlayerGroups(playerUuid)));
    }
    players.sort((left, right) -> left.name().compareToIgnoreCase(right.name()));

    int index = 0;
    for (PermissiblePlayerSnapshot snapshot : players) {
      uiCommandBuilder.append("#PlayerCards", "Pages/PlayerEntry.ui");
      uiCommandBuilder.set(
          "#PlayerCards[" + index + "] #PlayerName.Text",
          snapshot.name());
      uiCommandBuilder.set(
          "#PlayerCards[" + index + "] #PlayerUuid.Text",
          snapshot.uuid().toString());
      uiCommandBuilder.set(
          "#PlayerCards[" + index + "] #PlayerPermissions.Text",
          formatList(snapshot.permissions()));
      uiCommandBuilder.set(
          "#PlayerCards[" + index + "] #PlayerGroups.Text",
          formatList(snapshot.groups()));
      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          "#PlayerCards[" + index + "] #RefreshPlayerButton",
          EventData.of("Button", "RefreshPlayer").append("TargetPlayer", snapshot.name()),
          false);
      index++;
    }
  }

  private String formatList(Set<String> values) {
    if (values == null || values.isEmpty()) {
      return "-";
    }
    List<String> sorted = new ArrayList<>(values);
    sorted.sort(String::compareToIgnoreCase);
    return String.join(", ", sorted);
  }

  public static class PlayerGuiData {
    static final String KEY_BUTTON = "Button";
    static final String KEY_NAVBAR = "NavBar";
    static final String KEY_PLAYER_NAME = "@PlayerName";
    static final String KEY_PERMISSION_FIELD = "@PlayerPermission";
    static final String KEY_GROUP_NAME = "@PlayerGroup";
    static final String KEY_PLAYER_GROUP_NAME = "@PlayerGroupName";
    static final String KEY_TARGET_PLAYER = "TargetPlayer";

    public static final BuilderCodec<PlayerGuiData> CODEC =
        BuilderCodec.<PlayerGuiData>builder(PlayerGuiData.class, PlayerGuiData::new)
            .addField(
                new KeyedCodec<>(KEY_PLAYER_NAME, Codec.STRING),
                (guiData, value) -> guiData.playerName = value,
                guiData -> guiData.playerName)
            .addField(
                new KeyedCodec<>(KEY_PERMISSION_FIELD, Codec.STRING),
                (guiData, value) -> guiData.permissionField = value,
                guiData -> guiData.permissionField)
            .addField(
                new KeyedCodec<>(KEY_GROUP_NAME, Codec.STRING),
                (guiData, value) -> guiData.groupNameField = value,
                guiData -> guiData.groupNameField)
            .addField(
                new KeyedCodec<>(KEY_PLAYER_GROUP_NAME, Codec.STRING),
                (guiData, value) -> guiData.playerGroupNameField = value,
                guiData -> guiData.playerGroupNameField)
            .addField(
                new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                (guiData, value) -> guiData.button = value,
                guiData -> guiData.button)
            .addField(
                new KeyedCodec<>(KEY_TARGET_PLAYER, Codec.STRING),
                (guiData, value) -> guiData.targetPlayer = value,
                guiData -> guiData.targetPlayer)
            .addField(
                new KeyedCodec<>(KEY_NAVBAR, Codec.STRING),
                (guiData, value) -> guiData.navbar = value,
                guiData -> guiData.navbar)
            .build();

    private String button;
    private String navbar;
    private String playerName;
    private String permissionField;
    private String groupNameField;
    private String playerGroupNameField;
    private String targetPlayer;
  }
}
