/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.demo;

import static cm.jemil.proxi.gaz.domain.ErrorCodes.PROXIGAZ_409_000;

import cm.jemil.proxi.gaz.domain.DomainException;

public class DemoAlreadyExistsException extends DomainException {

  public DemoAlreadyExistsException() {
    super(PROXIGAZ_409_000);
  }
}
