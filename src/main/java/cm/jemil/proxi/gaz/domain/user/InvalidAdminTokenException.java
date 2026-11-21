/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.user;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

/** The admin token is valid but does not identify an admin user (subject missing or not a UUID). */
public class InvalidAdminTokenException extends DomainException {
  public InvalidAdminTokenException() {
    super(ErrorCodes.PROXIGAZ_401_001);
  }
}
