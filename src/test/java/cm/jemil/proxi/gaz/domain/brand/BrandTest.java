/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BrandTest {

  private static final CreatedAt NOW = CreatedAt.from(Instant.parse("2026-11-24T09:00:00Z"));

  @Test
  void create_shouldBuildAnActiveBrandWithAFreshId() {
    var first = Brand.create(BrandName.from("Tradex"), NOW);
    var second = Brand.create(BrandName.from("Tradex"), NOW);

    assertThat(first.name()).isEqualTo(BrandName.from("Tradex"));
    assertThat(first.active()).isTrue();
    assertThat(first.createdAt()).isEqualTo(NOW);
    assertThat(first.id()).isNotEqualTo(second.id());
  }

  @Test
  void constructor_shouldRejectMissingParts() {
    var id = BrandId.generate();
    var name = BrandName.from("Tradex");

    assertThatThrownBy(() -> new Brand(null, name, true, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new Brand(id, null, true, NOW))
        .isInstanceOf(InvalidDomainValueException.class);
    assertThatThrownBy(() -> new Brand(id, name, true, null))
        .isInstanceOf(InvalidDomainValueException.class);
  }
}
