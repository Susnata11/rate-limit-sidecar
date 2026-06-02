package com.example.ratelimiter.controller;

import com.example.ratelimiter.limiter.RateLimiterManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final RateLimiterManager rateLimiterManager;
    private final WebClient webClient;

    @RequestMapping("/**")
    public ResponseEntity<String> proxy(
            HttpServletRequest request) {

        String clientIp = request.getRemoteAddr();

        if (!rateLimiterManager.tryConsume(clientIp, 1)) {

            log.warn(
                    "Rate limit exceeded for {}",
                    clientIp);

            return ResponseEntity
                    .status(429)
                    .body("Rate limit exceeded");
        }

        String path = request.getRequestURI()
                .replaceFirst("/api/v1", "/api");

        String queryString = request.getQueryString();

        if (queryString != null) {
            path += "?" + queryString;
        }

        log.info(
                "Forwarding {} {}",
                request.getMethod(),
                path);

        try {

            String response =
                    webClient
                            .get()
                            .uri(path)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();

            return ResponseEntity.ok(response);

        } catch (Exception ex) {

            log.error(
                    "Wish Calculator unavailable",
                    ex);

            return ResponseEntity
                    .status(503)
                    .body("Wish Calculator unavailable");
        }
    }
}