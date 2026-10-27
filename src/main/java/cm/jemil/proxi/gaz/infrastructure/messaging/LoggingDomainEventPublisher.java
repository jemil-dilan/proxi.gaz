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
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@RequiredArgsConstructor
class LoggingDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publishDemoCreatedEvent(DemoCreatedEvent event) {
    log.info(
        "{{\"eventType\":\"DEMO_CREATED\",\"eventId\":\"{}\",\"name\":\"{}\",\"reference\":\"{}\"}}",
        event.id().value(),
        event.name().value(),
        event.reference().value());
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public void publishDemoUpdatedEvent(DemoUpdatedEvent event) {
    log.info(
        "{{\"eventType\":\"DEMO_UPDATED\",\"eventId\":\"{}\",\"name\":\"{}\"}}",
        event.id().value(),
        event.name().value());
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public void publishDemoDeletedEvent(DemoDeletedEvent event) {
    log.info("{{\"eventType\":\"DEMO_DELETED\",\"eventId\":\"{}\"}}", event.id().value());
    applicationEventPublisher.publishEvent(event);
  }
}
