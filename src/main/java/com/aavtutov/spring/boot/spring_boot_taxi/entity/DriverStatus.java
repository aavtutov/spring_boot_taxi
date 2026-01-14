package com.aavtutov.spring.boot.spring_boot_taxi.entity;

/**
 * Operational status of a driver.
 */
public enum DriverStatus {

	/**
     * The initial status for a newly registered driver. Requires administrative
     * review and approval of documents before moving to the ACTIVE state.
     */
    PENDING_APPROVAL,
    
    /**
     * The driver is approved, but not logged in and currently unable to accept orders.
     */
    INACTIVE,
    
    /**
     * The driver is approved, logged in, and actively ready to receive, 
     * accept and update orders.
     */
    ACTIVE,
    
    /**
     * The driver has been permanently or temporarily suspended by the administration
     * and cannot participate in the service.
     */
    BANNED;
}
