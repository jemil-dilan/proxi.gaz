/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.messaging;

import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.domain.demo.DemoCreatedEvent;
import cm.jemil.proxi.gaz.domain.demo.DemoDeletedEvent;
import cm.jemil.proxi.gaz.domain.demo.DemoUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class LoggingDomainEventPublisher implements DomainEventPublisher {

  @Override
  public void publishDemoCreatedEvent(DemoCreatedEvent event) {
    log.info("DEMO_CREATED {}", event);
  }

  @Override
  public void publishDemoUpdatedEvent(DemoUpdatedEvent event) {
    log.info("DEMO_UPDATED {}", event);
  }

  @Override
  public void publishDemoDeletedEvent(DemoDeletedEvent event) {
    log.info("DEMO_DELETED {}", event);
  }
}
