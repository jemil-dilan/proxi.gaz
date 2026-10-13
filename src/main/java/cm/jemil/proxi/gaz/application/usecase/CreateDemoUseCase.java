package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.application.outbound.ReferenceGenerator;
import cm.jemil.proxi.gaz.domain.demo.Demo;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoDescription;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoName;
import cm.jemil.proxi.gaz.domain.demo.DemoReference;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateDemoUseCase {

  private final ReferenceGenerator referenceGenerator;
  private final DemoRepository demoRepository;
  private final DomainEventPublisher domainEventPublisher;

  public UUID execute(Command input) {
    var name = DemoName.from(input.name);
    if (demoRepository.existsByName(name)) {
      throw new DemoAlreadyExistsException();
    }
    var index = referenceGenerator.generateForDemo();
    var reference = DemoReference.fromIndex(index);
    var demo = Demo.of(DemoId.generate(), name, reference, DemoDescription.from(input.description));
    demoRepository.insert(demo);
    domainEventPublisher.publishDemoCreatedEvent(demo.toCreatedEvent());
    return demo.id();
  }

  public record Command(String name, String description) {}
}
