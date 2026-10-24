/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AdminAccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.AdminPasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.AdminUserRepository;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.admin.InvalidAdminCredentialsException;
import cm.jemil.proxi.gaz.domain.shared.Email;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginAdminUseCase {

  private final AdminUserRepository adminUserRepository;
  private final AdminPasswordVerifier adminPasswordVerifier;
  private final AdminAccessTokenIssuer adminAccessTokenIssuer;

  public Result execute(Command command) {
    if (command == null || command.email() == null || command.password() == null) {
      throw new InvalidAdminCredentialsException();
    }

    Email email;
    try {
      email = Email.from(command.email());
    } catch (InvalidDomainValueException exception) {
      throw new InvalidAdminCredentialsException();
    }

    var adminUser =
        adminUserRepository.findByEmail(email).orElseThrow(InvalidAdminCredentialsException::new);
    if (!adminPasswordVerifier.matches(command.password(), adminUser.getPasswordHash())) {
      throw new InvalidAdminCredentialsException();
    }

    var accessToken = adminAccessTokenIssuer.issue(adminUser.getId(), adminUser.getEmail());
    return new Result(accessToken.value(), accessToken.expiresInSeconds());
  }

  public record Command(String email, String password) {}

  public record Result(String accessToken, long expiresInSeconds) {}
}
