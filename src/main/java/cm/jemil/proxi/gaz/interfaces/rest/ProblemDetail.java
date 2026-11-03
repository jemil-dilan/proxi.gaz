/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;

/**
 * RFC 7807 Problem Details response structure.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7807">RFC 7807</a>
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetail(
    @JsonProperty("type") String type,
    @JsonProperty("title") String title,
    @JsonProperty("status") Integer status,
    @JsonProperty("code") String code,
    @JsonProperty("correlationId") String correlationId,
    @JsonProperty("detail") String detail,
    @JsonProperty("instance") String instance,
    @JsonProperty("timestamp") Instant timestamp,
    @JsonProperty("errors") List<Map<String, String>> errors) {

  public static ProblemDetailBuilder builder() {
    return new ProblemDetailBuilder();
  }

  public static class ProblemDetailBuilder {
    private String type;
    private String title;
    private Integer status;
    private String code;
    private String correlationId;
    private String detail;
    private String instance;
    private Instant timestamp;
    private List<Map<String, String>> errors;

    public ProblemDetailBuilder type(String type) {
      this.type = type;
      return this;
    }

    public ProblemDetailBuilder title(String title) {
      this.title = title;
      return this;
    }

    public ProblemDetailBuilder status(Integer status) {
      this.status = status;
      return this;
    }

    public ProblemDetailBuilder code(String code) {
      this.code = code;
      return this;
    }

    public ProblemDetailBuilder correlationId(String correlationId) {
      this.correlationId = correlationId;
      return this;
    }

    public ProblemDetailBuilder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public ProblemDetailBuilder instance(String instance) {
      this.instance = instance;
      return this;
    }

    public ProblemDetailBuilder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public ProblemDetailBuilder errors(List<Map<String, String>> errors) {
      this.errors = errors;
      return this;
    }

    public ProblemDetail build() {
      return new ProblemDetail(
          type, title, status, code, correlationId, detail, instance, timestamp, errors);
    }
  }
}
