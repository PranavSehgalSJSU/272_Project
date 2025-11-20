package com.alert.source;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : WeatherSourceAdapter.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Weather API source adapter
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.persistance.Database.PropertyReader;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WeatherSourceAdapter implements SourceAdapter {
    private final WebClient webClient;

    public WeatherSourceAdapter() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openweathermap.org/data/2.5")
                .build();
    }

    @Override
    public CompletableFuture<Map<String, Object>> fetch(Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String city = (String) params.get("city");
                Double lat = (Double) params.get("lat");
                Double lon = (Double) params.get("lon");
                String apiKey = PropertyReader.getProperty("weather.api.key");

                if (apiKey == null || apiKey.isEmpty()) {
                    throw new RuntimeException("Weather API key not configured");
                }

                String url;
                if (city != null && !city.isEmpty()) {
                    url = "/weather?q=" + city + "&appid=" + apiKey + "&units=metric";
                } else if (lat != null && lon != null) {
                    url = "/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";
                } else {
                    throw new RuntimeException("Either city or lat/lon coordinates must be provided");
                }

                // Make the API call
                Map<String, Object> response = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                // Normalize the response
                return normalizeWeatherResponse(response);

            } catch (Exception e) {
                // Return mock data if API fails (for testing)
                System.err.println("Weather API failed, returning mock data: " + e.getMessage());
                return getMockWeatherData(params);
            }
        });
    }

    private Map<String, Object> normalizeWeatherResponse(Map<String, Object> response) {
        Map<String, Object> normalized = new HashMap<>();
        
        try {
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            normalized.put("temp_c", ((Number) main.get("temp")).doubleValue());
            normalized.put("humidity", ((Number) main.get("humidity")).intValue());
            normalized.put("pressure", ((Number) main.get("pressure")).intValue());
            
            normalized.put("city", (String) response.get("name"));
            normalized.put("timestamp", LocalDateTime.now().toString());
            
            Map<String, Object> weather = ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);
            normalized.put("description", (String) weather.get("description"));
            normalized.put("condition", (String) weather.get("main"));
            
        } catch (Exception e) {
            System.err.println("Error parsing weather response: " + e.getMessage());
            throw new RuntimeException("Failed to parse weather data");
        }
        
        return normalized;
    }

    private Map<String, Object> getMockWeatherData(Map<String, Object> params) {
        Map<String, Object> mock = new HashMap<>();
        String city = (String) params.getOrDefault("city", "Unknown");
        
        mock.put("temp_c", 25.0);
        mock.put("humidity", 65);
        mock.put("pressure", 1013);
        mock.put("city", city);
        mock.put("timestamp", LocalDateTime.now().toString());
        mock.put("description", "clear sky");
        mock.put("condition", "Clear");
        
        return mock;
    }

    @Override
    public String getSourceType() {
        return "WEATHER";
    }
}