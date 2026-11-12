/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
import cm.jemil.proxi.gaz.domain.shared.Email;
import cm.jemil.proxi.gaz.domain.shared.UserId;
import cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto.AdminProfileDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserControllerTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getAdminProfile_shouldRejectTokenWithoutSubject() {
    var jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("email", "admin@example.test")
            .build();
    var authentication =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("SCOPE_admin")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    var controller = controller();

    assertThat(controller.getAdminProfile().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getAdminProfile_shouldRejectTokenWithMalformedSubject() {
    var jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject("not-a-uuid")
            .claim("email", "admin@example.test")
            .build();
    var authentication =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("SCOPE_admin")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    var controller = controller();

    assertThat(controller.getAdminProfile().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getAdminProfile_shouldRejectTokenWithoutEmail() {
    UUID userId = UUID.randomUUID();
    var jwt =
        Jwt.withTokenValue("test-token").header("alg", "none").subject(userId.toString()).build();
    var authentication =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("SCOPE_admin")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    var controller = controller();

    assertThat(controller.getAdminProfile().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getAdminProfile_shouldRejectTokenWithMalformedEmail() {
    UUID userId = UUID.randomUUID();
    var jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject(userId.toString())
            .claim("email", "not-an-email")
            .build();
    var authentication =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("SCOPE_admin")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    var controller = controller();

    assertThat(controller.getAdminProfile().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getAdminProfile_shouldRejectTokenWithBlankEmail() {
    UUID userId = UUID.randomUUID();
    var jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject(userId.toString())
            .claim("email", "  ")
            .build();
    var authentication =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("SCOPE_admin")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    var controller = controller();

    assertThat(controller.getAdminProfile().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void getAdminProfile_shouldMapRolesFromScopeClaim() {
    UUID userId = UUID.randomUUID();
    var jwt =
        Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject(userId.toString())
            .claim("email", "admin@example.test")
            .claim("scope", "admin")
            .build();
    var authentication =
        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("SCOPE_admin")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    var restMapper = mock(RestMapper.class);
    var expected = mock(AdminProfileDTO.class);
    given(
            restMapper.fromDomain(
                UserId.from(userId), Email.from("admin@example.test"), List.of("admin")))
        .willReturn(expected);

    var controller =
        new UserController(
            restMapper, mock(LoginAdminUseCase.class), mock(HttpServletRequest.class));

    var response = controller.getAdminProfile();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(expected);
    then(restMapper)
        .should()
        .fromDomain(
            eq(UserId.from(userId)), eq(Email.from("admin@example.test")), eq(List.of("admin")));
  }

  private UserController controller() {
    return new UserController(
        mock(RestMapper.class), mock(LoginAdminUseCase.class), mock(HttpServletRequest.class));
  }
}
