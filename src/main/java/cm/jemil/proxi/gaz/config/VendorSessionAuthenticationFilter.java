/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import cm.jemil.proxi.gaz.application.outbound.Clock;
import cm.jemil.proxi.gaz.application.outbound.VendorSessionRepository;
import cm.jemil.proxi.gaz.application.usecase.ValidateVendorSessionUseCase;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.VendorInactiveException;
import cm.jemil.proxi.gaz.domain.shared.LastUsedAt;
import java.util.UUID;
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
  private final VendorSessionRepository vendorSessionRepository;
  private final Clock clock;

  // Simple in-memory throttling: vendorId -> last update timestamp (in ms)
  // This is per-instance; for clustered deployments, use a distributed cache
  private final java.util.Map<UUID, Long> lastUpdateMap = new java.util.concurrent.ConcurrentHashMap<>();
  private static final long THROTTLE_MS = 60_000; // 1 minute

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
        
        // Update last_used_at (throttled to once per minute)
        var now = clock.now();
        var lastUpdate = lastUpdateMap.getOrDefault(vendorId.value(), 0L);
        if (now.toEpochMilli() - lastUpdate >= THROTTLE_MS) {
          vendorSessionRepository.updateLastUsedAt(vendorId, LastUsedAt.from(now));
          lastUpdateMap.put(vendorId.value(), now.toEpochMilli());
        }
        
        var authentication =
            UsernamePasswordAuthenticationToken.authenticated(
                new VendorPrincipal(vendorId.value()),
                null,
                List.of(new SimpleGrantedAuthority(Scopes.VENDOR)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (InvalidVendorSessionException exception) {
        SecurityContextHolder.clearContext();
      }
      // VendorInactiveException is a DomainException and will be handled by DomainExceptionTranslator
    }
    filterChain.doFilter(request, response);
  }
}
