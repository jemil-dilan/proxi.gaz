/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.admin.InvalidAdminCredentialsException;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import cm.jemil.proxi.gaz.domain.order.InvalidOrderStatusTransitionException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
class DomainExceptionTranslator {

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
    var status =
        switch (ex) {
          case DemoNotFoundException _ -> HttpStatus.NOT_FOUND;
          case DemoAlreadyExistsException _ -> HttpStatus.CONFLICT;
          case InvalidAdminCredentialsException _ -> HttpStatus.UNAUTHORIZED;
          case FieldsExtractionCodeNotSupported _,
              InvalidOrderStatusTransitionException _,
              InvalidDomainValueException _ ->
              HttpStatus.BAD_REQUEST;
          default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    var message = ex.getMessage() == null ? "" : ex.getMessage();
    return ResponseEntity.status(status)
        .body(Map.of("errorCode", ex.getErrorCode().value(), "message", message));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception ex) {
    log.error("Unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("errorCode", "PROXIGAZ-500-000", "message", "An unexpected error occurred"));
  }
}
