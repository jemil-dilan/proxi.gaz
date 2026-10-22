/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.notifications;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoggingNotificationChannelTest {

  @Test
  void sendToVendor_ReturnsTrue() {
    var channel = new LoggingNotificationChannel();
    boolean result = channel.sendToVendor("vendor-uuid-123", "New Order", "You have a new order");
    assertThat(result).isTrue();
  }

  @Test
  void sendToVendor_WithNullValues_ReturnsTrue() {
    var channel = new LoggingNotificationChannel();
    // Null values should still return true (logging adapter doesn't validate)
    assertThat(channel.sendToVendor(null, null, null)).isTrue();
  }

  @Test
  void sendToClient_ReturnsTrue() {
    var channel = new LoggingNotificationChannel();
    boolean result =
        channel.sendToClient("+237123456789", "Order Update", "Your order is confirmed");
    assertThat(result).isTrue();
  }

  @Test
  void sendToClient_WithNullValues_ReturnsTrue() {
    var channel = new LoggingNotificationChannel();
    assertThat(channel.sendToClient(null, null, null)).isTrue();
  }
}
