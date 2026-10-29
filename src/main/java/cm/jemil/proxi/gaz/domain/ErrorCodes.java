/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain;

public enum ErrorCodes implements ErrorCode {
  PROXIGAZ_400_000("PROXIGAZ-400-000", "FIELDS EXTRACTION CODE NOT SUPPORTED"),
  PROXIGAZ_400_001("PROXIGAZ-400-001", "INVALID ORDER STATUS TRANSITION"),
  PROXIGAZ_400_002("PROXIGAZ-400-002", "INVALID DOMAIN VALUE"),
  PROXIGAZ_401_000("PROXIGAZ-401-000", "INVALID CREDENTIALS"),
  PROXIGAZ_404_000("PROXIGAZ-404-000", "DEMO NOT FOUND"),
  PROXIGAZ_404_001("PROXIGAZ-404-001", "USER NOT FOUND"),
  PROXIGAZ_404_002("PROXIGAZ-404-002", "VENDOR NOT FOUND"),
  PROXIGAZ_409_000("PROXIGAZ-409-000", "DEMO ALREADY EXISTS"),
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
