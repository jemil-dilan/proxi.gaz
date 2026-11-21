/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HttpLoggingFilterTest {

  private final HttpLoggingFilter filter = new HttpLoggingFilter();

  @Test
  void levelFor_shouldWarnOnClientErrorsAndFailOnServerErrors() {
    assertThat(HttpLoggingFilter.levelFor(200)).isEqualTo(Level.INFO);
    assertThat(HttpLoggingFilter.levelFor(302)).isEqualTo(Level.INFO);
    assertThat(HttpLoggingFilter.levelFor(400)).isEqualTo(Level.WARN);
    assertThat(HttpLoggingFilter.levelFor(401)).isEqualTo(Level.WARN);
    assertThat(HttpLoggingFilter.levelFor(404)).isEqualTo(Level.WARN);
    assertThat(HttpLoggingFilter.levelFor(500)).isEqualTo(Level.ERROR);
    assertThat(HttpLoggingFilter.levelFor(503)).isEqualTo(Level.ERROR);
  }

  @Test
  void doFilter_shouldExposeTheCorrelationIdToLogsDuringTheRequestOnly() throws Exception {
    var request = new MockHttpServletRequest("GET", "/admin/vendors");
    request.addHeader("X-Correlation-Id", "abc-123_x.y");
    var response = new MockHttpServletResponse();
    var seenInMdc = new AtomicReference<String>();
    var seenInHolder = new AtomicReference<String>();
    var chain =
        new MockFilterChain(
            new jakarta.servlet.http.HttpServlet() {
              @Override
              protected void service(
                  jakarta.servlet.http.HttpServletRequest req, HttpServletResponse res) {
                seenInMdc.set(MDC.get("correlationId"));
                seenInHolder.set(CorrelationIdHolder.get());
              }
            });

    filter.doFilter(request, response, chain);

    assertThat(seenInMdc.get()).isEqualTo("abc-123_x.y");
    assertThat(seenInHolder.get()).isEqualTo("abc-123_x.y");
    assertThat(response.getHeader("X-Correlation-Id")).isEqualTo("abc-123_x.y");
    assertThat(MDC.get("correlationId")).isNull();
    assertThat(CorrelationIdHolder.get()).isNull();
  }

  @Test
  void doFilter_shouldReplaceAnUnsafeCorrelationIdHeader() throws Exception {
    var request = new MockHttpServletRequest("GET", "/admin/vendors");
    request.addHeader("X-Correlation-Id", "abc\nFAKE LOG LINE");
    var response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader("X-Correlation-Id"))
        .isNotBlank()
        .doesNotContain("\n", "FAKE")
        .matches("[0-9a-f-]{36}");
  }

  @Test
  void doFilter_shouldReplaceAnOverlongCorrelationIdHeader() throws Exception {
    var request = new MockHttpServletRequest("GET", "/");
    request.addHeader("X-Correlation-Id", "a".repeat(65));
    var response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader("X-Correlation-Id")).matches("[0-9a-f-]{36}");
  }

  @Test
  void filter_shouldRunBeforeSpringSecurity() {
    var order = HttpLoggingFilter.class.getAnnotation(Order.class);

    assertThat(order).isNotNull();
    // Spring Security's filter chain is registered at order -100.
    assertThat(order.value()).isLessThan(-100).isGreaterThan(Ordered.HIGHEST_PRECEDENCE);
  }
}
