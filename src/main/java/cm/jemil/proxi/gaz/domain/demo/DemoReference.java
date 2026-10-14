/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.demo;

public record DemoReference(String value) {

  public static final DemoReference NULL = new DemoReference("");

  public static DemoReference from(String value) {
    return new DemoReference(value);
  }

  public static DemoReference fromIndex(String index) {
    return from("DEMO-%s".formatted(index));
  }
}
