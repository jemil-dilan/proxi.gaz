/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.GeoQueryPort;
import cm.jemil.proxi.gaz.application.outbound.NotificationChannel;
import cm.jemil.proxi.gaz.application.outbound.OtpProvider;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.infrastructure.geo.InMemoryGeoQueryPort;
import cm.jemil.proxi.gaz.infrastructure.notifications.LoggingNotificationChannel;
import cm.jemil.proxi.gaz.infrastructure.otp.NullOtpProvider;
import cm.jemil.proxi.gaz.infrastructure.security.SecureVendorSessionTokenGenerator;
import cm.jemil.proxi.gaz.infrastructure.security.Sha256VendorSessionTokenHasher;
import cm.jemil.proxi.gaz.infrastructure.time.FakeClock;
import cm.jemil.proxi.gaz.infrastructure.time.SystemClock;
import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Central Spring configuration for all infrastructure adapters. Declares beans for Clock,
 * OtpProvider, NotificationChannel, and GeoQueryPort.
 */
@Configuration
public class InfrastructureBeans {

  // ==================== Clock Beans ====================

  @Bean
  @Profile({"local", "staging", "production"})
  Clock clock() {
    return new SystemClock();
  }

  @Bean
  @Profile({"test", "data_test", "e2e"})
  Clock fakeClock() {
    return new FakeClock(Instant.now());
  }

  // ==================== OTP Provider Beans ====================

  @Bean
  OtpProvider otpProvider() {
    return new NullOtpProvider();
  }

  // ==================== Notification Channel Beans ====================

  @Bean
  NotificationChannel notificationChannel() {
    return new LoggingNotificationChannel();
  }

  @Bean
  VendorSessionTokenGenerator vendorSessionTokenGenerator() {
    return new SecureVendorSessionTokenGenerator(new SecureRandom());
  }

  @Bean
  VendorSessionTokenHasher vendorSessionTokenHasher() {
    return new Sha256VendorSessionTokenHasher();
  }

  // ==================== Geo Query Port Beans ====================

  @Bean
  @Profile("test")
  GeoQueryPort geoQueryPort() {
    return new InMemoryGeoQueryPort();
  }
}
