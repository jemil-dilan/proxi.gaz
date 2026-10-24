/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.AdminPasswordVerifier;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptAdminPasswordVerifier implements AdminPasswordVerifier {

  private final PasswordEncoder passwordEncoder;

  @Override
  public boolean matches(String rawPassword, PasswordHash passwordHash) {
    return rawPassword != null && passwordEncoder.matches(rawPassword, passwordHash.value());
  }
}
