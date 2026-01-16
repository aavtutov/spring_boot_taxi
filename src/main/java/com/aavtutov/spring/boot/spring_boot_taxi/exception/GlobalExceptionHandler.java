package com.aavtutov.spring.boot.spring_boot_taxi.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// 404 Not Found
    @ExceptionHandler({
            ClientNotFoundException.class,
            DriverNotFoundException.class,
            OrderNotFoundException.class,
            ResourceNotFoundException.class
    })
    public ResponseEntity<IncorrectData> handleNotFound(Exception exception, HttpServletRequest request) {
        return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
    }
    
    // 409 Conflict
    @ExceptionHandler({
            ClientAlreadyExistsException.class,
            DriverAlreadyExistsException.class,
            ActiveOrderAlreadyExistsException.class,
            DriverOfflineException.class,
            OrderStatusConflictException.class
    })
    public ResponseEntity<IncorrectData> handleConflict(Exception exception, HttpServletRequest request) {
        return buildErrorResponse(exception, HttpStatus.CONFLICT, request);
    }
    
    // 400 Bad Request
    @ExceptionHandler({
            IllegalArgumentException.class,
            MapboxServiceException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<IncorrectData> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof MethodArgumentTypeMismatchException mismatch 
            ? "Invalid value " + mismatch.getValue() + " for parameter " + mismatch.getName()
            : exception.getMessage();
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request);
    }
    
	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleNoContentException(NoContentException exception,
			HttpServletRequest request) {
		return buildErrorResponse(exception, HttpStatus.NO_CONTENT, request);
	}
    
    // @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<IncorrectData> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed");
        return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, request);
    }
    
    // 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<IncorrectData> handleAll(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error occurred at {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        return buildErrorResponse("An internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

	// Helpers
	
	private ResponseEntity<IncorrectData> buildErrorResponse(Exception exception, HttpStatus status,
			HttpServletRequest request) {
		return buildErrorResponse(exception.getMessage(), status, request);
	}

	private ResponseEntity<IncorrectData> buildErrorResponse(String message, HttpStatus status,
			HttpServletRequest request) {
		IncorrectData data = new IncorrectData();
		data.setError(message);
		data.setStatus(status.value());
		data.setPath(request.getRequestURI());
		data.setTimestamp(Instant.now());
		return new ResponseEntity<>(data, status);
	}
}
