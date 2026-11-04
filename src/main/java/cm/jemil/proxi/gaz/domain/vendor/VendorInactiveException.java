/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

/**
 * Exception thrown when trying to access an inactive vendor.
 */
public class VendorInactiveException extends DomainException {
  public VendorInactiveException(VendorId vendorId) {
    super(ErrorCodes.PROXIGAZ_403_002, "Vendor " + vendorId.value() + " is inactive");
  }
}
