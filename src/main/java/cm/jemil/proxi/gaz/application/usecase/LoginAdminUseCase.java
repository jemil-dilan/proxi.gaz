/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.application.usecase;

import cm.jemil.proxi.gaz.application.outbound.AccessTokenIssuer;
import cm.jemil.proxi.gaz.application.outbound.PasswordVerifier;
import cm.jemil.proxi.gaz.application.outbound.UserRepository;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.user.InvalidCredentialsException;
import cm.jemil.proxi.gaz.domain.user.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginAdminUseCase {

  private final UserRepository userRepository;
  private final PasswordVerifier passwordVerifier;
  private final AccessTokenIssuer accessTokenIssuer;

  public Response execute(Command command) {
    var user =
        userRepository
            .findByEmail(command.email())
            .filter(User::isAdmin)
            .orElseThrow(InvalidCredentialsException::new);
    var passwordHash =
        Optional.ofNullable(user.getPasswordHash()).orElseThrow(InvalidCredentialsException::new);
    if (!passwordVerifier.matches(command.password(), passwordHash)) {
      throw new InvalidCredentialsException();
    }

    var accessToken = accessTokenIssuer.issue(user.getId(), command.email(), user.scopes());
    return new Response(accessToken.value(), accessToken.expiresInSeconds());
  }

  public record Command(Email email, String password) {}

  public record Response(String accessToken, long expiresInSeconds) {}
}
