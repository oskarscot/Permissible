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
import javax.annotation.Nonnull;
import scot.oskar.permissible.PermissiblePlugin;

public class PermissibleDefaultGroupGui
    extends InteractiveCustomUIPage<PermissibleDefaultGroupGui.DefaultGroupData> {

  private String defaultGroupField;

  public PermissibleDefaultGroupGui(@Nonnull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismiss, DefaultGroupData.CODEC);
  }

  @Override
  public void build(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder uiCommandBuilder,
      @Nonnull UIEventBuilder uiEventBuilder,
      @Nonnull Store<EntityStore> store) {
    uiCommandBuilder.append("Pages/DefaultGroupPage.ui");
    PermissibleNavBar.setup(ref, uiCommandBuilder, uiEventBuilder, store, "default");
    String configuredGroup =
        PermissiblePlugin.getInstance().getPluginConfiguration().defaultGroup;
    defaultGroupField = configuredGroup;
    uiCommandBuilder.set("#DefaultGroupField.Value", configuredGroup);
    uiCommandBuilder.set("#CurrentDefaultGroup.Text", configuredGroup);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.ValueChanged,
        "#DefaultGroupField",
        EventData.of("@DefaultGroup", "#DefaultGroupField.Value"),
        false);
    uiEventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        "#SaveDefaultGroupButton",
        EventData.of("Button", "SaveDefaultGroup"),
        false);
  }

  @Override
  public void handleDataEvent(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      @Nonnull DefaultGroupData data) {
    super.handleDataEvent(ref, store, data);
    if (PermissibleNavBar.handleData(ref, store, data.navbar, () -> {})) {
      return;
    }

    if (data.defaultGroup != null) {
      defaultGroupField = data.defaultGroup.trim();
    }

    if (data.button != null && data.button.equals("SaveDefaultGroup")) {
      Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
      if (defaultGroupField == null || defaultGroupField.isBlank()) {
        player.sendMessage(Message.raw("Default group name is required").color(Color.RED));
      } else {
        PermissiblePlugin.getInstance().getPluginConfiguration().defaultGroup = defaultGroupField;
        PermissiblePlugin.getInstance().getPluginConfiguration().save();
        player.sendMessage(Message.raw("Default group saved").color(Color.GREEN));
      }
    }

    UICommandBuilder commandBuilder = new UICommandBuilder();
    UIEventBuilder eventBuilder = new UIEventBuilder();
    String configuredGroup =
        PermissiblePlugin.getInstance().getPluginConfiguration().defaultGroup;
    commandBuilder.set("#DefaultGroupField.Value", configuredGroup);
    commandBuilder.set("#CurrentDefaultGroup.Text", configuredGroup);
    sendUpdate(commandBuilder, eventBuilder, false);
  }

  public static class DefaultGroupData {
    static final String KEY_NAVBAR = "NavBar";
    static final String KEY_DEFAULT_GROUP = "@DefaultGroup";
    static final String KEY_BUTTON = "Button";

    public static final BuilderCodec<DefaultGroupData> CODEC =
        BuilderCodec.<DefaultGroupData>builder(DefaultGroupData.class, DefaultGroupData::new)
            .addField(
                new KeyedCodec<>(KEY_DEFAULT_GROUP, Codec.STRING),
                (guiData, value) -> guiData.defaultGroup = value,
                guiData -> guiData.defaultGroup)
            .addField(
                new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                (guiData, value) -> guiData.button = value,
                guiData -> guiData.button)
            .addField(
                new KeyedCodec<>(KEY_NAVBAR, Codec.STRING),
                (guiData, value) -> guiData.navbar = value,
                guiData -> guiData.navbar)
            .build();

    private String defaultGroup;
    private String button;
    private String navbar;
  }
}
