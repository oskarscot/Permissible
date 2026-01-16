package scot.oskar.permissible.internal.ui;

import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class PermissibleUIRegistry {

  private static final List<Entry> ENTRIES;

  static {
    List<Entry> entries = new ArrayList<>();
    entries.add(new Entry("index", "Home", PermissibleIndexGui::new));
    entries.add(new Entry("groups", "Groups", PermissibleGroupsGui::new));
    entries.add(new Entry("players", "Players", PermissiblePlayersGui::new));
    entries.add(new Entry("default", "Default Group", PermissibleDefaultGroupGui::new));
    ENTRIES = Collections.unmodifiableList(entries);
  }

  private PermissibleUIRegistry() {}

  public static List<Entry> getEntries() {
    return ENTRIES;
  }

  public static Entry getEntry(String id) {
    return ENTRIES.stream().filter(entry -> entry.id().equals(id)).findFirst().orElse(null);
  }

  public static class Entry {

    private final String id;
    private final String displayName;
    private final Function<PlayerRef, ? extends InteractiveCustomUIPage<?>> guiSupplier;

    public Entry(
        String id,
        String displayName,
        Function<PlayerRef, ? extends InteractiveCustomUIPage<?>> guiSupplier) {
      this.id = id;
      this.displayName = displayName;
      this.guiSupplier = guiSupplier;
    }

    public String id() {
      return id;
    }

    public String displayName() {
      return displayName;
    }

    public Function<PlayerRef, ? extends InteractiveCustomUIPage<?>> guiSupplier() {
      return guiSupplier;
    }
  }
}
