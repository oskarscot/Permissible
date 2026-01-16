package scot.oskar.permissible.internal.entity;

import scot.oskar.volt.annotation.Entity;
import scot.oskar.volt.annotation.Identifier;
import scot.oskar.volt.annotation.NamedField;
import scot.oskar.volt.entity.PrimaryKeyType;

import java.util.UUID;

@Entity("player_permissions")
public class PlayerPermission {

    @Identifier(type = PrimaryKeyType.NUMBER, generated = true)
    private Long id;

    @NamedField(name = "player_uuid")
    private UUID playerUuid;

    private String permission;

    public PlayerPermission() {
    }

    public PlayerPermission(UUID playerUuid, String permission) {
        this.playerUuid = playerUuid;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
