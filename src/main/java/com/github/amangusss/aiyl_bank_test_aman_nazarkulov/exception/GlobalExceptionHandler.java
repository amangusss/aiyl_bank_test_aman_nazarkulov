package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.aop.LoggingAspect;
import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomAccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(CustomAccountNotFoundException ex,
                                                                        HttpServletRequest request) {
        String transactionId = MDC.get(LoggingAspect.TRANSACTION_ID_KEY);
        log.error("[Transaction: {}] Account not found: {}", transactionId, ex.getMessage());

        HttpStatus status = HttpStatus.NOT_FOUND;
        ErrorResponse error = buildError(status, "Account Not Found", ex.getMessage(), request);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalStatusStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStatusStateException(IllegalStatusStateException ex,
                                                                            HttpServletRequest request) {
        String transactionId = MDC.get(LoggingAspect.TRANSACTION_ID_KEY);
        log.error("[Transaction: {}] Illegal status state: {}", transactionId, ex.getMessage());

        HttpStatus status = HttpStatus.CONFLICT;
        ErrorResponse error = buildError(status, "Illegal Status State", ex.getMessage(), request);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalTransferException.class)
    public ResponseEntity<ErrorResponse> handleIllegalTransferException(IllegalTransferException ex,
                                                                        HttpServletRequest request) {
        String transactionId = MDC.get(LoggingAspect.TRANSACTION_ID_KEY);
        log.error("[Transaction: {}] Illegal transfer: {}", transactionId, ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse error = buildError(status, "Illegal Transfer", ex.getMessage(), request);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest request) {
        String transactionId = MDC.get(LoggingAspect.TRANSACTION_ID_KEY);
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("[Transaction: {}] Validation error: {}", transactionId, errorMessage);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse error = buildError(status, "Validation Error", errorMessage, request);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String transactionId = MDC.get(LoggingAspect.TRANSACTION_ID_KEY);
        log.error("[Transaction: {}] Unexpected error: {}", transactionId, ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse error = buildError(status, "Internal Server Error", "An unexpected error occurred", request);
        return ResponseEntity.status(status).body(error);
    }

    private ErrorResponse buildError(HttpStatus status, String error, String message, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "UNKNOWN";
        String transactionId = MDC.get(LoggingAspect.TRANSACTION_ID_KEY);
        return new ErrorResponse(status.value(), error, message, path, transactionId);
    }
}
