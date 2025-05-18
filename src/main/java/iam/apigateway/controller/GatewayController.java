package iam.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewayController {

    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);

    @GetMapping("/usersFallback")
    public ResponseEntity<Object> userFallback() {
       log.error("User service is currently unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("User service is currently unavailable. Please try again later.");
    }

    @GetMapping("/userServiceDocsFallback")
    public ResponseEntity<Map<String, String>> userServiceFallback() {
        Map<String, String> response = new HashMap<>();
        log.error("User service API documentation is currently unavailable");
        response.put("message", "User service API documentation is currently unavailable. Please try again later.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }


    @GetMapping("/bookingsFallback")
    public ResponseEntity<Object> bookingFallback() {
        log.error("Booking service is currently unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Booking service is currently unavailable. Please try again later.");
    }

    @GetMapping("/bookingServiceDocsFallback")
    public ResponseEntity<Map<String, String>> bookingServiceFallback() {
        Map<String, String> response = new HashMap<>();
        log.error("Booking service API documentation is currently unavailable");
        response.put("message", "Booking service API documentation is currently unavailable. Please try again later.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

}
