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
	private final String mapboxAccessToken;
	
	public MapboxRoutingServiceImpl(
            WebClient webClient, 
            @Value("${mapbox.access.token}") String mapboxAccessToken) {
        this.webClient = webClient;
        this.mapboxAccessToken = mapboxAccessToken;
    }

	@Override
	public double getDistance(
			BigDecimal startLng, BigDecimal startLat,
			BigDecimal endLng, BigDecimal endLat) {
		return getRoute(startLng, startLat, endLng, endLat).getDistance();
	}

	@Override
	public Route getRoute(
			BigDecimal startLng, BigDecimal startLat,
			BigDecimal endLng, BigDecimal endLat) {
		
		String coordinates = String.format("%f,%f;%f,%f", startLng, startLat, endLng, endLat);
		
		MapboxResponse response = webClient.get()
				.uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.mapbox.com")
                        .path("/directions/v5/mapbox/driving-traffic/{coords}")
                        .queryParam("access_token", mapboxAccessToken)
                        .build(coordinates))
				.retrieve()
				.bodyToMono(MapboxResponse.class)
				.block();

		if (response == null || response.getRoutes().isEmpty()) {
            throw new MapboxServiceException("No routes found from Mapbox API");
        }

		return response.routes.get(0);
	}

	// Inner DTOs

	@Data
	public static class MapboxResponse {
		private List<Route> routes;
	}

	@Data
	public static class Route {
		private double distance; // meters
		private double duration; // seconds
	}
}
