package iam.apigateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user_service_route")
                .route(path("/users/**").or(path("/users")), http("http://localhost:8080"))
                //TODO: add response header and PrefixPath
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("userCircuitBreaker",
                        URI.create("forward:/fallbackUrl")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookingServiceRoute() {
        return route("booking_service_route")
                .route(path("/bookings/**").or(path("/bookings")), http("http://localhost:8081"))
                //TODO: add response header and PrefixPath
                .filter(
                        CircuitBreakerFilterFunctions.circuitBreaker("bookingCircuitBreaker",
                        URI.create("forward:/bookingFallbackUrl")))
                .build();
    }

    /**
     * Fallback route for user service
     * @return RouterFunction
     */
    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackUrl", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("User service is currently unavailable. Please try again later."))
                .build();
    }

    /**
     * Fallback route for booking service
     * @return RouterFunction
     */
    @Bean
    public RouterFunction<ServerResponse> bookingFallbackRoute() {
        return route("bookingFallbackRoute")
                .GET("/bookingFallbackUrl", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Booking Service is currently unavailable. Please try again later."))
                .build();
    }
}
