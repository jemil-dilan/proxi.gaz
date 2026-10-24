/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.e2e;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatusCode;

@Scope(SCOPE_CUCUMBER_GLUE)
public class HttpClient {

  public record LastRequestResponse(HttpStatusCode status, String errorCode) {}

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String scopes = "";
  private LastRequestResponse lastResponse;
  private String lastResponseBody = "";
  private String accessToken;

  public UUID lastUpdatedId;

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes == null ? "" : scopes;
  }

  public String authorizationHeader() {
    return accessToken == null ? null : "Bearer " + accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public void clearAccessToken() {
    accessToken = null;
  }

  public LastRequestResponse getLastResponse() {
    return lastResponse;
  }

  public void setLastResponse(LastRequestResponse lastResponse) {
    this.lastResponse = lastResponse;
  }

  public void recordResponse(int statusCode, String body) {
    lastResponse =
        new LastRequestResponse(HttpStatusCode.valueOf(statusCode), extractErrorCode(body));
    lastResponseBody = body == null ? "" : body;
  }

  public String getLastResponseBody() {
    return lastResponseBody;
  }

  private static String extractErrorCode(String body) {
    if (body == null || body.isBlank()) {
      return "";
    }
    try {
      JsonNode node = MAPPER.readTree(body);
      JsonNode errorCode = node.get("errorCode");
      return errorCode == null || errorCode.isNull() ? "" : errorCode.asText();
    } catch (Exception _) {
      return "";
    }
  }
}
