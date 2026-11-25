/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.domain.user.InvalidAdminTokenException;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** The admin behind the current request: the subject of the admin JWT is the admin's user id. */
final class AuthenticatedAdmin {

  private AuthenticatedAdmin() {}

  /** The admin's user id and the token it came from. */
  record AdminIdentity(UserId id, Jwt token) {}

  static UserId currentAdminId() {
    return current().id();
  }

  /**
   * One null-safe lookup: a missing authentication, a non-JWT authentication, or a subject that is
   * not a user id all throw {@link InvalidAdminTokenException} (401 {@code PROXIGAZ-401-001}).
   */
  static AdminIdentity current() {
    if (!(SecurityContextHolder.getContext().getAuthentication()
        instanceof JwtAuthenticationToken authentication)) {
      throw new InvalidAdminTokenException();
    }
    var token = authentication.getToken();
    var subject = token.getSubject();
    if (subject == null || subject.isBlank()) {
      throw new InvalidAdminTokenException();
    }
    try {
      return new AdminIdentity(UserId.from(UUID.fromString(subject)), token);
    } catch (IllegalArgumentException _) {
      throw new InvalidAdminTokenException();
    }
  }
}
