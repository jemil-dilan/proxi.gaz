/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.springframework.http.HttpStatus.OK;

import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.AdminAuthApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginRequest;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginResponse;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class AdminAuthController implements AdminAuthApi {

  private final RestMapper restMapper;
  private final LoginAdminUseCase loginAdminUseCase;

  @Override
  public ResponseEntity<AdminLoginResponse> adminLogin(AdminLoginRequest request) {
    var result = loginAdminUseCase.execute(restMapper.toCommand(request));
    return ResponseEntity.status(OK).body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<AdminProfileResponse> getAdminProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
      return ResponseEntity.status(401).build();
    }

    var token = jwtAuthentication.getToken();
    var userId = UserId.from(UUID.fromString(token.getSubject()));
    var email = Email.from(token.getClaimAsString("email"));
    return ResponseEntity.ok(restMapper.fromDomain(userId, email));
  }
}
