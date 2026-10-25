/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.vendor;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

public class VendorNotFoundException extends DomainException {
  public VendorNotFoundException() {
    super(ErrorCodes.PROXIGAZ_404_001);
  }
}
