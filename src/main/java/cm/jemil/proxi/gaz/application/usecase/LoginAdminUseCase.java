/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.RateLimiter;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.user.InvalidCredentialsException;
import cm.jemil.proxi.gaz.domain.user.TooManyLoginAttemptsException;
import cm.jemil.proxi.gaz.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginAdminUseCase {

  private static final int MAX_LOGIN_ATTEMPTS = 5;
  private static final int RATE_LIMIT_WINDOW_SECONDS = 900; // 15 minutes

  /**
   * Valid bcrypt hash used when no user/hash exists so password verification always runs (timing).
   * Value does not correspond to any real account password.
   */
  private static final PasswordHash DUMMY_PASSWORD_HASH =
      PasswordHash.from("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG");

  private final UserRepository userRepository;
  private final PasswordVerifier passwordVerifier;
  private final AccessTokenIssuer accessTokenIssuer;
  private final RateLimiter rateLimiter;

  public Response execute(Command command) {
    String key = buildRateLimitKey(command.email(), command.clientIp());

    int retryAfter = rateLimiter.getRetryAfterSeconds(key);
    if (retryAfter > 0) {
      throw new TooManyLoginAttemptsException(retryAfter);
    }

    var user = userRepository.findByEmail(command.email()).filter(User::isAdmin).orElse(null);

    PasswordHash hashToVerify =
        user != null && user.getPasswordHash() != null
            ? user.getPasswordHash()
            : DUMMY_PASSWORD_HASH;
    boolean passwordValid = passwordVerifier.matches(command.password(), hashToVerify);

    if (user == null || !passwordValid) {
      log.warn("Failed login attempt for email: {}", maskEmail(command.email()));
      if (!rateLimiter.allow(key, MAX_LOGIN_ATTEMPTS, RATE_LIMIT_WINDOW_SECONDS)) {
        throw new TooManyLoginAttemptsException(rateLimiter.getRetryAfterSeconds(key));
      }
      throw new InvalidCredentialsException();
    }

    var accessToken = accessTokenIssuer.issue(user.getId(), command.email(), user.scopes());
    log.info("Successful login for admin: {}", maskEmail(command.email()));
    return new Response(accessToken.value(), accessToken.expiresInSeconds());
  }

  /** Build rate limit key from email and IP. */
  private String buildRateLimitKey(Email email, String clientIp) {
    return "login:" + email.value() + ":" + (clientIp != null ? clientIp : "unknown");
  }

  /** Mask email for logging (show only first 3 chars + domain). */
  private String maskEmail(Email email) {
    String value = email.value();
    int atIndex = value.indexOf('@');
    if (atIndex <= 3) {
      return "***@" + (atIndex > 0 ? value.substring(atIndex + 1) : "");
    }
    return value.substring(0, 3) + "***@" + value.substring(atIndex + 1);
  }

  public record Command(Email email, String password, String clientIp) {}

  public record Response(String accessToken, long expiresInSeconds) {}
}
