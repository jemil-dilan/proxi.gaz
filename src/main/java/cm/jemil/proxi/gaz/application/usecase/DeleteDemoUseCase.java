package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.domain.demo.DemoDeletedEvent;
import cm.jemil.proxi.gaz.domain.demo.DemoId;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteDemoUseCase {

  private final DemoRepository demoRepository;
  private final DomainEventPublisher domainEventPublisher;

  public void execute(Command input) {
    var demoId = DemoId.from(input.demoId());
    if (!demoRepository.existsById(demoId)) {
      throw new DemoNotFoundException();
    }
    demoRepository.delete(demoId);
    domainEventPublisher.publishDemoDeletedEvent(new DemoDeletedEvent(demoId));
  }

  public record Command(UUID demoId) {}
}
