package com.supunkanushka.wtsgateway.config;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
public class SecurityConfig implements GatewayFilter, Ordered {

	private static HttpHeaders getHeaders() {
		String basicAuth = "mobile:pin";
		String encodedCredentials =
				new String(Base64.getEncoder().encode(basicAuth.getBytes()));

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Basic " + encodedCredentials);
		httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
		return httpHeaders;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		ServerHttpRequest request = exchange.getRequest();
		HttpHeaders headers = request.getHeaders();
		String token = headers.getFirst("Authorization");
		if (token == null) {
			token = request.getQueryParams().getFirst("Authorization");
		}

		ServerHttpResponse response = exchange.getResponse();
		if (StringUtils.isEmpty(token) || !StringUtils.startsWith(token, "Bearer")) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}

		String url = String.format("http://localhost:8082/oauth/check_token?token=%s", token.split(" ")[1]);

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders httpHeaders = getHeaders();

		HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

		boolean unauthenticated;
		try {
			ResponseEntity<JSONObject> res = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, JSONObject.class);
			unauthenticated = res.getStatusCode() != HttpStatus.OK;
		} catch (HttpClientErrorException e) {
			unauthenticated = true;
		}
		if (unauthenticated) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.setComplete();
		}

		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return 0;
	}
}