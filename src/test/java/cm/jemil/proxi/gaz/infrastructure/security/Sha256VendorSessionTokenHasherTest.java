/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.vendor.VendorSessionToken;
import org.junit.jupiter.api.Test;

class Sha256VendorSessionTokenHasherTest {

  private final Sha256VendorSessionTokenHasher hasher = new Sha256VendorSessionTokenHasher();

  @Test
  void hash_shouldBeDeterministicAndNotContainRawToken() {
    var token = new VendorSessionToken("random-secret");

    var first = hasher.hash(token);
    var second = hasher.hash(token);

    assertThat(first).isEqualTo(second);
    assertThat(first.value()).hasSize(64).isNotEqualTo(token.value());
  }
}
