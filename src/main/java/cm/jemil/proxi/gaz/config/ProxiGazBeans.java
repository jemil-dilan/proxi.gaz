/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.BrandRepository;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.Configurations;
import cm.jemil.proxi.gaz.application.outbound.DemoRepository;
import cm.jemil.proxi.gaz.application.outbound.DomainEventPublisher;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.RateLimiter;
import cm.jemil.proxi.gaz.application.outbound.ReferenceGenerator;
import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.application.usecase.CreateBrandUseCase;
import cm.jemil.proxi.gaz.application.usecase.CreateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.CreateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.DeleteDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAllVendorsUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetAuditLogUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetBrandsUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetDemoByIdUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetVendorByIdUseCase;
import cm.jemil.proxi.gaz.application.usecase.GetVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.application.usecase.RevokeVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.RotateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.SetVendorStatusUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateDemoUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.UpdateVendorUseCase;
import cm.jemil.proxi.gaz.application.usecase.ValidateVendorSessionUseCase;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxiGazBeans {

  @Bean
  public LoginAdminUseCase loginAdminUseCase(
      UserRepository userRepository,
      PasswordVerifier passwordVerifier,
      AccessTokenIssuer accessTokenIssuer,
      RateLimiter rateLimiter) {
    return new LoginAdminUseCase(userRepository, passwordVerifier, accessTokenIssuer, rateLimiter);
  }

  @Bean
  public CreateVendorUseCase createVendorUseCase(
      VendorOnboardingRepository onboardingRepository,
      VendorSessionTokenGenerator tokenGenerator,
      VendorSessionTokenHasher tokenHasher,
      Clock clock,
      AuditLogPort auditLog,
      UnitOfWork unitOfWork) {
    return new CreateVendorUseCase(
        onboardingRepository, tokenGenerator, tokenHasher, clock, auditLog, unitOfWork);
  }

  @Bean
  public UpdateVendorUseCase updateVendorUseCase(
      VendorRepository vendorRepository,
      AuditLogPort auditLog,
      UnitOfWork unitOfWork,
      Clock clock) {
    return new UpdateVendorUseCase(vendorRepository, auditLog, unitOfWork, clock);
  }

  @Bean
  public SetVendorStatusUseCase setVendorStatusUseCase(
      VendorRepository vendorRepository,
      AuditLogPort auditLog,
      UnitOfWork unitOfWork,
      Clock clock) {
    return new SetVendorStatusUseCase(vendorRepository, auditLog, unitOfWork, clock);
  }

  @Bean
  public CreateBrandUseCase createBrandUseCase(
      BrandRepository brandRepository, AuditLogPort auditLog, UnitOfWork unitOfWork, Clock clock) {
    return new CreateBrandUseCase(brandRepository, auditLog, unitOfWork, clock);
  }

  @Bean
  public GetBrandsUseCase getBrandsUseCase(BrandRepository brandRepository) {
    return new GetBrandsUseCase(brandRepository);
  }

  @Bean
  public GetAuditLogUseCase getAuditLogUseCase(AuditLogPort auditLog) {
    return new GetAuditLogUseCase(auditLog);
  }

  @Bean
  public GetAllVendorsUseCase listVendorsUseCase(VendorRepository vendorRepository) {
    return new GetAllVendorsUseCase(vendorRepository);
  }

  @Bean
  public ValidateVendorSessionUseCase validateVendorSessionUseCase(
      VendorSessionRepository repository,
      VendorSessionTokenHasher tokenHasher,
      VendorRepository vendorRepository) {
    return new ValidateVendorSessionUseCase(repository, tokenHasher, vendorRepository);
  }

  @Bean
  public RotateVendorSessionUseCase rotateVendorSessionUseCase(
      VendorRepository vendorRepository,
      VendorSessionRepository sessionRepository,
      VendorSessionTokenGenerator tokenGenerator,
      VendorSessionTokenHasher tokenHasher,
      Clock clock,
      AuditLogPort auditLog,
      UnitOfWork unitOfWork) {
    return new RotateVendorSessionUseCase(
        vendorRepository,
        sessionRepository,
        tokenGenerator,
        tokenHasher,
        clock,
        auditLog,
        unitOfWork);
  }

  @Bean
  public RevokeVendorSessionUseCase revokeVendorSessionUseCase(
      VendorRepository vendorRepository,
      VendorSessionRepository sessionRepository,
      AuditLogPort auditLog,
      UnitOfWork unitOfWork,
      Clock clock) {
    return new RevokeVendorSessionUseCase(
        vendorRepository, sessionRepository, auditLog, unitOfWork, clock);
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
  public GetVendorByIdUseCase getVendorUseCase(VendorRepository vendorRepository) {
    return new GetVendorByIdUseCase(vendorRepository);
  }

  @Bean
  public GetVendorSessionUseCase getVendorSessionUseCase(VendorRepository vendorRepository) {
    return new GetVendorSessionUseCase(vendorRepository);
  }

  @Bean
  public UpdateVendorSessionUseCase updateVendorSessionUseCase(
      VendorSessionRepository vendorSessionRepository, Clock clock) {
    return new UpdateVendorSessionUseCase(vendorSessionRepository, clock);
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
