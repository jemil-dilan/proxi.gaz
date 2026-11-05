/*
 * Copyright (c) 2026 ProxiGaz
 * All rights reserved.
 */
package cm.jemil.proxi.gaz.interfaces.rest;

import cm.jemil.proxi.gaz.config.CorrelationIdHolder;
import cm.jemil.proxi.gaz.domain.DomainException;
import cm.jemil.proxi.gaz.domain.ErrorCode;
import cm.jemil.proxi.gaz.domain.ErrorCodes;
import cm.jemil.proxi.gaz.domain.FieldsExtractionCodeNotSupported;
import cm.jemil.proxi.gaz.domain.InvalidDomainValueException;
import cm.jemil.proxi.gaz.domain.demo.DemoAlreadyExistsException;
import cm.jemil.proxi.gaz.domain.demo.DemoNotFoundException;
import cm.jemil.proxi.gaz.domain.order.InvalidOrderStatusTransitionException;
import cm.jemil.proxi.gaz.domain.user.InvalidCredentialsException;
import cm.jemil.proxi.gaz.domain.vendor.InvalidVendorSessionException;
import cm.jemil.proxi.gaz.domain.vendor.VendorNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
class DomainExceptionTranslator {

  private static final String PROBLEM_MEDIA_TYPE = "application/problem+json";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleInvalidRequest(MethodArgumentNotValidException ex) {
    var errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    Map.of(
                        "field",
                        error.getField(),
                        "message",
                        error.getDefaultMessage() == null ? " " : error.getDefaultMessage()))
            .collect(Collectors.toList());
    var detail =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .findFirst()
            .orElse("request contains invalid fields");
    return buildProblemResponse(
        ErrorCodes.PROXIGAZ_400_002, HttpStatus.BAD_REQUEST, "Validation failed", detail, errors);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ProblemDetail> handleInvalidMethodRequest(
      HandlerMethodValidationException ex) {
    return buildProblemResponse(
        ErrorCodes.PROXIGAZ_400_002,
        HttpStatus.BAD_REQUEST,
        "Invalid query parameters",
        "request contains invalid query parameters",
        null);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    MissingPathVariableException.class,
    HttpMediaTypeNotSupportedException.class,
    HttpRequestMethodNotSupportedException.class,
    NoHandlerFoundException.class
  })
  public ResponseEntity<ProblemDetail> handleSpringMVCClientErrors(Exception ex) {
    return buildProblemResponse(
        ErrorCodes.PROXIGAZ_400_002,
        HttpStatus.BAD_REQUEST,
        "Invalid request",
        ex.getMessage(),
        null);
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
    var status = resolveStatus(ex);
    return buildProblemResponse(ex.getErrorCode(), status, ex.getMessage(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception ex) {
    log.error("Unexpected error", ex);
    return buildProblemResponse(
        ErrorCodes.PROXIGAZ_500_000,
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred",
        null);
  }

  private HttpStatus resolveStatus(DomainException ex) {
    return switch (ex) {
      case DemoNotFoundException _, VendorNotFoundException _ -> HttpStatus.NOT_FOUND;
      case DemoAlreadyExistsException _ -> HttpStatus.CONFLICT;
      case InvalidCredentialsException _, InvalidVendorSessionException _ ->
          HttpStatus.UNAUTHORIZED;
      case FieldsExtractionCodeNotSupported _,
          InvalidOrderStatusTransitionException _,
          InvalidDomainValueException _ ->
          HttpStatus.BAD_REQUEST;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  private ResponseEntity<ProblemDetail> buildProblemResponse(
      ErrorCode errorCode, HttpStatusCode status, String detail, List<Map<String, String>> errors) {
    return buildProblemResponse(errorCode, status, null, detail, errors);
  }

  private ResponseEntity<ProblemDetail> buildProblemResponse(
      ErrorCode errorCode,
      HttpStatusCode status,
      String title,
      String detail,
      List<Map<String, String>> errors) {
    var problem =
        ProblemDetail.builder()
            .type(buildProblemType(errorCode))
            .title(title != null ? title : errorCode.description())
            .status(status.value())
            .code(errorCode.value())
            .correlationId(CorrelationIdHolder.get())
            .detail(detail)
            .timestamp(Instant.now())
            .errors(errors)
            .build();

    return ResponseEntity.status(status)
        .header(HttpHeaders.CONTENT_TYPE, PROBLEM_MEDIA_TYPE)
        .body(problem);
  }

  private String buildProblemType(ErrorCode errorCode) {
    return "https://api.proxigaz.cm/probs/" + errorCode.value().toLowerCase();
  }
}
