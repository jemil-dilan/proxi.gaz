/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.usecase.UpdateVendorSessionUseCase;
import cm.jemil.proxi.gaz.application.usecase.ValidateVendorSessionUseCase;
import cm.jemil.proxi.gaz.domain.ErrorCode;
import cm.jemil.proxi.gaz.domain.ErrorCodes;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import cm.jemil.proxi.gaz.interfaces.rest.ProblemDetail;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class VendorSessionAuthenticationFilter extends OncePerRequestFilter {
  private final ValidateVendorSessionUseCase validateVendorSessionUseCase;
  private final UpdateVendorSessionUseCase updateVendorSessionUseCase;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null && authorization.startsWith("Bearer ")) {
      var token = authorization.substring("Bearer ".length()).trim();
      try {
        var result = validateVendorSessionUseCase.execute(token);
        var vendorId = result.vendorId();

        updateVendorSessionUseCase.execute(vendorId);

        var authentication =
            UsernamePasswordAuthenticationToken.authenticated(
                new VendorPrincipal(vendorId.value()),
                null,
                List.of(new SimpleGrantedAuthority(Scopes.VENDOR)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (InvalidVendorSessionException e) {
        SecurityContextHolder.clearContext();
        writeProblem(
            response, HttpStatus.UNAUTHORIZED, ErrorCodes.PROXIGAZ_401_001, e.getMessage());
        return;
      } catch (VendorInactiveException e) {
        SecurityContextHolder.clearContext();
        writeProblem(response, HttpStatus.FORBIDDEN, ErrorCodes.PROXIGAZ_403_002, e.getMessage());
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  private void writeProblem(
      HttpServletResponse response, HttpStatus status, ErrorCode errorCode, String detail)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    var problem =
        ProblemDetail.builder()
            .type("https://api.proxigaz.cm/probs/" + errorCode.value().toLowerCase())
            .title(status.getReasonPhrase())
            .status(status.value())
            .code(errorCode.value())
            .correlationId(CorrelationIdHolder.get())
            .detail(detail)
            .timestamp(Instant.now())
            .build();
    response.getWriter().write(objectMapper.writeValueAsString(problem));
  }
}
