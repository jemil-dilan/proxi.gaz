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
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RotateVendorSessionUseCaseTest {
  private static final UserId ADMIN = UserId.generate();
  private final VendorRepository vendorRepository = mock(VendorRepository.class);
  private final VendorSessionRepository sessionRepository = mock(VendorSessionRepository.class);
  private final VendorSessionTokenGenerator tokenGenerator =
      mock(VendorSessionTokenGenerator.class);
  private final VendorSessionTokenHasher tokenHasher = mock(VendorSessionTokenHasher.class);
  private final Clock clock = mock(Clock.class);
  private final AuditLogPort auditLog = mock(AuditLogPort.class);
  private final RotateVendorSessionUseCase useCase =
      new RotateVendorSessionUseCase(
          vendorRepository,
          sessionRepository,
          tokenGenerator,
          tokenHasher,
          clock,
          auditLog,
          UnitOfWorkStubs.runningWork());

  @Test
  void execute_shouldIssueOnceAndReplaceExistingSession() {
    var vendorId = VendorId.generate();
    var token = new VendorSessionToken("new-opaque-secret");
    var tokenHash = new VendorSessionTokenHash("new-hash");
    Vendor vendor = mock(Vendor.class);

    when(vendorRepository.findById(vendorId)).thenReturn(vendor);
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(tokenHash);
    when(clock.now()).thenReturn(Instant.EPOCH);

    var returnedToken = useCase.execute(ADMIN, vendorId.value());

    assertThat(returnedToken).isEqualTo(token.value());
    var session = ArgumentCaptor.forClass(cm.jemil.proxi.gaz.domain.vendor.VendorSession.class);
    verify(sessionRepository)
        .replaceForVendor(org.mockito.ArgumentMatchers.eq(vendorId), session.capture());
    assertThat(session.getValue().tokenHash()).isEqualTo(tokenHash);
    assertThat(session.getValue().createdAt().value()).isEqualTo(Instant.EPOCH);

    var captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditLog).record(captor.capture());
    var entry = captor.getValue();
    assertThat(entry.adminUserId()).isEqualTo(ADMIN);
    assertThat(entry.action()).isEqualTo(AuditAction.VENDOR_SESSION_ROTATED);
    assertThat(entry.targetId()).isEqualTo(vendorId.value());
    assertThat(entry.metadata()).isEqualTo(AuditMetadata.empty());
    assertThat(entry.toString()).doesNotContain("new-opaque-secret", "new-hash");
  }

  @Test
  void execute_shouldRecordNothingWhenTheVendorDoesNotExist() {
    var vendorId = VendorId.generate();
    when(tokenGenerator.generate()).thenReturn(new VendorSessionToken("secret"));
    when(clock.now()).thenReturn(Instant.EPOCH);
    when(vendorRepository.findById(vendorId)).thenThrow(new VendorNotFoundException());
    var id = vendorId.value();

    assertThatThrownBy(() -> useCase.execute(ADMIN, id))
        .isInstanceOf(VendorNotFoundException.class);
    verifyNoInteractions(sessionRepository, auditLog);
  }
}
