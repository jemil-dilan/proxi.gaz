package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.application.outbound.ReferenceGenerator;
import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.DeleteDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetDemoByIdUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

  @Bean
  public CreateDemoUseCase createDemoUseCase(
      ReferenceGenerator referenceGenerator,
      DemoRepository demoRepository,
      DomainEventPublisher domainEventPublisher) {
    return new CreateDemoUseCase(referenceGenerator, demoRepository, domainEventPublisher);
  }

  @Bean
  public UpdateDemoUseCase updateDemoUseCase(
      DemoRepository demoRepository, DomainEventPublisher domainEventPublisher) {
    return new UpdateDemoUseCase(demoRepository, domainEventPublisher);
  }

  @Bean
  public DeleteDemoUseCase deleteDemoUseCase(
      DemoRepository demoRepository, DomainEventPublisher domainEventPublisher) {
    return new DeleteDemoUseCase(demoRepository, domainEventPublisher);
  }

  @Bean
  public GetAllDemoUseCase getAllDemoUseCase(DemoRepository demoRepository) {
    return new GetAllDemoUseCase(demoRepository);
  }

  @Bean
  public GetDemoByIdUseCase getDemoByIdUseCase(DemoRepository demoRepository) {
    return new GetDemoByIdUseCase(demoRepository);
  }
}
