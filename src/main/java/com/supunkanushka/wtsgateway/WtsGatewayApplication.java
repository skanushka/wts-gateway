package com.supunkanushka.wtsgateway;

import com.supunkanushka.wtsgateway.config.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WtsGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(WtsGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes().route(r ->
						r.path("/product")
								.filters(gatewayFilterSpec -> gatewayFilterSpec.filter(new SecurityConfig()))
								.uri("http://localhost:8080/product"))
				.build();
	}
}
