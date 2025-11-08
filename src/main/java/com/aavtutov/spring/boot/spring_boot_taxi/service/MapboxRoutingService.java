package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;

import com.aavtutov.spring.boot.spring_boot_taxi.service.MapboxRoutingServiceImpl.Route;

/**
 * Service interface defining the contract for interacting with an external
 * geographical routing service, such as Mapbox, to calculate trip metrics.
 *
 * <p>
 * This service is crucial for accurately determining the distance and estimated
 * duration of a taxi order based on real-world road networks.
 * </p>
 */
public interface MapboxRoutingService {

	/**
	 * Calculates the distance of the optimal route between two geographical points
	 * using the road network data.
	 *
	 * @param startLng The longitude of the starting point.
	 * @param startLat The latitude of the starting point.
	 * @param endLng   The longitude of the destination point.
	 * @param endLat   The latitude of the destination point.
	 * @return The calculated distance in kilometers (or the service's default unit,
	 *         typically meters, converted to kilometers).
	 */
	double getDistance(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat);

	/**
	 * Retrieves detailed routing information, including distance, estimated travel
	 * time, and possibly route geometry, between two geographical points.
	 *
	 * <p>
	 * The result is encapsulated in a {@link Route} object, which contains the full
	 * path data.
	 * </p>
	 *
	 * @param startLng The longitude of the starting point.
	 * @param startLat The latitude of the starting point.
	 * @param endLng   The longitude of the destination point.
	 * @param endLat   The latitude of the destination point.
	 * @return A {@link Route} object containing the total distance and duration of
	 *         the trip.
	 */
	Route getRoute(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat);

}
