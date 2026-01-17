package scot.oskar.permissible.api;

public record PermissibleGroupInfo(
    String name,
    int weight,
    String prefix,
    String suffix,
    String displayName) {}
