package com.aavtutov.spring.boot.spring_boot_taxi.exception;

public class ActiveOrderAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ActiveOrderAlreadyExistsException(String message) {
		super(message);
	}
	
}
