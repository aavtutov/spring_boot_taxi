package com.aavtutov.spring.boot.spring_boot_taxi.service;

import java.math.BigDecimal;

import com.aavtutov.spring.boot.spring_boot_taxi.service.MapboxRoutingServiceImpl.Route;

public interface MapboxRoutingService {
	
	double getDistance(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat);
	Route getRoute(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat);

}
