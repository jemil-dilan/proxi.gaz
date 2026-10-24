/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.admin.InvalidAdminCredentialsException;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import cm.jemil.proxi.gaz.domain.order.InvalidOrderStatusTransitionException;
import cm.jemil.proxi.gaz.domain.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DomainExceptionTranslatorTest {

  private final DomainExceptionTranslator translator = new DomainExceptionTranslator();

  @Test
  void handleDomainException_shouldReturnNotFound_forDemoNotFound() {
    // When
    var response = translator.handleDomainException(new DemoNotFoundException());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("errorCode", "PROXIGAZ-404-000");
    assertThat(response.getBody()).containsKey("message");
  }

  @Test
  void handleDomainException_shouldReturnConflict_forDemoAlreadyExists() {
    // When
    var response = translator.handleDomainException(new DemoAlreadyExistsException());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).containsEntry("errorCode", "PROXIGAZ-409-000");
  }

  @Test
  void handleDomainException_shouldReturnUnauthorized_forInvalidAdminCredentials() {
    var response = translator.handleDomainException(new InvalidAdminCredentialsException());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).containsEntry("errorCode", "PROXIGAZ-401-000");
  }

  @Test
  void handleDomainException_shouldReturnBadRequest_forUnsupportedExtractionCode() {
    // When
    var response = translator.handleDomainException(new FieldsExtractionCodeNotSupported());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("errorCode", "PROXIGAZ-400-000");
  }

  @Test
  void handleDomainException_shouldReturnBadRequest_forInvalidOrderStatusTransition() {
    var response =
        translator.handleDomainException(
            new InvalidOrderStatusTransitionException(OrderStatus.PENDING, OrderStatus.DELIVERED));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("errorCode", "PROXIGAZ-400-001");
  }

  @Test
  void handleDomainException_shouldReturnBadRequest_forInvalidDomainValue() {
    var response = translator.handleDomainException(new InvalidDomainValueException("bad value"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("errorCode", "PROXIGAZ-400-002");
  }

  @Test
  void handleDomainException_shouldReturnInternalServerError_forUnknownDomainException() {
    // When
    var response =
        translator.handleDomainException(new DomainException(ErrorCodes.PROXIGAZ_500_000) {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).containsEntry("errorCode", "PROXIGAZ-500-000");
  }

  @Test
  void handleDomainException_shouldAlwaysCarryNonEmptyMessage() {
    // When
    var response = translator.handleDomainException(new DemoNotFoundException());

    // Then
    assertThat(response.getBody().get("message")).isNotBlank();
  }
}
