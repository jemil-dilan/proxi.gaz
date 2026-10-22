/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class OrderStatusTest {

  @ParameterizedTest
  @CsvSource({
    "PENDING,CONFIRMED",
    "PENDING,EXPIRED",
    "PENDING,CANCELLED",
    "PENDING,FAILED",
    "CONFIRMED,IN_PROGRESS",
    "CONFIRMED,CANCELLED",
    "IN_PROGRESS,DELIVERED",
    "IN_PROGRESS,FAILED",
    "DELIVERED,DELIVERED_VERIFIED"
  })
  void canTransitionTo_allowsDocumentedHappyAndFailurePaths(OrderStatus from, OrderStatus to) {
    assertThat(from.canTransitionTo(to)).isTrue();
    assertThat(from.transitionTo(to)).isEqualTo(to);
  }

  @ParameterizedTest
  @CsvSource({
    "PENDING,DELIVERED",
    "PENDING,DELIVERED_VERIFIED",
    "PENDING,IN_PROGRESS",
    "CONFIRMED,PENDING",
    "CONFIRMED,DELIVERED",
    "IN_PROGRESS,PENDING",
    "IN_PROGRESS,CONFIRMED",
    "DELIVERED,PENDING",
    "DELIVERED,IN_PROGRESS",
    "DELIVERED_VERIFIED,DELIVERED",
    "EXPIRED,PENDING",
    "CANCELLED,CONFIRMED",
    "FAILED,IN_PROGRESS"
  })
  void canTransitionTo_rejectsIllegalTransitions(OrderStatus from, OrderStatus to) {
    assertThat(from.canTransitionTo(to)).isFalse();
    assertThatThrownBy(() -> from.transitionTo(to))
        .isInstanceOf(InvalidOrderStatusTransitionException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = OrderStatus.class,
      names = {"DELIVERED_VERIFIED", "EXPIRED", "CANCELLED", "FAILED"})
  void terminalStatuses_haveNoOutgoingTransitions(OrderStatus terminal) {
    for (OrderStatus target : OrderStatus.values()) {
      assertThat(terminal.canTransitionTo(target)).isFalse();
    }
  }

  @Test
  void label_matchesSpecificationFrenchWording() {
    assertThat(OrderStatus.PENDING.label()).isEqualTo("En attente");
    assertThat(OrderStatus.CONFIRMED.label()).isEqualTo("Confirmée");
    assertThat(OrderStatus.IN_PROGRESS.label()).isEqualTo("En cours");
    assertThat(OrderStatus.DELIVERED.label()).isEqualTo("Livrée");
    assertThat(OrderStatus.DELIVERED_VERIFIED.label()).isEqualTo("Livrée & Vérifiée");
    assertThat(OrderStatus.EXPIRED.label()).isEqualTo("Expirée");
    assertThat(OrderStatus.CANCELLED.label()).isEqualTo("Annulée");
    assertThat(OrderStatus.FAILED.label()).isEqualTo("Échouée");
  }

  @Test
  void fromLabel_roundTrips() {
    for (OrderStatus status : OrderStatus.values()) {
      assertThat(OrderStatus.fromLabel(status.label())).isEqualTo(status);
    }
  }
}
