/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.otp;

import cm.jemil.proxi.gaz.application.outbound.OtpProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Null OTP provider implementation for pilot phase. Does not send any actual OTP (no SMS/WhatsApp
 * costs). Logs requests for debugging purposes.
 */
@Slf4j
public class NullOtpProvider implements OtpProvider {

  @Override
  public String generateAndSend(String recipientPhone, String purpose) {
    log.debug(
        "OTP generation requested for phone: {} (purpose: {}) - null provider, no OTP sent",
        recipientPhone,
        purpose);
    return null;
  }
}
