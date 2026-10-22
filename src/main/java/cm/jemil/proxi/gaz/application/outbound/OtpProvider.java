/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

/**
 * Port interface for OTP (One-Time Password) generation and delivery. In the pilot phase,
 * implementations should be null or no-op only. Later integrations (reverse WhatsApp/SMS) are
 * activated only if the fake-order rate exceeds 10%.
 *
 * <p>Pilot requirement: No paid SMS/WhatsApp recurring cost. All OTP operations go through this
 * interface.
 */
public interface OtpProvider {

  /**
   * Generate and send an OTP to the specified recipient.
   *
   * <p>In pilot implementations: no-op or logging only.
   *
   * <p>Future implementations: reverse WhatsApp / SMS integration.
   *
   * @param recipientPhone the phone number to receive the OTP, not null
   * @param purpose descriptive purpose for logging (e.g., "order_verification"), not null
   * @return the OTP code value (for testing purposes; production implementations may return a
   *     placeholder or null)
   */
  String generateAndSend(String recipientPhone, String purpose);
}
