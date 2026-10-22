/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.order;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;

public class InvalidOrderStatusTransitionException extends DomainException {

  public InvalidOrderStatusTransitionException(OrderStatus from, OrderStatus to) {
    super(
        ErrorCodes.PROXIGAZ_400_001,
        "cannot transition from %s to %s".formatted(from.name(), to.name()));
  }
}
