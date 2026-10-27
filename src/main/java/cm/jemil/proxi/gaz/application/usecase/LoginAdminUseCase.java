/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.domain.admin.InvalidAdminCredentialsException;
import cm.jemil.proxi.gaz.domain.shared.Email;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginAdminUseCase {

  private final AdminUserRepository adminUserRepository;
  private final PasswordVerifier passwordVerifier;
  private final AccessTokenIssuer accessTokenIssuer;

  public Response execute(Command command) {
    var adminUser =
        adminUserRepository
            .findByEmail(command.email())
            .orElseThrow(InvalidAdminCredentialsException::new);
    if (!passwordVerifier.matches(command.password(), adminUser.getPasswordHash())) {
      throw new InvalidAdminCredentialsException();
    }

    var accessToken =
        accessTokenIssuer.issue(adminUser.getId(), adminUser.getEmail(), Set.of("admin"));
    return new Response(accessToken.value(), accessToken.expiresInSeconds());
  }

  public record Command(Email email, String password) {}

  public record Response(String accessToken, long expiresInSeconds) {}
}
