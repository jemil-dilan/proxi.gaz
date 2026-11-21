/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RevokeVendorSessionUseCaseTest {
  private static final UserId ADMIN = UserId.generate();

  private final VendorRepository vendorRepository = mock(VendorRepository.class);
  private final VendorSessionRepository sessionRepository = mock(VendorSessionRepository.class);
  private final AuditLogPort auditLog = mock(AuditLogPort.class);
  private final Clock clock = mock(Clock.class);

  @Test
  void execute_shouldRevokeTheSessionAndRecordIt() {
    var vendorId = VendorId.generate();
    when(clock.now()).thenReturn(Instant.EPOCH);

    useCase(UnitOfWorkStubs.runningWork()).execute(ADMIN, vendorId.value());

    verify(sessionRepository).revokeForVendor(vendorId);
    var captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditLog).record(captor.capture());
    var entry = captor.getValue();
    assertThat(entry.adminUserId()).isEqualTo(ADMIN);
    assertThat(entry.action()).isEqualTo(AuditAction.VENDOR_SESSION_REVOKED);
    assertThat(entry.targetId()).isEqualTo(vendorId.value());
    assertThat(entry.metadata()).isEqualTo(AuditMetadata.empty());
    assertThat(entry.createdAt().value()).isEqualTo(Instant.EPOCH);
  }

  @Test
  void execute_shouldRecordNothingWhenTheVendorDoesNotExist() {
    var vendorId = VendorId.generate();
    when(vendorRepository.findById(vendorId)).thenThrow(new VendorNotFoundException());
    var useCase = useCase(UnitOfWorkStubs.runningWork());
    var id = vendorId.value();

    assertThatThrownBy(() -> useCase.execute(ADMIN, id))
        .isInstanceOf(VendorNotFoundException.class);
    verifyNoInteractions(sessionRepository, auditLog);
  }

  @Test
  void execute_shouldWriteOnlyInsideTheUnitOfWork() {
    useCase(UnitOfWorkStubs.notRunningWork()).execute(ADMIN, VendorId.generate().value());

    verifyNoInteractions(vendorRepository, sessionRepository, auditLog);
  }

  private RevokeVendorSessionUseCase useCase(
      cm.jemil.proxi.gaz.application.outbound.UnitOfWork unitOfWork) {
    return new RevokeVendorSessionUseCase(
        vendorRepository, sessionRepository, auditLog, unitOfWork, clock);
  }
}
