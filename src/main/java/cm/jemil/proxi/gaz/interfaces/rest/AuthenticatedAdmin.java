/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.InvalidAdminTokenException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** The admin behind the current request: the subject of the admin JWT is the admin's user id. */
final class AuthenticatedAdmin {

  private AuthenticatedAdmin() {}

  static UserId currentAdminId() {
    if (!(SecurityContextHolder.getContext().getAuthentication()
        instanceof JwtAuthenticationToken jwt)) {
      throw new InvalidAdminTokenException();
    }
    var subject = jwt.getToken().getSubject();
    if (subject == null || subject.isBlank()) {
      throw new InvalidAdminTokenException();
    }
    try {
      return UserId.from(UUID.fromString(subject));
    } catch (IllegalArgumentException _) {
      throw new InvalidAdminTokenException();
    }
  }
}
