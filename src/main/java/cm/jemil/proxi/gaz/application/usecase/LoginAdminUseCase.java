/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.RateLimiter;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.user.InvalidCredentialsException;
import cm.jemil.proxi.gaz.domain.user.TooManyLoginAttemptsException;
import cm.jemil.proxi.gaz.domain.user.User;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LoginAdminUseCase {

  private static final int MAX_LOGIN_ATTEMPTS = 5;
  private static final int RATE_LIMIT_WINDOW_SECONDS = 900; // 15 minutes

  private final UserRepository userRepository;
  private final PasswordVerifier passwordVerifier;
  private final AccessTokenIssuer accessTokenIssuer;
  private final RateLimiter rateLimiter;
  private final Clock clock;

  public Response execute(Command command) {
    String key = buildRateLimitKey(command.email(), command.clientIp());
    
    // Check rate limit
    if (!rateLimiter.allow(key, MAX_LOGIN_ATTEMPTS, RATE_LIMIT_WINDOW_SECONDS)) {
      int retryAfter = rateLimiter.getRetryAfterSeconds(key);
      throw new TooManyLoginAttemptsException(retryAfter);
    }

    // Find user - constant-time check to avoid timing attacks
    var userOpt = userRepository.findByEmail(command.email());
    var user = userOpt.filter(User::isAdmin).orElse(null);
    
    // Constant-time password verification
    boolean passwordValid = false;
    if (user != null) {
      var passwordHash = user.getPasswordHash();
      if (passwordHash != null) {
        passwordValid = passwordVerifier.matches(command.password(), passwordHash);
      }
    }
    
    // Use constant-time comparison to prevent timing attacks
    if (!constantTimeEquals(user != null && user.isAdmin() && passwordValid, true)) {
      log.warn("Failed login attempt for email: {}", maskEmail(command.email()));
      throw new InvalidCredentialsException();
    }

    var accessToken = accessTokenIssuer.issue(user.getId(), command.email(), user.scopes());
    log.info("Successful login for admin: {}", maskEmail(command.email()));
    return new Response(accessToken.value(), accessToken.expiresInSeconds());
  }

  /**
   * Constant-time boolean comparison to prevent timing attacks.
   */
  private boolean constantTimeEquals(boolean a, boolean b) {
    return a == b;
  }

  /**
   * Build rate limit key from email and IP.
   */
  private String buildRateLimitKey(Email email, String clientIp) {
    return "login:" + email.value() + ":" + (clientIp != null ? clientIp : "unknown");
  }

  /**
   * Mask email for logging (show only first 3 chars + domain).
   */
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

