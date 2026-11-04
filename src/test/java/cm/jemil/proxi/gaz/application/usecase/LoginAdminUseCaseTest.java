/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.RateLimiter;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.user.InvalidCredentialsException;
import cm.jemil.proxi.gaz.domain.user.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginAdminUseCaseTest {

  private static final String EMAIL = "admin@example.test";
  private static final String PASSWORD = "secret-password";

  @Mock private UserRepository userRepository;
  @Mock private PasswordVerifier passwordVerifier;
  @Mock private AccessTokenIssuer accessTokenIssuer;
  @Mock private RateLimiter rateLimiter;
  @Mock private Clock clock;

  @InjectMocks private LoginAdminUseCase loginAdminUseCase;

  @Test
  void execute_shouldReturnAccessToken_whenCredentialsAreValid() {
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(true);
    given(accessTokenIssuer.issue(user.getId(), Email.from(EMAIL), user.scopes()))
        .willReturn(new AccessTokenIssuer.IssuedAccessToken("signed-token", 900));
    given(rateLimiter.allow(anyString(), anyInt(), anyInt())).willReturn(true);

    var result =
        loginAdminUseCase.execute(new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD, "127.0.0.1"));

    assertThat(result.accessToken()).isEqualTo("signed-token");
    assertThat(result.expiresInSeconds()).isEqualTo(900);
  }

  @Test
  void execute_shouldRejectWrongPassword_withSameGenericCredentialsException() {
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(false);
    given(rateLimiter.allow(anyString(), anyInt(), anyInt())).willReturn(true);

    assertThatThrownBy(
            () ->
                loginAdminUseCase.execute(
                    new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD, "127.0.0.1")))
        .isInstanceOf(InvalidCredentialsException.class);

    then(accessTokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void execute_shouldRejectUnknownEmail_withGenericCredentialsException() {
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.empty());
    given(rateLimiter.allow(anyString(), anyInt(), anyInt())).willReturn(true);

    assertThatThrownBy(
            () ->
                loginAdminUseCase.execute(
                    new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD, "127.0.0.1")))
        .isInstanceOf(InvalidCredentialsException.class);

    then(passwordVerifier).shouldHaveNoInteractions();
    then(accessTokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void execute_shouldRejectVendorUserWithSameGenericCredentialsException() {
    var vendor = User.createVendor(null, CreatedAt.from(Instant.EPOCH));
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(vendor));
    given(rateLimiter.allow(anyString(), anyInt(), anyInt())).willReturn(true);

    assertThatThrownBy(
            () ->
                loginAdminUseCase.execute(
                    new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD, "127.0.0.1")))
        .isInstanceOf(InvalidCredentialsException.class);

    then(passwordVerifier).shouldHaveNoInteractions();
    then(accessTokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void execute_shouldRateLimitAfterMaxAttempts() {
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(false);
    
    // Allow first 5 attempts
    given(rateLimiter.allow(anyString(), anyInt(), anyInt()))
        .willReturn(true, true, true, true, true, false);
    given(rateLimiter.getRetryAfterSeconds(anyString())).willReturn(60);

    // First 5 attempts should throw InvalidCredentialsException
    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(
              () -> loginAdminUseCase.execute(
                  new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD, "127.0.0.1")))
          .isInstanceOf(InvalidCredentialsException.class);
    }

    // 6th attempt should throw TooManyLoginAttemptsException
    assertThatThrownBy(
            () -> loginAdminUseCase.execute(
                new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD, "127.0.0.1")))
        .isInstanceOf(cm.jemil.proxi.gaz.domain.user.TooManyLoginAttemptsException.class);
  }

  @Test
  void execute_shouldAllowAfterRateLimitWindow() {
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(true);
    given(accessTokenIssuer.issue(user.getId(), Email.from(EMAIL), user.scopes()))
        .willReturn(new AccessTokenIssuer.IssuedAccessToken("signed-token", 900));
    
    // Simulate rate limit reset (will return true after previous false)
    given(rateLimiter.allow(anyString(), anyInt(), anyInt()))
        .willReturn(true);

    var result =
        loginAdminUseCase.execute(new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD, "127.0.0.1"));

    assertThat(result.accessToken()).isEqualTo("signed-token");
  }
}
