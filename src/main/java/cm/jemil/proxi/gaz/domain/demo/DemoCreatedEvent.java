/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.demo;

public record DemoCreatedEvent(
    DemoId id, DemoName name, DemoReference reference, DemoDescription description) {}
