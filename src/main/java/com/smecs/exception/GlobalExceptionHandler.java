package com.smecs.exception;

import com.smecs.dto.ResponseDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDTO<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(new ResponseDTO<>("error", "Validation failed", errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDTO<String>> handleConstraintViolationException(ConstraintViolationException ex) {
        return new ResponseEntity<>(new ResponseDTO<>("error", ex.getMessage(), null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ResponseDTO<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ResponseDTO<>("error", ex.getMessage(), null), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseDTO<String>> handleUnauthorizedException(UnauthorizedException ex) {
        return new ResponseEntity<>(new ResponseDTO<>("error", ex.getMessage(), null), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ResponseDTO<String>> handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>(new ResponseDTO<>("error", ex.getMessage(), null), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CategoryInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseDTO<String>> handleCategoryInUseException(CategoryInUseException ex) {
        return new ResponseEntity<>(new ResponseDTO<>("error", ex.getMessage(), null), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseDTO<String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        String userFriendlyMessage;

        // Check if it's a foreign key constraint violation
        if (message != null && message.contains("foreign key constraint")) {
            if (message.contains("categories") && message.contains("products")) {
                userFriendlyMessage = "Cannot delete category because it contains products. Please remove or reassign all products first.";
            } else if (message.contains("products") && message.contains("order")) {
                userFriendlyMessage = "Cannot delete product because it is referenced in existing orders.";
            } else {
                userFriendlyMessage = "Cannot delete this record because it is referenced by other data.";
            }
        } else if (message != null && message.contains("unique constraint")) {
            userFriendlyMessage = "A record with this value already exists.";
        } else {
            userFriendlyMessage = "Database constraint violation occurred.";
        }

        return new ResponseEntity<>(new ResponseDTO<>("error", userFriendlyMessage, null), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseDTO<String>> handleAllExceptions(Exception ex) {
        return new ResponseEntity<>(new ResponseDTO<>("error", "An unexpected error occurred: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
