/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class DomainExceptionTranslator {

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
    var status =
        switch (ex) {
          case DemoNotFoundException _ -> HttpStatus.NOT_FOUND;
          case DemoAlreadyExistsException _ -> HttpStatus.CONFLICT;
          case FieldsExtractionCodeNotSupported _ -> HttpStatus.BAD_REQUEST;
          default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    var message = ex.getMessage() == null ? "" : ex.getMessage();
    return ResponseEntity.status(status)
        .body(Map.of("errorCode", ex.getErrorCode().value(), "message", message));
  }
}
