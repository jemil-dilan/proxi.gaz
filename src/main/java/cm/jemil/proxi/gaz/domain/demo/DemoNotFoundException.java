/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.demo;

import static cm.jemil.proxi.gaz.domain.ErrorCodes.PROXIGAZ_404_000;

import cm.jemil.proxi.gaz.domain.DomainException;

public class DemoNotFoundException extends DomainException {

  public DemoNotFoundException() {
    super(PROXIGAZ_404_000);
  }
}
