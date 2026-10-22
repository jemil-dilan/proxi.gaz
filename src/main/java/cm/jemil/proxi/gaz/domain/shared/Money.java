/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.shared;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;

/** Amount in FCFA stored as a non-negative integer (no fractional currency unit). */
public record Money(int amountFcfa) {

  public Money {
    if (amountFcfa < 0) {
      throw new InvalidDomainValueException("money amount must be >= 0 FCFA");
    }
  }

  public static Money fcfa(int amountFcfa) {
    return new Money(amountFcfa);
  }

  public Money add(Money other) {
    return fcfa(Math.addExact(this.amountFcfa, other.amountFcfa));
  }
}
