/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static cm.jemil.proxi.gaz.application.usecase.UnitOfWorkStubs.notRunningWork;
import static cm.jemil.proxi.gaz.application.usecase.UnitOfWorkStubs.runningWork;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorId;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SetVendorStatusUseCaseTest {
  private static final UserId ADMIN = UserId.generate();
  private static final Instant NOW = Instant.parse("2026-11-18T10:00:00Z");

  private final VendorRepository repository = mock(VendorRepository.class);
  private final AuditLogPort auditLog = mock(AuditLogPort.class);
  private final Clock clock = mock(Clock.class);

  private SetVendorStatusUseCase useCase(boolean runsWork) {
    when(clock.now()).thenReturn(NOW);
    return new SetVendorStatusUseCase(
        repository, auditLog, runsWork ? runningWork() : notRunningWork(), clock);
  }

  @Test
  void execute_shouldChangeOnlyTheRequestedVendorStatus() {
    var vendor = vendor(true);
    when(repository.findById(vendor.getId())).thenReturn(vendor);

    useCase(true).execute(ADMIN, vendor.id(), false);

    var captor = ArgumentCaptor.forClass(Vendor.class);
    verify(repository).update(captor.capture());
    assertThat(captor.getValue().isActive()).isFalse();
    assertThat(captor.getValue().getName()).isEqualTo(VendorName.from("Vendor"));
    assertThat(captor.getValue().getPhone()).isEqualTo(PhoneNumber.from("+237699000001"));
  }

  @Test
  void execute_shouldRecordDeactivationWithThePreviousStatus() {
    var vendor = vendor(true);
    when(repository.findById(vendor.getId())).thenReturn(vendor);

    useCase(true).execute(ADMIN, vendor.id(), false);

    var entry = recordedEntry();
    assertThat(entry.adminUserId()).isEqualTo(ADMIN);
    assertThat(entry.action()).isEqualTo(AuditAction.VENDOR_DEACTIVATED);
    assertThat(entry.targetType()).isEqualTo(AuditTargetType.VENDOR);
    assertThat(entry.targetId()).isEqualTo(vendor.id());
    assertThat(entry.metadata()).isEqualTo(AuditMetadata.statusChanged(true));
    assertThat(entry.createdAt().value()).isEqualTo(NOW);
  }

  @Test
  void execute_shouldStillRecordAStatusThatIsAlreadySet() {
    var vendor = vendor(true);
    when(repository.findById(vendor.getId())).thenReturn(vendor);

    useCase(true).execute(ADMIN, vendor.id(), true);

    var entry = recordedEntry();
    assertThat(entry.action()).isEqualTo(AuditAction.VENDOR_ACTIVATED);
    assertThat(entry.metadata()).isEqualTo(AuditMetadata.statusChanged(true));
  }

  @Test
  void execute_shouldRecordNothingWhenTheVendorDoesNotExist() {
    var vendorId = VendorId.generate();
    when(repository.findById(vendorId)).thenThrow(new VendorNotFoundException());
    var useCase = useCase(true);
    var id = vendorId.value();

    assertThatThrownBy(() -> useCase.execute(ADMIN, id, false))
        .isInstanceOf(VendorNotFoundException.class);
    verify(repository, never()).update(any(Vendor.class));
    verifyNoInteractions(auditLog);
  }

  @Test
  void execute_shouldWriteOnlyInsideTheUnitOfWork() {
    useCase(false).execute(ADMIN, VendorId.generate().value(), false);

    verifyNoInteractions(repository, auditLog);
  }

  private AuditEntry recordedEntry() {
    var captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditLog).record(captor.capture());
    return captor.getValue();
  }

  private static Vendor vendor(boolean active) {
    return Vendor.of(
        UserId.generate(),
        VendorName.from("Vendor"),
        PhoneNumber.from("+237699000001"),
        GeoPoint.of(4.05, 9.70),
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(1000),
        active);
  }
}
