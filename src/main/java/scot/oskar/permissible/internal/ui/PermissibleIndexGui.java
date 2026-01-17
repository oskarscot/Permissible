package scot.oskar.permissible.internal.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.internal.repository.PermissionRepository;

public class PermissibleIndexGui
    extends InteractiveCustomUIPage<PermissibleIndexGui.IndexGuiData> {

  // Card accent colors
  private static final String COLOR_GROUPS = "#4a7c59";
  private static final String COLOR_PLAYERS = "#4a6a7c";
  private static final String COLOR_DEFAULT = "#7c6a4a";

  public PermissibleIndexGui(@Nonnull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, IndexGuiData.CODEC);
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder uiCommandBuilder,
      @Nonnull UIEventBuilder uiEventBuilder,
      @Nonnull Store<EntityStore> store) {
    uiCommandBuilder.append("Pages/IndexPage.ui");

    PermissionRepository repository = PermissiblePlugin.getInstance().getInternalPermissionRepository();
    String defaultGroupName = PermissiblePlugin.getInstance().getPluginConfiguration().defaultGroup;

    // Calculate stats
    Set<String> allGroups = repository.getAllGroups();
    int groupCount = allGroups.size();
    int totalPermissions = 0;
    int totalPlayersWithGroups = 0;

    for (String group : allGroups) {
      Set<String> perms = repository.loadGroupPermissions(group);
      totalPermissions += perms.size();
      totalPlayersWithGroups += repository.getPlayersInGroup(group).size();
    }

    // Build cards
    uiCommandBuilder.clear("#IndexCards");

    // Groups card
    uiCommandBuilder.append("#IndexCards", "Pages/IndexCard.ui");
    uiCommandBuilder.set("#IndexCards[0] #AccentBar.Background", "(Color: " + COLOR_GROUPS + ")");
    uiCommandBuilder.set("#IndexCards[0] #CardTitle.Text", "Groups");
    uiCommandBuilder.set("#IndexCards[0] #PrimaryStat.Text", groupCount + " groups");
    uiCommandBuilder.set("#IndexCards[0] #SecondaryStat.Text", totalPermissions + " permissions");
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#IndexCards[0]",
        EventData.of("Button", "groups"));

    // Players card
    uiCommandBuilder.append("#IndexCards", "Pages/IndexCard.ui");
    uiCommandBuilder.set("#IndexCards[1] #AccentBar.Background", "(Color: " + COLOR_PLAYERS + ")");
    uiCommandBuilder.set("#IndexCards[1] #CardTitle.Text", "Players");
    uiCommandBuilder.set("#IndexCards[1] #PrimaryStat.Text", totalPlayersWithGroups + " players");
    uiCommandBuilder.set("#IndexCards[1] #SecondaryStat.Text", "with custom groups");
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#IndexCards[1]",
        EventData.of("Button", "players"));

    // Default Group card
    uiCommandBuilder.append("#IndexCards", "Pages/IndexCard.ui");
    uiCommandBuilder.set("#IndexCards[2] #AccentBar.Background", "(Color: " + COLOR_DEFAULT + ")");
    uiCommandBuilder.set("#IndexCards[2] #CardTitle.Text", "Default");
    uiCommandBuilder.set("#IndexCards[2] #PrimaryStat.Text", defaultGroupName);
    Set<String> defaultPerms = repository.loadGroupPermissions(defaultGroupName);
    uiCommandBuilder.set("#IndexCards[2] #SecondaryStat.Text", defaultPerms.size() + " permissions");
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#IndexCards[2]",
        EventData.of("Button", "default"));

    // Update summary bar
    uiCommandBuilder.set("#TotalGroups.Text", groupCount + " groups");
    uiCommandBuilder.set("#TotalPlayers.Text", totalPlayersWithGroups + " players");
    uiCommandBuilder.set("#TotalPermissions.Text", totalPermissions + " permissions");
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull IndexGuiData data) {
    super.handleDataEvent(ref, store, data);
    if (data.button != null) {
      PermissibleUIRegistry.Entry entry = PermissibleUIRegistry.getEntry(data.button);
      if (entry != null) {
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());
        if (playerRef != null && player != null) {
          player.getPageManager().openCustomPage(ref, store, entry.guiSupplier().apply(playerRef));
        }
        return;
      }
    }
    sendUpdate();
  }

  public static class IndexGuiData {
    static final String KEY_BUTTON = "Button";

    public static final BuilderCodec<IndexGuiData> CODEC =
        BuilderCodec.<IndexGuiData>builder(IndexGuiData.class, IndexGuiData::new)
            .addField(
                new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                (guiData, value) -> guiData.button = value,
                guiData -> guiData.button)
            .build();

    private String button;
  }
}
