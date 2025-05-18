package iam.apigateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/* Reactive security configuration */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private final String[] allowedResourceUrls = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/swagger-resources/**", "/api-docs/**", "/aggregate/**", "/actuator/**"};

    /* Configure security filter chain:
    1. All requests require authentication
    2. Configure as OAuth2 resource server with JWT validation
   Note: Currently using default JWT converter, will be replaced with KeycloakJwtAuthenticationConverter
         for proper role/authority mapping from Keycloak tokens
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(allowedResourceUrls).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(
                                // jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()) // will explore this later
                                Customizer.withDefaults()))
                .build();
    }




    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("*"));
        corsConfig.setMaxAge(3600L); // Cache preflight for 1 hour
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}



/*  Will be explored later....
* The KeycloakJwtAuthenticationConverter is specifically designed to:
- Extract roles and permissions from Keycloak's token structure
- Map Keycloak's role format to Spring Security authorities
*/
//public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
//    @Override
//    public AbstractAuthenticationToken convert(Jwt jwt) {
//        // Extracts roles from Keycloak-specific claims like "realm_access" and "resource_access"
//        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
//
//        // Creates a Spring Security authentication token with the extracted authorities
//        return new JwtAuthenticationToken(jwt, authorities);
//    }
//}

