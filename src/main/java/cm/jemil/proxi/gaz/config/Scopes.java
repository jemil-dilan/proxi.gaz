/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

public final class Scopes {

  public static final String ADMIN_SCOPE = "admin";
  public static final String ADMIN = "SCOPE_" + ADMIN_SCOPE;
  public static final String DEMO_READ_ALL = "SCOPE_demo:read:all";
  public static final String DEMO_CREATE = "SCOPE_demo:create";
  public static final String DEMO_READ = "SCOPE_demo:read";
  public static final String DEMO_UPDATE = "SCOPE_demo:update";
  public static final String DEMO_DELETE = "SCOPE_demo:delete";

  private Scopes() {}
}
