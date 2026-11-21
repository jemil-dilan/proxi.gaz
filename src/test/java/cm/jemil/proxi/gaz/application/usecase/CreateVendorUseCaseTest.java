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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateVendorUseCaseTest {

  private static final UserId ADMIN = UserId.generate();

  private final VendorOnboardingRepository onboardingRepository =
      mock(VendorOnboardingRepository.class);
  private final VendorSessionTokenGenerator tokenGenerator =
      mock(VendorSessionTokenGenerator.class);
  private final VendorSessionTokenHasher tokenHasher = mock(VendorSessionTokenHasher.class);
  private final Clock clock = mock(Clock.class);
  private final AuditLogPort auditLog = mock(AuditLogPort.class);
  private final CreateVendorUseCase useCase =
      new CreateVendorUseCase(
          onboardingRepository,
          tokenGenerator,
          tokenHasher,
          clock,
          auditLog,
          UnitOfWorkStubs.runningWork());

  @Test
  void execute_shouldInsertUserVendorAndHashedSessionAndReturnRawTokenOnce() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var token = new VendorSessionToken("one-time-secret");
    var hash = new VendorSessionTokenHash("sha256-hash");
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(hash);

    var result = useCase.execute(ADMIN, command(null));

    var userCaptor = ArgumentCaptor.forClass(User.class);
    var vendorCaptor = ArgumentCaptor.forClass(Vendor.class);
    var sessionCaptor = ArgumentCaptor.forClass(VendorSession.class);
    verify(onboardingRepository)
        .insert(userCaptor.capture(), vendorCaptor.capture(), sessionCaptor.capture());

    var user = userCaptor.getValue();
    var vendor = vendorCaptor.getValue();
    var session = sessionCaptor.getValue();
    assertThat(user.isVendor()).isTrue();
    assertThat(user.email()).isEmpty();
    assertThat(vendor.getUserId()).isEqualTo(user.getId());
    assertThat(session.vendorId()).isEqualTo(vendor.getId());
    assertThat(session.tokenHash()).isEqualTo(hash);
    assertThat(result.vendorId()).isEqualTo(vendor.getId());
    assertThat(result.sessionToken()).isEqualTo(token.value());
  }

  @Test
  void execute_shouldAttachOptionalEmailToTheUserAccount() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var token = new VendorSessionToken("one-time-secret");
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(new VendorSessionTokenHash("hash"));
    var email = Email.from("vendor@example.test");

    useCase.execute(ADMIN, command(email));

    var userCaptor = ArgumentCaptor.forClass(User.class);
    verify(onboardingRepository)
        .insert(userCaptor.capture(), any(Vendor.class), any(VendorSession.class));
    assertThat(userCaptor.getValue().email()).contains(email);
  }

  @Test
  void execute_shouldRecordTheCreationWithoutTheSessionSecret() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var token = new VendorSessionToken("one-time-secret");
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(new VendorSessionTokenHash("sha256-hash"));

    var result = useCase.execute(ADMIN, command(null));

    var captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditLog).record(captor.capture());
    var entry = captor.getValue();
    assertThat(entry.adminUserId()).isEqualTo(ADMIN);
    assertThat(entry.action()).isEqualTo(AuditAction.VENDOR_CREATED);
    assertThat(entry.targetType()).isEqualTo(AuditTargetType.VENDOR);
    assertThat(entry.targetId()).isEqualTo(result.vendorId().value());
    assertThat(entry.metadata()).isEqualTo(AuditMetadata.created(true));
    assertThat(entry.createdAt().value()).isEqualTo(Instant.EPOCH);
    assertThat(entry.toString()).doesNotContain("one-time-secret", "sha256-hash");
  }

  @Test
  void execute_shouldRecordNothingWhenTheInsertFails() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var token = new VendorSessionToken("one-time-secret");
    when(tokenGenerator.generate()).thenReturn(token);
    when(tokenHasher.hash(token)).thenReturn(new VendorSessionTokenHash("hash"));
    doThrow(new IllegalStateException("duplicate phone"))
        .when(onboardingRepository)
        .insert(any(User.class), any(Vendor.class), any(VendorSession.class));
    var command = command(null);

    assertThatThrownBy(() -> useCase.execute(ADMIN, command))
        .isInstanceOf(IllegalStateException.class);
    verifyNoInteractions(auditLog);
  }

  private CreateVendorUseCase.Command command(Email email) {
    return new CreateVendorUseCase.Command(
        email,
        VendorName.from("Vendor"),
        PhoneNumber.from("+237699002001"),
        GeoPoint.of(4.05, 9.70),
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(1200),
        true);
  }
}
