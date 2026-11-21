/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.application.outbound.AuditLogPort;
import cm.jemil.proxi.gaz.application.outbound.UnitOfWork;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorOnboardingRepository;
import cm.jemil.proxi.gaz.application.outbound.VendorRepository;
import cm.jemil.proxi.gaz.application.usecase.SetVendorStatusUseCase;
import cm.jemil.proxi.gaz.application.views.AuditLogView;
import cm.jemil.proxi.gaz.domain.audit.AuditAction;
import cm.jemil.proxi.gaz.domain.audit.AuditEntry;
import cm.jemil.proxi.gaz.domain.audit.AuditMetadata;
import cm.jemil.proxi.gaz.domain.audit.AuditTargetType;
import cm.jemil.proxi.gaz.domain.shared.AddressText;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.GeoPoint;
import cm.jemil.proxi.gaz.domain.shared.Money;
import cm.jemil.proxi.gaz.domain.shared.PaginationFetchRequest;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.shared.PhoneNumber;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.User;
import cm.jemil.proxi.gaz.domain.vendor.DeliveryFeeType;
import cm.jemil.proxi.gaz.domain.vendor.Vendor;
import cm.jemil.proxi.gaz.domain.vendor.VendorName;
import cm.jemil.proxi.gaz.domain.vendor.VendorSession;
import cm.jemil.proxi.gaz.domain.vendor.VendorSessionTokenHash;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;

@Tag("data")
@SpringBootTest
@ActiveProfiles("data_test")
class AuditLogDataTest {

  private static final AtomicInteger PHONE = new AtomicInteger(6201);
  private static final Instant T0 = Instant.parse("2026-11-18T08:00:00Z");

  @Autowired private AuditLogPort auditLog;
  @Autowired private UnitOfWork unitOfWork;
  @Autowired private UserRepository userRepository;
  @Autowired private VendorRepository vendorRepository;
  @Autowired private VendorOnboardingRepository onboardingRepository;
  @Autowired private SetVendorStatusUseCase setVendorStatusUseCase;
  @Autowired private JdbcTemplate jdbc;

  private UserId admin;

  @BeforeEach
  void createAdmin() {
    var user =
        User.createAdmin(
            Email.from("audit-" + UUID.randomUUID() + "@example.test"),
            PasswordHash.from("{noop}irrelevant"),
            CreatedAt.from(Instant.now()));
    userRepository.insert(user);
    admin = user.getId();
  }

  @Test
  void record_shouldStoreTheEntryWithJsonbMetadataAndReadItBackWithTheAdminEmail() {
    var vendorId = UUID.randomUUID();
    var entry =
        AuditEntry.vendor(
            admin,
            AuditAction.VENDOR_UPDATED,
            vendorId,
            AuditMetadata.updated(List.of("name", "phone")),
            CreatedAt.from(T0));

    unitOfWork.inTransaction(
        () -> {
          auditLog.record(entry);
          return entry;
        });

    assertThat(
            jdbc.queryForObject(
                "SELECT jsonb_typeof(c_metadata -> 'changedFields') FROM t_admin_audit_log"
                    + " WHERE c_id = ?",
                String.class,
                entry.id().value()))
        .isEqualTo("array");
    var page = auditLog.find(AuditTargetType.VENDOR, vendorId, new PaginationFetchRequest(10, 0));
    assertThat(page.total()).isEqualTo(1);
    var view = page.elements().getFirst();
    assertThat(view.entry()).isEqualTo(entry);
    assertThat(view.adminEmail()).isEqualTo(adminEmail());
  }

  @Test
  void record_shouldRefuseToWriteOutsideABusinessTransaction() {
    var entry =
        AuditEntry.vendor(
            admin,
            AuditAction.VENDOR_SESSION_REVOKED,
            UUID.randomUUID(),
            AuditMetadata.empty(),
            CreatedAt.from(T0));

    assertThatThrownBy(() -> auditLog.record(entry))
        .isInstanceOf(IllegalTransactionStateException.class);
  }

  @Test
  void find_shouldFilterByTargetAndPageNewestFirst() {
    var vendorId = UUID.randomUUID();
    var otherVendorId = UUID.randomUUID();
    unitOfWork.inTransaction(
        () -> {
          for (int minute = 0; minute < 3; minute++) {
            auditLog.record(
                AuditEntry.vendor(
                    admin,
                    AuditAction.VENDOR_ACTIVATED,
                    vendorId,
                    AuditMetadata.statusChanged(minute % 2 == 0),
                    CreatedAt.from(T0.plus(minute, ChronoUnit.MINUTES))));
          }
          auditLog.record(
              AuditEntry.vendor(
                  admin,
                  AuditAction.VENDOR_CREATED,
                  otherVendorId,
                  AuditMetadata.created(true),
                  CreatedAt.from(T0.plus(10, ChronoUnit.MINUTES))));
          return vendorId;
        });

    var first = auditLog.find(AuditTargetType.VENDOR, vendorId, new PaginationFetchRequest(2, 0));
    var second = auditLog.find(AuditTargetType.VENDOR, vendorId, new PaginationFetchRequest(2, 1));

    assertThat(first.total()).isEqualTo(3);
    assertThat(first.totalPages()).isEqualTo(2);
    assertThat(createdAt(first.elements()))
        .containsExactly(T0.plus(2, ChronoUnit.MINUTES), T0.plus(1, ChronoUnit.MINUTES));
    assertThat(createdAt(second.elements())).containsExactly(T0);
    var everything = auditLog.find(null, null, new PaginationFetchRequest(100, 0));
    assertThat(everything.elements())
        .extracting(view -> view.entry().targetId())
        .contains(vendorId, otherVendorId);
  }

  @Test
  void find_shouldListEntriesWithTheSameTimestampInReverseInsertionOrder() {
    var vendorId = UUID.randomUUID();
    var actions =
        List.of(
            AuditAction.VENDOR_CREATED,
            AuditAction.VENDOR_UPDATED,
            AuditAction.VENDOR_DEACTIVATED,
            AuditAction.VENDOR_SESSION_ROTATED,
            AuditAction.VENDOR_SESSION_REVOKED);
    for (var action : actions) {
      unitOfWork.inTransaction(
          () -> {
            auditLog.record(
                AuditEntry.vendor(
                    admin, action, vendorId, AuditMetadata.empty(), CreatedAt.from(T0)));
            return action;
          });
    }

    var page = auditLog.find(AuditTargetType.VENDOR, vendorId, new PaginationFetchRequest(10, 0));

    assertThat(page.elements())
        .extracting(view -> view.entry().action())
        .containsExactlyElementsOf(actions.reversed());
  }

  @Test
  void unitOfWork_shouldRollBackTheAuditRowWithTheBusinessChange() {
    var vendor = persistVendor(true);
    var entry =
        AuditEntry.vendor(
            admin,
            AuditAction.VENDOR_DEACTIVATED,
            vendor.id(),
            AuditMetadata.statusChanged(true),
            CreatedAt.from(T0));

    assertThatThrownBy(
            () ->
                unitOfWork.inTransaction(
                    () -> {
                      vendor.deactivate();
                      vendorRepository.update(vendor);
                      auditLog.record(entry);
                      throw new IllegalStateException("business failure after both writes");
                    }))
        .isInstanceOf(IllegalStateException.class);

    assertThat(vendorRepository.findById(vendor.getId()).isActive()).isTrue();
    assertThat(auditRows(vendor.id())).isZero();
  }

  @Test
  void setVendorStatus_shouldCommitTheChangeAndItsAuditRowTogether() {
    var vendor = persistVendor(true);

    setVendorStatusUseCase.execute(admin, vendor.id(), false);

    assertThat(vendorRepository.findById(vendor.getId()).isActive()).isFalse();
    assertThat(auditRows(vendor.id())).isOne();
  }

  @Test
  void setVendorStatus_shouldRollBackTheChangeWhenTheAuditRowCannotBeStored() {
    var vendor = persistVendor(true);
    var unknownAdmin = UserId.generate();
    var vendorId = vendor.id();

    assertThatThrownBy(() -> setVendorStatusUseCase.execute(unknownAdmin, vendorId, false))
        .isInstanceOf(DataIntegrityViolationException.class);

    assertThat(vendorRepository.findById(vendor.getId()).isActive()).isTrue();
    assertThat(auditRows(vendorId)).isZero();
  }

  private String adminEmail() {
    return jdbc.queryForObject(
        "SELECT c_email FROM t_users WHERE c_id = ?", String.class, admin.value());
  }

  private Integer auditRows(UUID targetId) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM t_admin_audit_log WHERE c_target_id = ?", Integer.class, targetId);
  }

  private static List<Instant> createdAt(List<AuditLogView> views) {
    return views.stream().map(view -> view.entry().createdAt().value()).toList();
  }

  private Vendor persistVendor(boolean active) {
    var createdAt = CreatedAt.from(Instant.now());
    var user = User.createVendor(null, createdAt);
    var vendor =
        Vendor.of(
            user.getId(),
            VendorName.from("Audit Data Vendor"),
            PhoneNumber.from("+237699" + String.format("%06d", PHONE.getAndIncrement())),
            GeoPoint.of(4.051, 9.702),
            AddressText.from("Akwa"),
            false,
            DeliveryFeeType.from("flat"),
            Money.fcfa(1200),
            active);
    onboardingRepository.insert(
        user,
        vendor,
        VendorSession.of(
            vendor.getId(),
            new VendorSessionTokenHash("audit-hash-" + UUID.randomUUID()),
            createdAt));
    return vendor;
  }
}
