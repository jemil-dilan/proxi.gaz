/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain;

public class DomainException extends RuntimeException {

  private final ErrorCode errorCode;

  public DomainException(ErrorCode errorCode) {
    super("%s: %s".formatted(errorCode.value(), errorCode.description()));
    this.errorCode = errorCode;
  }

  public DomainException(ErrorCode errorCode, String detail) {
    super("%s: %s (%s)".formatted(errorCode.value(), errorCode.description(), detail));
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
