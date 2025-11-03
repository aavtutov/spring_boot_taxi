package com.aavtutov.spring.boot.spring_boot_taxi.entity;

/**
 * Enumeration defining the operational status of a driver in the taxi service.
 *
 * <p>This status dictates whether a driver is eligible to receive and fulfill orders.</p>
 */
public enum DriverStatus {

	/**
     * The initial status for a newly registered driver. Requires administrative
     * review and approval of documents before moving to the ACTIVE state.
     */
    PENDING_APPROVAL,
    
    /**
     * The driver is approved, but not logged in and currently unable to accept orders.
     * This status is set by the system.
     */
    INACTIVE,
    
    /**
     * The driver is approved, logged in, and actively ready to receive and accept orders.
     */
    ACTIVE,
    
    /**
     * The driver has been permanently or temporarily suspended by the administration
     * and cannot participate in the service.
     */
    BANNED;
}
