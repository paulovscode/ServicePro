package com.servicepro.backend;

import com.servicepro.backend.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Trata validações de DTO (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
            false,
            "Erro de validação",
            errors,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Trata violações de constraints
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    ConstraintViolation::getMessage
                ));
        
        ApiResponse<Map<String, String>> response = new ApiResponse<>(
            false,
            "Erro de validação de constraints",
            errors,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Trata exceções de negócio (RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        ApiResponse<Void> response = new ApiResponse<>(
            false,
            ex.getMessage(),
            null,
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Trata exceções genéricas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        // Log do erro (em produção, usar logger)
        System.err.println("Erro interno: " + ex.getMessage());
        ex.printStackTrace();
        
        ApiResponse<Void> response = new ApiResponse<>(
            false,
            "Erro interno do servidor. Por favor, tente novamente mais tarde.",
            null,
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Trata recursos não encontrados
     */
    @ExceptionHandler(com.servicepro.backend.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            com.servicepro.backend.exception.ResourceNotFoundException ex) {
        
        ApiResponse<Void> response = new ApiResponse<>(
            false,
            ex.getMessage(),
            null,
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value()
        );
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
