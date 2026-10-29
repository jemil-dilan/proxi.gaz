/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.messaging;

import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingBeans {

  @Bean
  DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    return new LoggingDomainEventPublisher(applicationEventPublisher);
  }
}
