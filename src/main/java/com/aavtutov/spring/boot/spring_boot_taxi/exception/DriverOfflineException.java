package com.aavtutov.spring.boot.spring_boot_taxi.exception;

public class DriverOfflineException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DriverOfflineException(String message) {
		super(message);
	}

}
