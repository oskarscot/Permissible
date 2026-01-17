package scot.oskar.permissible.internal.ui;

import java.util.Set;
import java.util.UUID;

public record PermissiblePlayerSnapshot(
    UUID uuid,
    String name,
    Set<String> permissions,
    Set<String> groups) {}
