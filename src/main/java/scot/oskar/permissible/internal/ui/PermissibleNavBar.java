package scot.oskar.permissible.internal.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class PermissibleNavBar {

  private PermissibleNavBar() {}

  public static void setup(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UICommandBuilder uiCommandBuilder,
      @Nonnull UIEventBuilder uiEventBuilder,
      @Nonnull Store<EntityStore> store,
      @Nonnull String selectedTab) {
    int index = 0;
    for (PermissibleUIRegistry.Entry entry : PermissibleUIRegistry.getEntries()) {
      if (entry.id().equals("index")) {
        continue;
      }
      uiCommandBuilder.append("#HeaderTabs", "Pages/NavTab.ui");
      String basePath = "#HeaderTabs[" + index + "] #NavTab";
      boolean isSelected = entry.id().equals(selectedTab);
      uiCommandBuilder.set(basePath + ".Text", entry.displayName());
      uiCommandBuilder.set(basePath + ".Disabled", isSelected);
      uiEventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          basePath,
          EventData.of("NavBar", entry.id()));
      index++;
    }
  }

  public static boolean handleData(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Store<EntityStore> store,
      String navBarData,
      Runnable onCancel) {
    if (navBarData == null) {
      return false;
    }
    PermissibleUIRegistry.Entry entry = PermissibleUIRegistry.getEntry(navBarData);
    if (entry == null) {
      return false;
    }
    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    Player player = store.getComponent(ref, Player.getComponentType());
    if (playerRef == null || player == null) {
      return false;
    }
    onCancel.run();
    player.getPageManager().openCustomPage(ref, store, entry.guiSupplier().apply(playerRef));
    return true;
  }
}
