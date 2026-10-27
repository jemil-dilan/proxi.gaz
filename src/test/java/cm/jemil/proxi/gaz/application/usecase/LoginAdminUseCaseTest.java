/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.domain.admin.AdminUser;
import cm.jemil.proxi.gaz.domain.admin.InvalidAdminCredentialsException;
import cm.jemil.proxi.gaz.domain.shared.CreatedAt;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.PasswordHash;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginAdminUseCaseTest {

  private static final String EMAIL = "admin@example.test";
  private static final String PASSWORD = "secret-password";

  @Mock private AdminUserRepository adminUserRepository;
  @Mock private PasswordVerifier passwordVerifier;
  @Mock private AccessTokenIssuer accessTokenIssuer;

  @InjectMocks private LoginAdminUseCase loginAdminUseCase;

  @Test
  void execute_shouldReturnAccessToken_whenCredentialsAreValid() {
    var adminUser =
        AdminUser.of(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(adminUserRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(adminUser));
    given(passwordVerifier.matches(PASSWORD, adminUser.getPasswordHash())).willReturn(true);
    given(accessTokenIssuer.issue(adminUser.getId(), adminUser.getEmail(), Set.of("admin")))
        .willReturn(new AccessTokenIssuer.IssuedAccessToken("signed-token", 900));

    var result =
        loginAdminUseCase.execute(new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD));

    assertThat(result.accessToken()).isEqualTo("signed-token");
    assertThat(result.expiresInSeconds()).isEqualTo(900);
  }

  @Test
  void execute_shouldRejectWrongPassword_withSameGenericCredentialsException() {
    var adminUser =
        AdminUser.of(
            Email.from(EMAIL),
            PasswordHash.from("$2a$10$test-hash"),
            CreatedAt.from(Instant.EPOCH));
    given(adminUserRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.of(adminUser));
    given(passwordVerifier.matches(PASSWORD, adminUser.getPasswordHash())).willReturn(false);

    assertThatThrownBy(
            () ->
                loginAdminUseCase.execute(
                    new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD)))
        .isInstanceOf(InvalidAdminCredentialsException.class);

    then(accessTokenIssuer).shouldHaveNoInteractions();
  }

  @Test
  void execute_shouldRejectUnknownEmail_withGenericCredentialsException() {
    given(adminUserRepository.findByEmail(Email.from(EMAIL))).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                loginAdminUseCase.execute(
                    new LoginAdminUseCase.Command(Email.from(EMAIL), PASSWORD)))
        .isInstanceOf(InvalidAdminCredentialsException.class);

    then(passwordVerifier).shouldHaveNoInteractions();
    then(accessTokenIssuer).shouldHaveNoInteractions();
  }
}
