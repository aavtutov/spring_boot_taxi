package com.aavtutov.spring.boot.spring_boot_taxi.exception;

public class OrderStatusConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OrderStatusConflictException(String message) {
		super(message);
	}
	
	
}
