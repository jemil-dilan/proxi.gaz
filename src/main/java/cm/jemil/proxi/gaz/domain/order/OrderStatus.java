/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.order;

import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Order lifecycle statuses from the MVP specification (French labels preserved for persistence /
 * UI).
 */
public enum OrderStatus {
  PENDING("En attente"),
  CONFIRMED("Confirmée"),
  IN_PROGRESS("En cours"),
  DELIVERED("Livrée"),
  DELIVERED_VERIFIED("Livrée & Vérifiée"),
  EXPIRED("Expirée"),
  CANCELLED("Annulée"),
  FAILED("Échouée");

  private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED =
      Map.of(
          PENDING, EnumSet.of(CONFIRMED, EXPIRED, CANCELLED, FAILED),
          CONFIRMED, EnumSet.of(IN_PROGRESS, CANCELLED),
          IN_PROGRESS, EnumSet.of(DELIVERED, FAILED),
          DELIVERED, EnumSet.of(DELIVERED_VERIFIED),
          DELIVERED_VERIFIED, EnumSet.noneOf(OrderStatus.class),
          EXPIRED, EnumSet.noneOf(OrderStatus.class),
          CANCELLED, EnumSet.noneOf(OrderStatus.class),
          FAILED, EnumSet.noneOf(OrderStatus.class));

  private final String label;

  OrderStatus(String label) {
    this.label = label;
  }

  public String label() {
    return label;
  }

  public boolean canTransitionTo(OrderStatus target) {
    return ALLOWED.getOrDefault(this, Set.of()).contains(target);
  }

  public OrderStatus transitionTo(OrderStatus target) {
    if (!canTransitionTo(target)) {
      throw new InvalidOrderStatusTransitionException(this, target);
    }
    return target;
  }

  public static OrderStatus fromLabel(String label) {
    for (OrderStatus status : values()) {
      if (status.label.equals(label)) {
        return status;
      }
    }
    throw new InvalidDomainValueException("unknown order status label: " + label);
  }
}
