package scot.oskar.permissible.event;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import scot.oskar.permissible.PermissiblePlugin;
import scot.oskar.permissible.component.EntityStoreRegistry;
import scot.oskar.permissible.component.PermissionAttachment;
import scot.oskar.permissible.repository.PermissionRepository;

import java.util.Set;
import java.util.UUID;

public class PlayerReadyHandler {

  public void onPlayerReady(PlayerReadyEvent event) {;
    Store<EntityStore> store = event.getPlayerRef().getStore();
    UUIDComponent component = store.getComponent(event.getPlayerRef(), UUIDComponent.getComponentType());
    UUID playerUuid = component.getUuid();

    PermissionRepository repository = PermissiblePlugin.getInstance().getPermissionRepository();
    Set<String> permissions = repository.loadPlayerPermissions(playerUuid);
    Set<String> groups = repository.loadPlayerGroups(playerUuid);

    PermissionAttachment attachment = new PermissionAttachment(permissions, groups);

    store.addComponent(
        event.getPlayerRef(),
        EntityStoreRegistry.get().getPermissionAttachmentComponentType(),
        attachment);

  }
}
