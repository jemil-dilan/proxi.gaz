/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

/**
 * Port interface for sending notifications to vendors and clients. Primary implementation in pilot:
 * Web Push. Escalation path: manual admin call (when Web Push fails or for time-sensitive orders).
 *
 * <p>Notification failures must NOT break the order flow. Implementations should be non-blocking
 * and handle failures gracefully.
 */
public interface NotificationChannel {

  /**
   * Send a notification to a vendor.
   *
   * @param vendorId the unique identifier of the vendor, not null
   * @param title notification title, not null
   * @param body notification body/message, not null
   * @return true if the notification was successfully delivered, false otherwise
   */
  boolean sendToVendor(String vendorId, String title, String body);

  /**
   * Send a notification to a client.
   *
   * @param recipientPhone the client's phone number, not null
   * @param title notification title, not null
   * @param body notification body/message, not null
   * @return true if the notification was successfully delivered, false otherwise
   */
  boolean sendToClient(String recipientPhone, String title, String body);
}
