package com.aavtutov.spring.boot.spring_boot_taxi.dto;

public enum OrderAction {

	ACCEPT, START_TRIP, COMPLETE, CANCEL_BY_DRIVER, CANCEL_BY_CLIENT;

	public boolean isDriverAction() {
		return this == ACCEPT || this == START_TRIP || this == COMPLETE || this == CANCEL_BY_DRIVER;
	}
}
