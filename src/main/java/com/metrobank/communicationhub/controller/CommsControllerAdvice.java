/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.metrobank.communicationhub.exception.RestProxyException;
import com.metrobank.communicationhub.exception.RestProxyUnprocessableEntityException;
import com.metrobank.communicationhub.model.Error;
import com.mongodb.MongoExecutionTimeoutException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.kafka.requestreply.KafkaReplyTimeoutException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ServerErrorException;

import static com.metrobank.communicationhub.util.CommsHubConstants.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Log4j2
public class CommsControllerAdvice {

  @ExceptionHandler({
    RestProxyUnprocessableEntityException.class,
    IllegalArgumentException.class,
    MissingServletRequestParameterException.class,
    InvalidFormatException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<Error> restProxyUnprocessableEntityException(final Exception exception) {
    log.error("Invalid request parameters:{}", exception.getMessage());
    final String message =
        exception.getClass() == RestProxyUnprocessableEntityException.class
            ? exception.getMessage()
            : BAD_REQUEST_ERROR_MESSAGE;
    final Error errors = Error.builder().code(BAD_REQUEST_ERROR_CODE).message(message).build();
    return new ResponseEntity<>(errors, BAD_REQUEST);
  }

  @ExceptionHandler({KafkaReplyTimeoutException.class})
  public ResponseEntity<Error> kafkaReplyTimeoutException(
      final KafkaReplyTimeoutException exception) {
    log.error(RESPONSE_TIMEOUT_ERROR_MESSAGE);
    final Error errors =
        Error.builder().code(RESPONSE_TIMEOUT_ERROR_CODE).message(exception.getMessage()).build();
    return new ResponseEntity<>(errors, REQUEST_TIMEOUT);
  }

  @ExceptionHandler({MongoExecutionTimeoutException.class})
  public ResponseEntity<Error> mongoExecutionTimeoutException(final MongoExecutionTimeoutException exception) {
    log.error(RESPONSE_TIMEOUT_ERROR_MESSAGE);
    final Error errors =
        Error.builder().code(RESPONSE_TIMEOUT_ERROR_CODE).message(exception.getMessage()).build();
    return new ResponseEntity<>(errors, REQUEST_TIMEOUT);
  }

  @ExceptionHandler({
    NullPointerException.class,
    InterruptedException.class,
    InternalServerErrorException.class,
    ServerErrorException.class,
    HttpServerErrorException.class,
    RestProxyException.class
  })
  public ResponseEntity<Error> handleException(final Exception exception) {
    log.error(INTERNAL_SERVER_ERROR_CODE, exception);
    final Error errors =
        Error.builder()
            .code(INTERNAL_SERVER_ERROR_CODE)
            .message(INTERNAL_SERVER_ERROR_MESSAGE)
            .build();
    return new ResponseEntity<>(errors, INTERNAL_SERVER_ERROR);
  }
}
