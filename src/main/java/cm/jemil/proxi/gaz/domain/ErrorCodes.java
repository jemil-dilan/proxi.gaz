package cm.jemil.proxi.gaz.domain;

public enum ErrorCodes implements ErrorCode {
  PROXIGAZ_400_000("PROXIGAZ-400-000", "FIELDS EXTRACTION CODE NOT SUPPORTED"),
  PROXIGAZ_404_000("PROXIGAZ-404-000", "DEMO NOT FOUND"),
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
