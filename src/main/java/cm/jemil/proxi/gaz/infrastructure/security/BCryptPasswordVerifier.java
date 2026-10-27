/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.infrastructure.security;

import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class BCryptPasswordVerifier implements PasswordVerifier {

  private final PasswordEncoder passwordEncoder;

  @Override
  public boolean matches(String rawPassword, PasswordHash passwordHash) {
    return rawPassword != null && passwordEncoder.matches(rawPassword, passwordHash.value());
  }
}
