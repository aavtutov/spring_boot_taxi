package com.aavtutov.spring.boot.spring_boot_taxi.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<IncorrectData> buildErrorResponse(Exception exception, HttpStatus status,
			HttpServletRequest request) {
		IncorrectData data = new IncorrectData();
		data.setError(exception.getMessage());
		data.setStatus(status.value());
		data.setPath(request.getRequestURI());
		data.setTimestamp(Instant.now());
		return new ResponseEntity<>(data, status);
	}

	private ResponseEntity<IncorrectData> buildErrorResponse(String exceptionMessage, HttpStatus status,
			HttpServletRequest request) {
		IncorrectData data = new IncorrectData();
		data.setError(exceptionMessage);
		data.setStatus(status.value());
		data.setPath(request.getRequestURI());
		data.setTimestamp(Instant.now());
		return new ResponseEntity<>(data, status);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleClientAlreadyExistException(ClientAlreadyExistsException exception,
			HttpServletRequest request) {
		return buildErrorResponse(exception, HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleDriverAlreadyExistException(DriverAlreadyExistsException exception,
			HttpServletRequest request) {
		return buildErrorResponse(exception, HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleNoSuchClientException(ClientNotFoundException exception,
			HttpServletRequest request) {
		return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleNoSuchDriverException(DriverNotFoundException exception,
			HttpServletRequest request) {
		return buildErrorResponse(exception, HttpStatus.NOT_FOUND, request);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleDriverOfflineException(DriverOfflineException exception,
			HttpServletRequest request) {
		return buildErrorResponse(exception, HttpStatus.CONFLICT, request);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleTypeMismatchException(MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		String errorMessage = "Invalid value " + exception.getValue() + " for parameter " + exception.getName();
		return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleNotValidException(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String errorMessage = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getDefaultMessage()).findFirst().orElse("Validation failed");
		return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler
	public ResponseEntity<IncorrectData> handleException(Exception exception, HttpServletRequest request) {
		return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

}
