/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import static cm.jemil.proxi.gaz.domain.ErrorCodes.PROXIGAZ_403_002;

import cm.jemil.proxi.gaz.domain.DomainException;

/** Exception thrown when trying to access an inactive vendor. */
public class VendorInactiveException extends DomainException {
  public VendorInactiveException() {
    super(PROXIGAZ_403_002);
  }
}
