/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.RateLimiter;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase.Command;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import cm.jemil.proxi.gaz.domain.user.InvalidCredentialsException;
import cm.jemil.proxi.gaz.domain.user.TooManyLoginAttemptsException;
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

  @InjectMocks private LoginAdminUseCase loginAdminUseCase;

  @Test
  void execute_shouldReturnAccessToken_whenCredentialsAreValid() {
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(rateLimiter.getRetryAfterSeconds(anyString())).willReturn(0);
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(true);
    given(accessTokenIssuer.issue(user.getId(), Email.from(EMAIL), user.scopes()))
        .willReturn(new AccessTokenIssuer.IssuedAccessToken("signed-token", 900));

    var result = loginAdminUseCase.execute(new Command(Email.from(EMAIL), PASSWORD, "127.0.0.1"));

    assertThat(result.accessToken()).isEqualTo("signed-token");
    assertThat(result.expiresInSeconds()).isEqualTo(900);
    then(rateLimiter).should(never()).allow(anyString(), anyInt(), anyInt());
  }

  @Test
  void execute_shouldRejectWrongPassword_withSameGenericCredentialsException() {
    Command command = new Command(Email.from(EMAIL), PASSWORD, "127.0.0.1");
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(rateLimiter.getRetryAfterSeconds(anyString())).willReturn(0);
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(false);
    given(rateLimiter.allow(anyString(), anyInt(), anyInt())).willReturn(true);

    assertThatThrownBy(() -> loginAdminUseCase.execute(command))
        .isInstanceOf(InvalidCredentialsException.class);

    then(accessTokenIssuer).shouldHaveNoInteractions();
    then(rateLimiter).should().allow(anyString(), eq(5), eq(900));
  }

  @Test
  void execute_shouldRejectUnknownEmail_withGenericCredentialsException() {
    Command command = new Command(Email.from(EMAIL), PASSWORD, "127.0.0.1");
    given(rateLimiter.getRetryAfterSeconds(anyString())).willReturn(0);
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.empty());
    given(passwordVerifier.matches(eq(PASSWORD), any(PasswordHash.class))).willReturn(false);
    given(rateLimiter.allow(anyString(), anyInt(), anyInt())).willReturn(true);

    assertThatThrownBy(() -> loginAdminUseCase.execute(command))
        .isInstanceOf(InvalidCredentialsException.class);

    then(passwordVerifier).should().matches(eq(PASSWORD), any(PasswordHash.class));
    then(accessTokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void execute_shouldRejectVendorUserWithSameGenericCredentialsException() {
    Command command = new Command(Email.from(EMAIL), PASSWORD, "127.0.0.1");
    var vendor = User.createVendor(null, CreatedAt.from(Instant.EPOCH));
    given(rateLimiter.getRetryAfterSeconds(anyString())).willReturn(0);
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(vendor));
    given(passwordVerifier.matches(eq(PASSWORD), any(PasswordHash.class))).willReturn(false);
    given(rateLimiter.allow(anyString(), anyInt(), anyInt())).willReturn(true);

    assertThatThrownBy(() -> loginAdminUseCase.execute(command))
        .isInstanceOf(InvalidCredentialsException.class);

    then(passwordVerifier).should().matches(eq(PASSWORD), any(PasswordHash.class));
    then(accessTokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void execute_shouldRateLimitAfterMaxFailures() {
    Command command = new Command(Email.from(EMAIL), PASSWORD, "127.0.0.1");
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(false);
    given(rateLimiter.getRetryAfterSeconds(anyString())).willReturn(0, 0, 0, 0, 0, 60);
    given(rateLimiter.allow(anyString(), anyInt(), anyInt()))
        .willReturn(true, true, true, true, true);

    for (int i = 0; i < 5; i++) {
      assertThatThrownBy(() -> loginAdminUseCase.execute(command))
          .isInstanceOf(InvalidCredentialsException.class);
    }

    assertThatThrownBy(() -> loginAdminUseCase.execute(command))
        .isInstanceOf(TooManyLoginAttemptsException.class)
        .extracting(ex -> ((TooManyLoginAttemptsException) ex).getRetryAfterSeconds())
        .isEqualTo(60);
  }

  @Test
  void execute_shouldAllowAfterRateLimitWindow() {
    var user =
        User.createAdmin(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(rateLimiter.getRetryAfterSeconds(anyString())).willReturn(0);
    given(userRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(user));
    given(passwordVerifier.matches(PASSWORD, user.getPasswordHash())).willReturn(true);
    given(accessTokenIssuer.issue(user.getId(), Email.from(EMAIL), user.scopes()))
        .willReturn(new AccessTokenIssuer.IssuedAccessToken("signed-token", 900));

    var result = loginAdminUseCase.execute(new Command(Email.from(EMAIL), PASSWORD, "127.0.0.1"));

    assertThat(result.accessToken()).isEqualTo("signed-token");
  }
}
