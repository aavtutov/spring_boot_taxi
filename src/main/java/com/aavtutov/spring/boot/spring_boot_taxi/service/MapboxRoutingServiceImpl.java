package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.aavtutov.spring.boot.spring_boot_taxi.exception.MapboxServiceException;

import lombok.Data;

@Service
public class MapboxRoutingServiceImpl implements MapboxRoutingService {

	private final WebClient webClient;
	private String mapboxAccessToken;

	public MapboxRoutingServiceImpl(@Value("${mapbox.access.token}") String mapboxAccessToken, WebClient webClient) {
		this.mapboxAccessToken = mapboxAccessToken;
		this.webClient = webClient;
	}

	@Override
	public double getDistance(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat) {
		String url = String.format("https://api.mapbox.com/directions/v5/mapbox/driving/%f,%f;%f,%f?access_token=%s",
				startLng.doubleValue(), startLat.doubleValue(), endLng.doubleValue(), endLat.doubleValue(),
				mapboxAccessToken);

		MapboxResponse response = webClient.get().uri(url).retrieve().bodyToMono(MapboxResponse.class).block();

		if (response != null && !response.routes.isEmpty()) {
			return response.routes.get(0).distance;
		}

		throw new MapboxServiceException("Не удалось получить расстояние от Mapbox API");
	}

	@Override
	public Route getRoute(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat) {
		String url = String.format("https://api.mapbox.com/directions/v5/mapbox/driving/%f,%f;%f,%f?access_token=%s",
				startLng.doubleValue(), startLat.doubleValue(), endLng.doubleValue(), endLat.doubleValue(),
				mapboxAccessToken);

		MapboxResponse response = webClient.get().uri(url).retrieve().bodyToMono(MapboxResponse.class).block();

		if (response != null && !response.routes.isEmpty()) {
			return response.routes.get(0);
		}

		throw new MapboxServiceException("Не удалось получить маршрут от Mapbox API");
	}

	@Data
	public static class MapboxResponse {
		private List<Route> routes;
	}

	@Data
	public static class Route {
		private double distance;
		private double duration;
	}

}
