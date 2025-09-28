package com.aavtutov.spring.boot.spring_boot_taxi.exception;

public class DriverNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DriverNotFoundException(String message) {
		super(message);
	}

}
