/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditMetadataTest {

  @Test
  void empty_shouldHaveNoEntries() {
    assertThat(AuditMetadata.empty().values()).isEmpty();
  }

  @Test
  void created_shouldRecordActivationFlag() {
    assertThat(AuditMetadata.created(true).values()).containsExactly(Map.entry("isActive", true));
  }

  @Test
  void statusChanged_shouldRecordPreviousFlag() {
    assertThat(AuditMetadata.statusChanged(false).values())
        .containsExactly(Map.entry("previousActive", false));
  }

  @Test
  void updated_shouldRecordChangedFieldNamesInOrder() {
    assertThat(AuditMetadata.updated(List.of("name", "phone")).values())
        .containsExactly(Map.entry("changedFields", List.of("name", "phone")));
  }

  @Test
  void updated_shouldCopyTheFieldList() {
    var fields = new ArrayList<>(List.of("name"));
    var metadata = AuditMetadata.updated(fields);

    fields.add("phone");

    assertThat(metadata.values()).containsEntry("changedFields", List.of("name"));
  }

  @Test
  void of_shouldRejectKeysOutsideTheAllowlist() {
    var values = Map.<String, Object>of("sessionToken", "secret");
    assertThatThrownBy(() -> AuditMetadata.of(values))
        .isInstanceOf(InvalidDomainValueException.class)
        .hasMessageContaining("sessionToken");
  }

  @Test
  void of_shouldRejectValuesOfTheWrongType() {
    var values = Map.<String, Object>of("isActive", "yes");
    assertThatThrownBy(() -> AuditMetadata.of(values))
        .isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void of_shouldRejectUnknownChangedFieldNames() {
    var values = Map.<String, Object>of("changedFields", List.of("password"));
    assertThatThrownBy(() -> AuditMetadata.of(values))
        .isInstanceOf(InvalidDomainValueException.class);
  }

  @Test
  void of_shouldAcceptStoredMetadata() {
    var values = Map.<String, Object>of("changedFields", List.of("name"), "isActive", true);

    assertThat(AuditMetadata.of(values).values()).isEqualTo(values);
  }

  @Test
  void values_shouldBeImmutable() {
    var values = AuditMetadata.created(true).values();
    assertThatThrownBy(() -> values.put("isActive", false))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
