/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import java.time.Duration;

/** Application-facing view of the configured JWT signing settings. */
public interface JwtSettings {

  String signingSecret();

  Duration accessTokenTtl();
}
