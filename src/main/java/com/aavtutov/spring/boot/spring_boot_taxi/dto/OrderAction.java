package com.aavtutov.spring.boot.spring_boot_taxi.dto;

import java.util.Set;

/**
 * Actions to modify order states.
 */
public enum OrderAction {

	ACCEPT,
	
	START_TRIP,
	
	COMPLETE,
	
	CANCEL_BY_DRIVER,
	
	CANCEL_BY_CLIENT;
	
	private static final Set<OrderAction> DRIVER_ACTIONS = Set.of(
			ACCEPT, START_TRIP, COMPLETE, CANCEL_BY_DRIVER );
	
	public boolean isDriverAction() {
		return DRIVER_ACTIONS.contains(this);
	}
}
