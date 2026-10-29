/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import org.junit.jupiter.api.Test;

class SecureVendorSessionTokenGeneratorTest {

  @Test
  void generate_shouldProduceDistinctHighEntropyUrlSafeTokens() {
    var generator = new SecureVendorSessionTokenGenerator(new SecureRandom());

    var first = generator.generate();
    var second = generator.generate();

    assertThat(first.value()).hasSize(43).matches("[A-Za-z0-9_-]+");
    assertThat(second).isNotEqualTo(first);
  }
}
