/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.admin;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

public class InvalidAdminCredentialsException extends DomainException {

  public InvalidAdminCredentialsException() {
    super(ErrorCodes.PROXIGAZ_401_000);
  }
}
