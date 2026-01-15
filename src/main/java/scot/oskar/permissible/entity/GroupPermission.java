package scot.oskar.permissible.entity;

import scot.oskar.volt.annotation.Entity;
import scot.oskar.volt.annotation.Identifier;
import scot.oskar.volt.annotation.NamedField;
import scot.oskar.volt.entity.PrimaryKeyType;

@Entity("group_permissions")
public class GroupPermission {

    @Identifier(type = PrimaryKeyType.NUMBER, generated = true)
    private Long id;

    @NamedField(name = "group_name")
    private String groupName;

    private String permission;

    public GroupPermission() {
    }

    public GroupPermission(String groupName, String permission) {
        this.groupName = groupName;
        this.permission = permission;
    }

    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
