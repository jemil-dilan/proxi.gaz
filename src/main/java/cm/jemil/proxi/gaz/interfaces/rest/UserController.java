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
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api.UserApi;
import java.util.List;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminLoginDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileDTO;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.LoginResponseDTO;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    var command = restMapper.toCommand(request, clientIp);
    var result = loginAdminUseCase.execute(command);
    return ResponseEntity.status(OK).body(restMapper.fromDomain(result));
  }

  private String extractClientIp() {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isBlank()) {
      ip = request.getRemoteAddr();
    }
    // Handle comma-separated list (e.g., "client, proxy1, proxy2")
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
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
    var userId = UserId.from(subjectId);
    
    // Extract roles from scopes claim
    List<String> scopes = token.getClaimAsStringList("scopes");
    List<String> roles = scopes != null ? scopes : List.of();

    Email email;
    try {
      email = Email.from(emailClaim);
    } catch (InvalidDomainValueException _) {
      return ResponseEntity.status(UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(restMapper.fromDomain(userId, email, roles));
  }
}
