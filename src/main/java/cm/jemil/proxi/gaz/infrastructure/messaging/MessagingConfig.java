package cm.jemil.proxi.gaz.infrastructure.messaging;

import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

  @Bean
  DomainEventPublisher domainEventPublisher() {
    return new LoggingDomainEventPublisher();
  }
}
