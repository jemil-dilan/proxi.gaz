/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.BrandRepository;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.brand.Brand;
import cm.jemil.proxi.gaz.domain.brand.BrandAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.brand.BrandName;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateBrandUseCaseTest {

  private static final UserId ADMIN = UserId.generate();
  private static final Instant NOW = Instant.parse("2026-11-24T09:00:00Z");

  private final BrandRepository brandRepository = mock(BrandRepository.class);
  private final AuditLogPort auditLog = mock(AuditLogPort.class);
  private final Clock clock = mock(Clock.class);
  private final CreateBrandUseCase useCase =
      new CreateBrandUseCase(brandRepository, auditLog, UnitOfWorkStubs.runningWork(), clock);

  @BeforeEach
  void fixTime() {
    when(clock.now()).thenReturn(NOW);
  }

  @Test
  void execute_shouldStoreAnActiveBrandAndAuditItsCreation() {
    var name = BrandName.from("Tradex");

    var brand = useCase.execute(ADMIN, name);

    assertThat(brand.name()).isEqualTo(name);
    assertThat(brand.active()).isTrue();
    assertThat(brand.createdAt().value()).isEqualTo(NOW);
    verify(brandRepository).insert(brand);
    var captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditLog).record(captor.capture());
    var entry = captor.getValue();
    assertThat(entry.adminUserId()).isEqualTo(ADMIN);
    assertThat(entry.action()).isEqualTo(AuditAction.BRAND_CREATED);
    assertThat(entry.targetType()).isEqualTo(AuditTargetType.BRAND);
    assertThat(entry.targetId()).isEqualTo(brand.id().value());
    assertThat(entry.metadata()).isEqualTo(AuditMetadata.created(true));
  }

  @Test
  void execute_shouldRejectAnExistingNameWithoutWritingAnything() {
    var name = BrandName.from("  tRADEX ");
    when(brandRepository.existsByName(name)).thenReturn(true);

    assertThatThrownBy(() -> useCase.execute(ADMIN, name))
        .isInstanceOf(BrandAlreadyExistsException.class);
    verify(brandRepository, never()).insert(any(Brand.class));
    verifyNoInteractions(auditLog);
  }

  @Test
  void execute_shouldNotAuditWhenTheDatabaseRejectsAConcurrentDuplicate() {
    var name = BrandName.from("Tradex");
    doThrow(new BrandAlreadyExistsException()).when(brandRepository).insert(any(Brand.class));

    assertThatThrownBy(() -> useCase.execute(ADMIN, name))
        .isInstanceOf(BrandAlreadyExistsException.class);
    verifyNoInteractions(auditLog);
  }

  @Test
  void execute_shouldWriteOnlyInsideTheUnitOfWork() {
    var outside =
        new CreateBrandUseCase(brandRepository, auditLog, UnitOfWorkStubs.notRunningWork(), clock);

    outside.execute(ADMIN, BrandName.from("Tradex"));

    verifyNoInteractions(brandRepository, auditLog);
  }
}
