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
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs every request and its response, including requests rejected by Spring Security (it runs
 * before the security filter chain, registered at -100). The correlation ID is put in the logging
 * MDC so every log line of the request, SQL included, carries it.
 *
 * <p>Response lines are INFO for 1xx-3xx, WARN for 4xx and ERROR for 5xx.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class HttpLoggingFilter extends OncePerRequestFilter {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

  /** Client-supplied IDs end up in every log line: allow only short, log-safe values. */
  private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var correlationId = correlationIdOf(request);
    CorrelationIdHolder.set(correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    log.info(
        "{\"eventType\":\"HTTP_REQUEST\",\"correlationId\":\"{}\",\"method\":\"{}\",\"uri\":\"{}\"}",
        correlationId,
        request.getMethod(),
        request.getRequestURI());

    long startTime = System.currentTimeMillis();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      var status = response.getStatus();
      log.atLevel(levelFor(status))
          .log(
              "{\"eventType\":\"HTTP_RESPONSE\",\"correlationId\":\"{}\",\"method\":\"{}\","
                  + "\"uri\":\"{}\",\"status\":{},\"durationMs\":{}}",
              correlationId,
              request.getMethod(),
              request.getRequestURI(),
              status,
              duration);
      CorrelationIdHolder.clear();
    }
  }

  static Level levelFor(int status) {
    if (status >= 500) {
      return Level.ERROR;
    }
    return status >= 400 ? Level.WARN : Level.INFO;
  }

  private static String correlationIdOf(HttpServletRequest request) {
    var header = request.getHeader(CORRELATION_ID_HEADER);
    return header != null && SAFE_CORRELATION_ID.matcher(header).matches()
        ? header
        : UUID.randomUUID().toString();
  }
}
