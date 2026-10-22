/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain;

public class InvalidDomainValueException extends DomainException {

  public InvalidDomainValueException(String detail) {
    super(ErrorCodes.PROXIGAZ_400_002, detail);
  }
}
