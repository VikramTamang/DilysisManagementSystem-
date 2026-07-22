package com.fonepay.gateway.exception;

import com.fonepay.gateway.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles our custom business-logic exceptions
     * (e.g. "Merchant not found", "Email already exists").
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        logger.warn("AppException: {} [{}]", ex.getMessage(), ex.getErrorCode());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    /**
     * Handles validation failures from @Valid on request DTOs
     * (e.g. blank name, invalid email format).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        logger.warn("Validation failed: {}", fieldErrors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(fieldErrors)
                .errorCode("VALIDATION_ERROR")
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Access Denied / Authorization exceptions (403 Forbidden).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("AccessDeniedException: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("Access denied: You do not have permission to access this resource.")
                .errorCode("ACCESS_DENIED")
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles unsupported HTTP methods (405 Method Not Allowed).
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        logger.warn("HttpRequestMethodNotSupportedException: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("HTTP method '" + ex.getMethod() + "' is not supported for this endpoint. Supported methods: " + ex.getSupportedHttpMethods())
                .errorCode("METHOD_NOT_ALLOWED")
                .build();

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Catch-all safety net for any exception we didn't explicitly anticipate.
     * In production, we never expose raw exception details to the client —
     * full details go to logs only, the client just gets a safe generic message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("Unhandled exception occurred", ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message("An unexpected error occurred. Please try again later.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}