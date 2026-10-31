/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.usecase.ValidateVendorSessionUseCase;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class VendorSessionAuthenticationFilter extends OncePerRequestFilter {
  private final ValidateVendorSessionUseCase validateVendorSessionUseCase;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization != null && authorization.startsWith("Bearer ")) {
      var token = authorization.substring("Bearer ".length()).trim();
      try {
        var result = validateVendorSessionUseCase.execute(token);
        var authentication =
            UsernamePasswordAuthenticationToken.authenticated(
                new VendorPrincipal(result.vendorId().value()),
                null,
                List.of(new SimpleGrantedAuthority(Scopes.VENDOR)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (InvalidVendorSessionException exception) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(request, response);
  }
}
