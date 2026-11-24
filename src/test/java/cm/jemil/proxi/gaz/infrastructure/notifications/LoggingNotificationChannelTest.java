/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.notifications;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
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

  @Test
  void sendToClient_shouldNeverLogTheFullPhoneNumber(CapturedOutput output) {
    new LoggingNotificationChannel().sendToClient("+237699001122", "Order Update", "Confirmed");

    assertThat(output).doesNotContain("+237699001122").doesNotContain("699001122");
    assertThat(output).contains("+237*******22").contains("Order Update");
  }

  @Test
  void maskPhone_shouldKeepTheCountryCodeAndTheLastTwoDigits() {
    assertThat(LoggingNotificationChannel.maskPhone("+237699001122")).isEqualTo("+237*******22");
    assertThat(LoggingNotificationChannel.maskPhone("699001122")).isEqualTo("*******22");
  }

  @Test
  void maskPhone_shouldHideShortOrMissingValuesEntirely() {
    assertThat(LoggingNotificationChannel.maskPhone(null)).isEqualTo("***");
    assertThat(LoggingNotificationChannel.maskPhone("")).isEqualTo("***");
    assertThat(LoggingNotificationChannel.maskPhone("123")).isEqualTo("***");
  }
}
