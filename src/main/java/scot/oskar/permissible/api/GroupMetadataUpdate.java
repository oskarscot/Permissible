package scot.oskar.permissible.api;

public record GroupMetadataUpdate(
    int weight,
    String prefix,
    String suffix,
    String displayName) {}
