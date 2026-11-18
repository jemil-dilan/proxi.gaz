/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class UpdateVendorUseCaseTest {

  private static final UserId ADMIN = UserId.generate();

  private final VendorRepository repository = mock(VendorRepository.class);
  private final AuditLogPort auditLog = mock(AuditLogPort.class);
  private final Clock clock = mock(Clock.class);
  private final UpdateVendorUseCase useCase =
      new UpdateVendorUseCase(repository, auditLog, UnitOfWorkStubs.runningWork(), clock);

  @Test
  void execute_shouldUpdateProfileAndPreserveVendorAndUserIdentity() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var userId = UserId.generate();
    var vendor = vendor(VendorId.generate(), userId, "Before", true);
    when(repository.findById(vendor.getId())).thenReturn(vendor);

    useCase.execute(
        ADMIN,
        new UpdateVendorUseCase.Command(
            vendor.getId(),
            VendorName.from("After"),
            PhoneNumber.from("+237699002002"),
            GeoPoint.of(4.06, 9.71),
            AddressText.from("Akwa"),
            false,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1500)));

    var updatedCaptor = ArgumentCaptor.forClass(Vendor.class);
    verify(repository).update(updatedCaptor.capture());
    var updated = updatedCaptor.getValue();
    assertThat(updated.getId()).isEqualTo(vendor.getId());
    assertThat(updated.getUserId()).isEqualTo(userId);
    assertThat(updated.getName()).isEqualTo(VendorName.from("After"));
    assertThat(updated.isActive()).isTrue();
  }

  @Test
  void execute_shouldFailWhenVendorDoesNotExist() {
    var vendorId = VendorId.generate();
    UpdateVendorUseCase.Command command =
        new UpdateVendorUseCase.Command(
            vendorId,
            VendorName.from("After"),
            PhoneNumber.from("+237699002002"),
            GeoPoint.of(4.06, 9.71),
            AddressText.from("Akwa"),
            false,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1500));
    when(repository.findById(vendorId)).thenThrow(new VendorNotFoundException());

    assertThatThrownBy(() -> useCase.execute(ADMIN, command))
        .isInstanceOf(VendorNotFoundException.class);
    verify(repository, never()).update(any(Vendor.class));
    verifyNoInteractions(auditLog);
  }

  @Test
  void execute_shouldRecordTheNamesOfTheChangedFieldsOnly() {
    when(clock.now()).thenReturn(Instant.EPOCH);
    var vendor = vendor(VendorId.generate(), UserId.generate(), "Before", true);
    when(repository.findById(vendor.getId())).thenReturn(vendor);

    useCase.execute(
        ADMIN,
        new UpdateVendorUseCase.Command(
            vendor.getId(),
            VendorName.from("After"),
            PhoneNumber.from("+237699002009"),
            vendor.getLocation(),
            vendor.getAddressText(),
            vendor.isStation(),
            vendor.getDeliveryFeeType(),
            vendor.getDeliveryFeeAmount()));

    var captor = ArgumentCaptor.forClass(AuditEntry.class);
    verify(auditLog).record(captor.capture());
    var entry = captor.getValue();
    assertThat(entry.adminUserId()).isEqualTo(ADMIN);
    assertThat(entry.action()).isEqualTo(AuditAction.VENDOR_UPDATED);
    assertThat(entry.targetId()).isEqualTo(vendor.id());
    assertThat(entry.metadata()).isEqualTo(AuditMetadata.updated(List.of("name", "phone")));
    assertThat(entry.toString()).doesNotContain("+237699002009", "After");
  }

  @Test
  void execute_shouldWriteOnlyInsideTheUnitOfWork() {
    var outside =
        new UpdateVendorUseCase(repository, auditLog, UnitOfWorkStubs.notRunningWork(), clock);
    var vendor = vendor(VendorId.generate(), UserId.generate(), "Before", true);

    outside.execute(
        ADMIN,
        new UpdateVendorUseCase.Command(
            vendor.getId(),
            vendor.getName(),
            vendor.getPhone(),
            vendor.getLocation(),
            vendor.getAddressText(),
            vendor.isStation(),
            vendor.getDeliveryFeeType(),
            vendor.getDeliveryFeeAmount()));

    verifyNoInteractions(repository, auditLog);
  }

  private Vendor vendor(VendorId id, UserId userId, String name, boolean active) {
    return Vendor.reconstitute(
        id,
        userId,
        VendorName.from(name),
        PhoneNumber.from("+237699002001"),
        GeoPoint.of(4.05, 9.70),
        AddressText.from("Akwa"),
        false,
        DeliveryFeeType.from("flat"),
        Money.fcfa(1200),
        active);
  }
}
