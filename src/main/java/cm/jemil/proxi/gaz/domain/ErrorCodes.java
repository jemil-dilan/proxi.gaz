/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain;

public enum ErrorCodes implements ErrorCode {
  // 400 Bad Request
  PROXIGAZ_400_000("PROXIGAZ-400-000", "FIELDS EXTRACTION CODE NOT SUPPORTED"),
  PROXIGAZ_400_001("PROXIGAZ-400-001", "INVALID ORDER STATUS TRANSITION"),
  PROXIGAZ_400_002("PROXIGAZ-400-002", "VALIDATION FAILED"),
  
  // 401 Unauthorized
  PROXIGAZ_401_000("PROXIGAZ-401-000", "INVALID CREDENTIALS"),
  PROXIGAZ_401_001("PROXIGAZ-401-001", "TOKEN INVALID"),
  PROXIGAZ_401_002("PROXIGAZ-401-002", "VENDOR INACTIVE"),
  
  // 403 Forbidden
  PROXIGAZ_403_001("PROXIGAZ-403-001", "FORBIDDEN AUDIENCE"),
  PROXIGAZ_403_002("PROXIGAZ-403-002", "VENDOR INACTIVE"),
  
  // 404 Not Found
  PROXIGAZ_404_000("PROXIGAZ-404-000", "DEMO NOT FOUND"),
  PROXIGAZ_404_001("PROXIGAZ-404-001", "USER NOT FOUND"),
  PROXIGAZ_404_002("PROXIGAZ-404-002", "VENDOR NOT FOUND"),
  
  // 409 Conflict
  PROXIGAZ_409_000("PROXIGAZ-409-000", "DEMO ALREADY EXISTS"),
  PROXIGAZ_409_001("PROXIGAZ-409-001", "DUPLICATE PHONE"),
  PROXIGAZ_409_002("PROXIGAZ-409-002", "DUPLICATE EMAIL"),
  
  // 429 Too Many Requests
  PROXIGAZ_429_001("PROXIGAZ-429-001", "RATE LIMITED"),
  
  // 500 Internal Server Error
  PROXIGAZ_500_000("PROXIGAZ-500-000", "INTERNAL SERVER ERROR"),
  ;

  private final String value;
  private final String description;

  ErrorCodes(String value, String description) {
    this.value = value;
    this.description = description;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public String description() {
    return description;
  }
}
