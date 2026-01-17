package scot.oskar.permissible.internal.entity;

import scot.oskar.volt.annotation.Entity;
import scot.oskar.volt.annotation.Identifier;
import scot.oskar.volt.annotation.NamedField;
import scot.oskar.volt.entity.PrimaryKeyType;

@Entity("group_metadata")
public class GroupMetadata {

  @Identifier(type = PrimaryKeyType.NUMBER, generated = true)
  private Long id;

  @NamedField(name = "group_name")
  private String groupName;

  private int weight;

  private String prefix;

  private String suffix;

  @NamedField(name = "display_name")
  private String displayName;

  public GroupMetadata() {}

  public GroupMetadata(
      String groupName,
      int weight,
      String prefix,
      String suffix,
      String displayName) {
    this.groupName = groupName;
    this.weight = weight;
    this.prefix = prefix;
    this.suffix = suffix;
    this.displayName = displayName;
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

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
