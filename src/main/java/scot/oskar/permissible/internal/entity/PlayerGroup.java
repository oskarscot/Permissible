package scot.oskar.permissible.internal.entity;

import scot.oskar.volt.annotation.Entity;
import scot.oskar.volt.annotation.Identifier;
import scot.oskar.volt.annotation.NamedField;
import scot.oskar.volt.entity.PrimaryKeyType;

import java.util.UUID;

@Entity("player_groups")
public class PlayerGroup {

    @Identifier(type = PrimaryKeyType.NUMBER, generated = true)
    private Long id;

    @NamedField(name = "player_uuid")
    private UUID playerUuid;

    @NamedField(name = "group_name")
    private String groupName;

    public PlayerGroup() {
    }

    public PlayerGroup(UUID playerUuid, String groupName) {
        this.playerUuid = playerUuid;
        this.groupName = groupName;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
