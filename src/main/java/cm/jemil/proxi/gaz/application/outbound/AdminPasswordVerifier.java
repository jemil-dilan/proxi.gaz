/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.outbound;

import cm.jemil.proxi.gaz.domain.shared.PasswordHash;

public interface AdminPasswordVerifier {

  boolean matches(String rawPassword, PasswordHash passwordHash);
}
