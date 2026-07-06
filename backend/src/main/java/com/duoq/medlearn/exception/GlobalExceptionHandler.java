package com.duoq.medlearn.exception;

import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.exception.AiInjectionDetectedException;
import com.duoq.medlearn.ai.exception.AiOutputFilteredException;
import com.duoq.medlearn.ai.exception.AiProviderException;
import com.duoq.medlearn.ai.exception.AiProviderUnavailableException;
import com.duoq.medlearn.ai.exception.AiQuotaExceededException;
import com.duoq.medlearn.ai.exception.AiRateLimitException;
import com.duoq.medlearn.ai.exception.AiContextExceededException;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex,
                                                                            HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleSpringSecurityAuthException(
            org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex,
                                                                  HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimit(RateLimitExceededException ex,
                                                              HttpServletRequest request) {
        return error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            UsernameAlreadyExistsException.class,
            AccountDeactivatedException.class,
            EmailNotVerifiedException.class,
            InvalidCredentialsException.class,
            TokenReusedException.class,
            AiConfigurationException.class,
            AiInjectionDetectedException.class,
            AiOutputFilteredException.class,
            AiContextExceededException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflictAndBadRequests(RuntimeException ex,
                                                                            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AiQuotaExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiQuotaExceeded(AiQuotaExceededException ex,
                                                                     HttpServletRequest request) {
        return error(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler(AiRateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiRateLimit(AiRateLimitException ex,
                                                                HttpServletRequest request) {
        return error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(AiProviderException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiProvider(AiProviderException ex,
                                                               HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, "AI provider error: " + ex.getMessage());
    }

    @ExceptionHandler(AiProviderUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiProviderUnavailable(AiProviderUnavailableException ex,
                                                                          HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AI service unavailable: " + ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex,
                                                                  HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex,
                                                                      HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex,
                                                                         HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex,
                                                                  HttpServletRequest request) {
        log.warn("Business validation failed: {}", ex.getMessage());
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("workflow transition")
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;
        return error(status, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }
}
