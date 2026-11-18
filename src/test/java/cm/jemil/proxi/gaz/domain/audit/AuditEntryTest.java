/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditEntryTest {

  private static final UserId ADMIN = UserId.generate();
  private static final UUID TARGET = UUID.randomUUID();
  private static final CreatedAt NOW = CreatedAt.from(Instant.parse("2026-11-18T10:00:00Z"));

  @Test
  void vendor_shouldBuildAVendorEntryWithAFreshId() {
    var first =
        AuditEntry.vendor(
            ADMIN, AuditAction.VENDOR_CREATED, TARGET, AuditMetadata.created(true), NOW);
    var second =
        AuditEntry.vendor(
            ADMIN, AuditAction.VENDOR_CREATED, TARGET, AuditMetadata.created(true), NOW);

    assertThat(first.adminUserId()).isEqualTo(ADMIN);
    assertThat(first.action()).isEqualTo(AuditAction.VENDOR_CREATED);
    assertThat(first.targetType()).isEqualTo(AuditTargetType.VENDOR);
    assertThat(first.targetId()).isEqualTo(TARGET);
    assertThat(first.metadata()).isEqualTo(AuditMetadata.created(true));
    assertThat(first.createdAt()).isEqualTo(NOW);
    assertThat(first.id()).isNotEqualTo(second.id());
  }

  @Test
  void constructor_shouldRejectMissingParts() {
    var id = AuditEntryId.generate();
    var metadata = AuditMetadata.empty();
    var action = AuditAction.VENDOR_UPDATED;
    var type = AuditTargetType.VENDOR;

    assertThatThrownBy(() -> new AuditEntry(null, ADMIN, action, type, TARGET, metadata, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new AuditEntry(id, null, action, type, TARGET, metadata, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new AuditEntry(id, ADMIN, null, type, TARGET, metadata, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new AuditEntry(id, ADMIN, action, null, TARGET, metadata, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new AuditEntry(id, ADMIN, action, type, null, metadata, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new AuditEntry(id, ADMIN, action, type, TARGET, null, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new AuditEntry(id, ADMIN, action, type, TARGET, metadata, null))
        .isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void statusAction_shouldMapTheNewState() {
    assertThat(AuditAction.forStatus(true)).isEqualTo(AuditAction.VENDOR_ACTIVATED);
    assertThat(AuditAction.forStatus(false)).isEqualTo(AuditAction.VENDOR_DEACTIVATED);
  }
}
