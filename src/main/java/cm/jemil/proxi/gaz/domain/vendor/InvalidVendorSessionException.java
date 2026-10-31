/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

public class InvalidVendorSessionException extends DomainException {
  public InvalidVendorSessionException() {
    super(ErrorCodes.PROXIGAZ_401_001);
  }
}
