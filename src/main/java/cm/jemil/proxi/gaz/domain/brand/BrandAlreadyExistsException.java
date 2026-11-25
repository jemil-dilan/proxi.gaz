/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.brand;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

/** A brand with the same name, ignoring case and spacing, is already in the catalogue. */
public class BrandAlreadyExistsException extends DomainException {
  public BrandAlreadyExistsException() {
    super(ErrorCodes.PROXIGAZ_409_003);
  }
}
