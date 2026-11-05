/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import cm.jemil.proxi.gaz.config.CorrelationIdHolder;
import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCodes;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import cm.jemil.proxi.gaz.domain.order.InvalidOrderStatusTransitionException;
import cm.jemil.proxi.gaz.domain.order.OrderStatus;
import cm.jemil.proxi.gaz.domain.user.InvalidCredentialsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DomainExceptionTranslatorTest {

  private final DomainExceptionTranslator translator = new DomainExceptionTranslator();

  @BeforeEach
  void setUp() {
    CorrelationIdHolder.set("test-correlation-id");
  }

  @AfterEach
  void tearDown() {
    CorrelationIdHolder.clear();
  }

  @Test
  void handleDomainException_shouldReturnNotFound_forDemoNotFound() {
    // When
    var response = translator.handleDomainException(new DemoNotFoundException());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    var problem = response.getBody();
    assertThat(problem).isNotNull();
    assertThat(problem.code()).isEqualTo("PROXIGAZ-404-000");
    assertThat(problem.status()).isEqualTo(404);
    assertThat(problem.correlationId()).isEqualTo("test-correlation-id");
    assertThat(problem.title()).isEqualTo("DEMO NOT FOUND");
  }

  @Test
  void handleDomainException_shouldReturnConflict_forDemoAlreadyExists() {
    // When
    var response = translator.handleDomainException(new DemoAlreadyExistsException());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    var problem = response.getBody();
    assertThat(problem).isNotNull();
    assertThat(problem.code()).isEqualTo("PROXIGAZ-409-000");
    assertThat(problem.status()).isEqualTo(409);
  }

  @Test
  void handleDomainException_shouldReturnUnauthorized_forInvalidAdminCredentials() {
    var response = translator.handleDomainException(new InvalidCredentialsException());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    var problem = response.getBody();
    assertThat(problem).isNotNull();
    assertThat(problem.code()).isEqualTo("PROXIGAZ-401-000");
    assertThat(problem.status()).isEqualTo(401);
  }

  @Test
  void handleDomainException_shouldReturnBadRequest_forUnsupportedExtractionCode() {
    // When
    var response = translator.handleDomainException(new FieldsExtractionCodeNotSupported());

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    var problem = response.getBody();
    assertThat(problem).isNotNull();
    assertThat(problem.code()).isEqualTo("PROXIGAZ-400-000");
    assertThat(problem.status()).isEqualTo(400);
  }

  @Test
  void handleDomainException_shouldReturnBadRequest_forInvalidOrderStatusTransition() {
    var response =
        translator.handleDomainException(
            new InvalidOrderStatusTransitionException(OrderStatus.PENDING, OrderStatus.DELIVERED));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    var problem = response.getBody();
    assertThat(problem).isNotNull();
    assertThat(problem.code()).isEqualTo("PROXIGAZ-400-001");
    assertThat(problem.status()).isEqualTo(400);
  }

  @Test
  void handleDomainException_shouldReturnBadRequest_forInvalidDomainValue() {
    var response = translator.handleDomainException(new InvalidDomainValueException("bad value"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    var problem = response.getBody();
    assertThat(problem).isNotNull();
    assertThat(problem.code()).isEqualTo("PROXIGAZ-400-002");
    assertThat(problem.status()).isEqualTo(400);
  }

  @Test
  void handleDomainException_shouldReturnInternalServerError_forUnknownDomainException() {
    // When
    var response =
        translator.handleDomainException(new DomainException(ErrorCodes.PROXIGAZ_500_000) {});

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    var problem = response.getBody();
    assertThat(problem).isNotNull();
    assertThat(problem.code()).isEqualTo("PROXIGAZ-500-000");
    assertThat(problem.status()).isEqualTo(500);
  }

  @Test
  void handleDomainException_shouldAlwaysCarryNonEmptyDetail() {
    // When
    var response = translator.handleDomainException(new DemoNotFoundException());

    // Then
    var problem = response.getBody();
    assertThat(problem.detail()).isNotBlank();
  }

  @Test
  void handleDomainException_shouldHaveRFC7807Structure() {
    // When
    var response = translator.handleDomainException(new DemoNotFoundException());

    // Then
    var problem = response.getBody();
    assertThat(problem.type()).isNotNull();
    assertThat(problem.title()).isNotNull();
    assertThat(problem.status()).isNotNull();
    assertThat(problem.code()).isNotNull();
    assertThat(problem.correlationId()).isNotNull();
    assertThat(problem.timestamp()).isNotNull();
  }
}
