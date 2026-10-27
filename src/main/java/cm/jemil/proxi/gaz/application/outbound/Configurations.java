/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import java.time.Duration;

public interface Configurations {
  Duration accessTokenTtl();

  String secret();

  boolean bootstrapEnabled();

  String bootstrapEmail();

  String bootstrapPassword();
}
