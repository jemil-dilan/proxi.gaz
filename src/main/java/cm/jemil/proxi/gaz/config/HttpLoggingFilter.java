/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs incoming requests and outgoing responses for debugging purposes. Generates a correlation ID
 * per request and includes it in the log output.
 */
@Slf4j
@Component
class HttpLoggingFilter extends OncePerRequestFilter {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String correlationId =
        request.getHeader(CORRELATION_ID_HEADER) != null
            ? request.getHeader(CORRELATION_ID_HEADER)
            : UUID.randomUUID().toString();

    log.info(
        "{{\"eventType\":\"HTTP_REQUEST\",\"correlationId\":\"{}\",\"method\":\"{}\",\"uri\":\"{}\"}}",
        correlationId,
        request.getMethod(),
        request.getRequestURI());

    long startTime = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      log.info(
          "{{\"eventType\":\"HTTP_RESPONSE\",\"correlationId\":\"{}\",\"status\":{},\"durationMs\":{}}}",
          correlationId,
          response.getStatus(),
          duration);
    }
  }
}
