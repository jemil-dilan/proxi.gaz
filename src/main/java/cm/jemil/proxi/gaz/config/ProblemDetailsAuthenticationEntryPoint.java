/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.domain.ErrorCodes;
import cm.jemil.proxi.gaz.interfaces.rest.ProblemDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

/**
 * Custom authentication entry point that returns RFC 7807 Problem Details instead of a bare HTTP
 * status code.
 */
@RequiredArgsConstructor
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    var problem =
        ProblemDetail.builder()
            .type("https://api.proxigaz.cm/probs/proxigaz-401-000")
            .title("Unauthorized")
            .status(HttpStatus.UNAUTHORIZED.value())
            .code(ErrorCodes.PROXIGAZ_401_000.value())
            .correlationId(CorrelationIdHolder.get())
            .detail("Authentication required: " + authException.getMessage())
            .timestamp(Instant.now())
            .build();

    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.getWriter().write(objectMapper.writeValueAsString(problem));
  }
}
