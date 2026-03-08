package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.exception;

import com.github.amangusss.aiyl_bank_test_aman_nazarkulov.dto.ErrorResponse;
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
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(CustomAccountNotFoundException ex) {
        String transactionId = MDC.get("transactionId");
        log.error("[Transaction: {}] Account not found: {}", transactionId, ex.getMessage());

        ErrorResponse error = new ErrorResponse("Account Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalStatusStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStatusStateException(IllegalStatusStateException ex) {
        String transactionId = MDC.get("transactionId");
        log.error("[Transaction: {}] Illegal status state: {}", transactionId, ex.getMessage());

        ErrorResponse error = new ErrorResponse("Illegal Status State", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalTransferException.class)
    public ResponseEntity<ErrorResponse> handleIllegalTransferException(IllegalTransferException ex) {
        String transactionId = MDC.get("transactionId");
        log.error("[Transaction: {}] Illegal transfer: {}", transactionId, ex.getMessage());

        ErrorResponse error = new ErrorResponse("Illegal Transfer", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String transactionId = MDC.get("transactionId");
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("[Transaction: {}] Validation error: {}", transactionId, errorMessage);

        ErrorResponse error = new ErrorResponse("Validation Error", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String transactionId = MDC.get("transactionId");
        log.error("[Transaction: {}] Unexpected error: {}", transactionId, ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse("Internal Server Error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
