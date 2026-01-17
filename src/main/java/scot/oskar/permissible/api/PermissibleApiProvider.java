package scot.oskar.permissible.api;

import scot.oskar.permissible.PermissiblePlugin;

public final class PermissibleApiProvider {
  private PermissibleApiProvider() {}

  public static PermissibleApi get() {
    return PermissiblePlugin.getInstance().getApi();
  }
}
