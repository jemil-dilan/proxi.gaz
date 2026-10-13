package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.demo.DemoCreatedEvent;
import cm.jemil.proxi.gaz.domain.demo.DemoDeletedEvent;
import cm.jemil.proxi.gaz.domain.demo.DemoUpdatedEvent;

public interface DomainEventPublisher {

  void publishDemoCreatedEvent(DemoCreatedEvent event);

  void publishDemoUpdatedEvent(DemoUpdatedEvent event);

  void publishDemoDeletedEvent(DemoDeletedEvent event);
}
