/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.notifications;

import cm.jemil.proxi.gaz.application.outbound.NotificationChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * Logging-only notification channel implementation. Logs notifications without actually delivering
 * them. Suitable for testing and when real delivery channels are unavailable.
 */
@Slf4j
public class LoggingNotificationChannel implements NotificationChannel {

  @Override
  public boolean sendToVendor(String vendorId, String title, String body) {
    log.info("Notification to vendor {}: {} - {}", vendorId, title, body);
    return true;
  }

  @Override
  public boolean sendToClient(String recipientPhone, String title, String body) {
    log.info("Notification to client {}: {} - {}", recipientPhone, title, body);
    return true;
  }
}
