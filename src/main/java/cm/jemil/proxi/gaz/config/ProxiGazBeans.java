/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.application.outbound.Configurations;
import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.ReferenceGenerator;
import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.DeleteDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetDemoByIdUseCase;
import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxiGazBeans {
  @Bean
  public LoginAdminUseCase loginAdminUseCase(
      AdminUserRepository adminUserRepository,
      PasswordVerifier passwordVerifier,
      AccessTokenIssuer accessTokenIssuer) {
    return new LoginAdminUseCase(adminUserRepository, passwordVerifier, accessTokenIssuer);
  }

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

  @Bean
  Configurations configurations(
      @Value("${proxigaz.security.jwt.access-token-ttl:15m}") Duration accessTokenTtl,
      @Value("${proxigaz.security.jwt.secret:}") String secret,
      @Value("${proxigaz.admin.bootstrap.enabled:false}") boolean bootstrapEnabled,
      @Value("${proxigaz.admin.bootstrap.email:}") String bootstrapEmail,
      @Value("${proxigaz.admin.bootstrap.password:}") String bootstrapPassword) {
    return new ConfigurationsImpl(
        accessTokenTtl, secret, bootstrapEnabled, bootstrapEmail, bootstrapPassword);
  }
}
