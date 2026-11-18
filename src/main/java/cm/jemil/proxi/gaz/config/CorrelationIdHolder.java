/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import org.slf4j.MDC;

/**
 * Thread-local holder for the current request's correlation ID. Used to propagate correlation IDs
 * from filters to exception handlers. The ID is mirrored in the SLF4J MDC under {@value #MDC_KEY},
 * which the log pattern ({@code logging.pattern.correlation}) prints on every line.
 */
public final class CorrelationIdHolder {

  public static final String MDC_KEY = "correlationId";

  private static final ThreadLocal<String> correlationId = new ThreadLocal<>();

  private CorrelationIdHolder() {}

  public static void set(String id) {
    correlationId.set(id);
    MDC.put(MDC_KEY, id);
  }

  public static String get() {
    return correlationId.get();
  }

  public static void clear() {
    correlationId.remove();
    MDC.remove(MDC_KEY);
  }
}
