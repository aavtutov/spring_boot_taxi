package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;

import com.aavtutov.spring.boot.spring_boot_taxi.service.MapboxRoutingServiceImpl.Route;

/**
 * Interface for calculating trip metrics via external routing services.
 * Provides distance and duration data based on real-world road networks.
 */
public interface MapboxRoutingService {

	/**
     * Calculates the distance between two points using the road network.
     * @return Distance in meters.
     */
	double getDistance(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat);

	/**
     * Retrieves full route details including distance and estimated travel time.
     */
	Route getRoute(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat);
}
