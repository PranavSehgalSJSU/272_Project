package com.alert.source;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : StatusSourceAdapter.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Status check source adapter for monitoring services
///////////////////////////////////////////////////////////////////////////////////////////////////////

import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StatusSourceAdapter implements SourceAdapter {
    private final WebClient webClient;

    public StatusSourceAdapter() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public CompletableFuture<Map<String, Object>> fetch(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = (String) params.get("url");
                if (url == null || url.isEmpty()) {
                    throw new RuntimeException("URL parameter is required for status check");
                }

                // Make the HTTP request
                Map<String, Object> response = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                // Normalize the response
                return normalizeStatusResponse(response, url);

            } catch (Exception e) {
                // Return DOWN status if request fails
                System.err.println("Status check failed: " + e.getMessage());
                return getFailureStatusData(params, e.getMessage());
            }
        });
    }

    private Map<String, Object> normalizeStatusResponse(Map<String, Object> response, String url) {
        Map<String, Object> normalized = new HashMap<>();
        
        // Try to extract status from common response formats
        String status = "UNKNOWN";
        if (response.containsKey("status")) {
            status = (String) response.get("status");
        } else if (response.containsKey("health")) {
            status = (String) response.get("health");
        } else if (response.containsKey("state")) {
            status = (String) response.get("state");
        } else if (!response.isEmpty()) {
            // If we got any response, assume it's OK
            status = "OK";
        }

        normalized.put("status", normalizeStatus(status));
        normalized.put("url", url);
        normalized.put("timestamp", LocalDateTime.now().toString());
        normalized.put("response_time_ms", System.currentTimeMillis() % 1000); // Mock response time
        normalized.put("raw_response", response);
        
        return normalized;
    }

    private String normalizeStatus(String status) {
        if (status == null) return "DOWN";
        
        String upperStatus = status.toUpperCase();
        if (upperStatus.contains("OK") || upperStatus.contains("UP") || 
            upperStatus.contains("HEALTHY") || upperStatus.contains("ONLINE")) {
            return "OK";
        }
        return "DOWN";
    }

    private Map<String, Object> getFailureStatusData(Map<String, Object> params, String error) {
        Map<String, Object> failure = new HashMap<>();
        String url = (String) params.getOrDefault("url", "unknown");
        
        failure.put("status", "DOWN");
        failure.put("url", url);
        failure.put("timestamp", LocalDateTime.now().toString());
        failure.put("error", error);
        failure.put("response_time_ms", -1);
        
        return failure;
    }

    @Override
    public String getSourceType() {
        return "STATUS";
    }
}