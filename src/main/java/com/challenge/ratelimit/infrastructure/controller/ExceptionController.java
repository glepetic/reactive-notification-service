package com.challenge.ratelimit.infrastructure.controller;

import com.challenge.ratelimit.domain.exception.NotificationRejectedException;
import com.challenge.ratelimit.infrastructure.dto.http.inbound.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(NotificationRejectedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotificationRejectedException(NotificationRejectedException e) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "Limit Exceeded", Collections.singletonList(e.getMessage()));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServerWebInputException(ServerWebInputException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid input", Collections.singletonList(e.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebExchangeBindException(WebExchangeBindException e) {
        final String errorTemplate = "Invalid field [%s] with value [%s] - Error [%s] - Message: %s";
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format(errorTemplate, fieldError.getField(), fieldError.getRejectedValue(),
                        fieldError.getCode(), fieldError.getDefaultMessage()))
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", errors);
    }

    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnsupportedMediaTypeStatusException(UnsupportedMediaTypeStatusException e) {
        return buildForbidden(e);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNoResourceFoundException(NoResourceFoundException e) {
        return buildForbidden(e);
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMethodNotAllowedException(MethodNotAllowedException e) {
        return buildForbidden(e);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> fallbackHandleException(Exception e) {
        return Mono.just(e)
                .doOnNext(ex -> log.error("Unexpected ex {} with message: {}", ex.getClass().getName(), ex.getMessage()))
                .flatMap(ex -> buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", Collections.singletonList(ex.getMessage())));
    }

    private Mono<ResponseEntity<ErrorResponse>> buildForbidden(Exception e) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", Collections.singletonList("Forbidden"))
                .doOnNext(response -> log.warn("Forbbiden access - Ex is {} with message: {}", e.getClass().getName(), e.getMessage()));
    }

    private Mono<ResponseEntity<ErrorResponse>> buildResponse(HttpStatus status, String reason, List<String> errors) {
        return Mono.fromSupplier(() -> new ErrorResponse(reason, errors))
                .map(body -> ResponseEntity.status(status)
                        .body(body));
    }

}
