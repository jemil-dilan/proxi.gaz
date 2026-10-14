/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain;

import static cm.jemil.proxi.gaz.domain.ErrorCodes.PROXIGAZ_400_000;

public class FieldsExtractionCodeNotSupported extends DomainException {

  public FieldsExtractionCodeNotSupported() {
    super(PROXIGAZ_400_000);
  }
}
