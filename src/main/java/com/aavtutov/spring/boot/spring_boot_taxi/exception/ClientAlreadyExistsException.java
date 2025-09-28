package com.aavtutov.spring.boot.spring_boot_taxi.exception;

public class ClientAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClientAlreadyExistsException(String message) {
		super(message);
	}

}
