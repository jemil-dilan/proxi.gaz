/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoUpdatedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateDemoUseCase {

  private final DemoRepository demoRepository;
  private final DomainEventPublisher domainEventPublisher;

  public void execute(Command input) {
    DemoId demoId = DemoId.from(input.demoId());
    Demo demo = demoRepository.load(demoId);
    if (!demo.getName().value().equals(input.name)
        && demoRepository.existsByNameAndIdNot(input.name, demoId)) {
      throw new DemoAlreadyExistsException();
    }
    DemoUpdatedEvent event =
        demo.update(DemoName.from(input.name), DemoDescription.from(input.description));
    demoRepository.update(demo);
    domainEventPublisher.publishDemoUpdatedEvent(event);
  }

  public record Command(UUID demoId, String name, String description) {}
}
