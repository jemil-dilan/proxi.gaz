/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cm.jemil.proxi.gaz.application.usecase.LoginAdminUseCase;
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

  private UserController controller() {
    return new UserController(mock(RestMapper.class), mock(LoginAdminUseCase.class));
  }
}
