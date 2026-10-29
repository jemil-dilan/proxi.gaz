/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.user;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

/** Generic authentication failure; deliberately does not reveal whether an account exists. */
public class InvalidCredentialsException extends DomainException {
  public InvalidCredentialsException() {
    super(ErrorCodes.PROXIGAZ_401_000);
  }
}
