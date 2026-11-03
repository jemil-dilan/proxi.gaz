/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

/**
 * Thread-local holder for the current request's correlation ID. Used to propagate correlation IDs
 * from filters to exception handlers.
 */
public final class CorrelationIdHolder {

  private static final ThreadLocal<String> correlationId = new ThreadLocal<>();

  private CorrelationIdHolder() {}

  public static void set(String id) {
    correlationId.set(id);
  }

  public static String get() {
    return correlationId.get();
  }

  public static void clear() {
    correlationId.remove();
  }
}
