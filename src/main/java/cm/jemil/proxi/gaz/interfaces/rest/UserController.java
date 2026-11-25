/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.springframework.http.HttpStatus.OK;

import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.user.InvalidAdminTokenException;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.UserApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.LoginResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class UserController implements UserApi {

  private final RestMapper restMapper;
  private final LoginAdminUseCase loginAdminUseCase;
  private final HttpServletRequest request;

  @Override
  public ResponseEntity<LoginResponseDTO> adminLogin(AdminLoginDTO request) {
    String clientIp = extractClientIp();
    var result = loginAdminUseCase.execute(restMapper.toCommand(request, clientIp));
    return ResponseEntity.status(OK).body(restMapper.fromDomain(result));
  }

  /**
   * A token that is valid but does not describe an admin (missing or malformed subject or email)
   * gets the same 401 problem response as every other admin endpoint ({@code PROXIGAZ-401-001}).
   */
  @Override
  public ResponseEntity<AdminProfileDTO> getAdminProfile() {
    var admin = AuthenticatedAdmin.current();
    var userId = admin.id();
    var token = admin.token();

    // JWT stores space-separated scopes in the standard "scope" claim
    String scopeClaim = token.getClaimAsString("scope");
    List<String> roles =
        scopeClaim == null || scopeClaim.isBlank()
            ? List.of()
            : List.of(scopeClaim.trim().split("\\s+"));

    Email email;
    try {
      email = Email.from(token.getClaimAsString("email"));
    } catch (InvalidDomainValueException _) {
      throw new InvalidAdminTokenException();
    }
    return ResponseEntity.ok(restMapper.fromDomain(userId, email, roles));
  }

  private String extractClientIp() {
    // Use the direct remote address only until a trusted-proxy / X-Forwarded-For
    // allowlist is configured; clients must not be able to spoof the lockout key.
    return request.getRemoteAddr();
  }
}
