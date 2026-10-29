/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.user;

public enum UserRole {
  ADMIN("admin"),
  VENDOR("vendor");

  private final String scope;

  UserRole(String scope) {
    this.scope = scope;
  }

  public String scope() {
    return scope;
  }
}
