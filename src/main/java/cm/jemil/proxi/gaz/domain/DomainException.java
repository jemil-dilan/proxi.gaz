package cm.jemil.proxi.gaz.domain;

public class DomainException extends RuntimeException {

  private final ErrorCode errorCode;

  public DomainException(ErrorCode errorCode) {
    super("%s: %s".formatted(errorCode.value(), errorCode.description()));
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
