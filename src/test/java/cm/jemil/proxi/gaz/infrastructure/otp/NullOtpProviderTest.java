/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.otp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NullOtpProviderTest {

  @Test
  void generateAndSend_ReturnsNull() {
    var provider = new NullOtpProvider();
    String result = provider.generateAndSend("+237123456789", "order_verification");
    assertThat(result).isNull();
  }

  @Test
  void generateAndSend_WithDifferentPurposes_ReturnsNull() {
    var provider = new NullOtpProvider();

    assertThat(provider.generateAndSend("+237123456789", "login")).isNull();
    assertThat(provider.generateAndSend("+237987654321", "reset")).isNull();
    assertThat(provider.generateAndSend("", "test")).isNull();
  }
}
