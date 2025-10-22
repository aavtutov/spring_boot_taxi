package com.aavtutov.spring.boot.spring_boot_taxi.exception;

public class MapboxServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MapboxServiceException(String message) {
		super(message);
	}

}
