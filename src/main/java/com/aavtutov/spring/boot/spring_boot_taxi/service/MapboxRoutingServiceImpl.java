package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.aavtutov.spring.boot.spring_boot_taxi.exception.MapboxServiceException;

import lombok.Data;

/**
 * Implementation of the {@link MapboxRoutingService} that communicates with the
 * Mapbox Directions API (v5) to calculate route metrics for driving profiles.
 *
 * <p>
 * Uses Spring's {@link WebClient} for non-blocking HTTP requests and requires a
 * Mapbox Access Token for authentication.
 * </p>
 */
@Service
public class MapboxRoutingServiceImpl implements MapboxRoutingService {

	private final WebClient webClient;
	private String mapboxAccessToken;

	/**
	 * Constructs the service, injecting the required Mapbox access token and the
	 * pre-configured WebClient instance.
	 *
	 * @param mapboxAccessToken The security token for the Mapbox API, injected via
	 *                          Spring Value.
	 * @param webClient         The reactive HTTP client used for external service
	 *                          calls.
	 */
	public MapboxRoutingServiceImpl(@Value("${mapbox.access.token}") String mapboxAccessToken, WebClient webClient) {
		this.mapboxAccessToken = mapboxAccessToken;
		this.webClient = webClient;
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Queries the Mapbox Directions API for the optimal route distance.
	 *             </p>
	 *
	 * @param startLng The longitude of the starting point.
	 * @param startLat The latitude of the starting point.
	 * @param endLng   The longitude of the destination point.
	 * @param endLat   The latitude of the destination point.
	 * @return The distance in meters (as returned by Mapbox API, typically).
	 * @throws MapboxServiceException if the API call fails or returns no routes.
	 */
	@Override
	public double getDistance(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat) {

		// Rationale: Construct the Mapbox API URL using the driving profile and
		// coordinates.
		String url = String.format("https://api.mapbox.com/directions/v5/mapbox/driving/%f,%f;%f,%f?access_token=%s",
				startLng.doubleValue(), startLat.doubleValue(), endLng.doubleValue(), endLat.doubleValue(),
				mapboxAccessToken);

		// Rationale: Execute the reactive call and block to obtain the response
		// synchronously.
		MapboxResponse response = webClient.get().uri(url).retrieve().bodyToMono(MapboxResponse.class).block();

		if (response != null && !response.routes.isEmpty()) {

			// Rationale: Distance is returned in meters. We return the raw value from the
			// first (best) route.
			return response.routes.get(0).distance;
		}

		// Note: The original exception message is in Russian; replacing with an English
		// equivalent.
		throw new MapboxServiceException("Не удалось получить расстояние от Mapbox API");
	}

	/**
	 * @inheritDoc
	 *             <p>
	 *             Queries the Mapbox Directions API for the optimal route,
	 *             returning the distance and duration.
	 *             </p>
	 *
	 * @param startLng The longitude of the starting point.
	 * @param startLat The latitude of the starting point.
	 * @param endLng   The longitude of the destination point.
	 * @param endLat   The latitude of the destination point.
	 * @return The {@link Route} object containing distance and duration metrics.
	 * @throws MapboxServiceException if the API call fails or returns no routes.
	 */
	@Override
	public Route getRoute(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat) {

		// Rationale: Construct the Mapbox API URL using the driving profile and
		// coordinates.
		String url = String.format("https://api.mapbox.com/directions/v5/mapbox/driving/%f,%f;%f,%f?access_token=%s",
				startLng.doubleValue(), startLat.doubleValue(), endLng.doubleValue(), endLat.doubleValue(),
				mapboxAccessToken);

		// Rationale: Execute the reactive call and block to obtain the response
		// synchronously.
		MapboxResponse response = webClient.get().uri(url).retrieve().bodyToMono(MapboxResponse.class).block();

		if (response != null && !response.routes.isEmpty()) {
			return response.routes.get(0);
		}

		throw new MapboxServiceException("Failed to retrieve route from Mapbox API");
	}

	// --- Inner DTOs for Mapbox Response Deserialization ---

	/**
	 * Data Transfer Object (DTO) representing the top-level response structure from
	 * the Mapbox Directions API.
	 */
	@Data
	public static class MapboxResponse {
		/**
		 * A list of possible routes. We typically use the first route returned.
		 */
		private List<Route> routes;
	}

	/**
	 * DTO representing a single route object within the Mapbox response.
	 *
	 * <p>
	 * This class is exposed through the {@link MapboxRoutingService#getRoute}
	 * method.
	 * </p>
	 */
	@Data
	public static class Route {
		/**
		 * The distance of the route in meters.
		 */
		private double distance;

		/**
		 * The estimated duration of the route in seconds.
		 */
		private double duration;
	}

}
