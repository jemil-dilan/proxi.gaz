/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.AdminAuthApi;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.LoginResponseDTO;
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
  public ResponseEntity<LoginResponseDTO> adminLogin(AdminLoginDTO request) {
    var result = loginAdminUseCase.execute(restMapper.toCommand(request));
    return ResponseEntity.status(OK).body(restMapper.fromDomain(result));
  }

  @Override
  public ResponseEntity<AdminProfileDTO> getAdminProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }

    var token = jwtAuthentication.getToken();
    var subject = token.getSubject();
    if (subject == null || subject.isBlank()) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }

    UUID subjectId;
    try {
      subjectId = UUID.fromString(subject);
    } catch (IllegalArgumentException _) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }

    String emailClaim = token.getClaimAsString("email");
    if (emailClaim == null || emailClaim.isBlank()) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }
    var userId = UserId.from(subjectId);

    Email email;
    try {
      email = Email.from(emailClaim);
    } catch (InvalidDomainValueException exception) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(restMapper.fromDomain(userId, email));
  }
}
