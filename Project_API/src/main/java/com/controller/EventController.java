package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : EventController.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: REST controller for event/firing history
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.dto.EventDTO;
import com.model.Event;
import com.persistance.Event.EventDAO;
import com.persistance.Event.EventFileDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    
    private final EventDAO eventDAO;
    
    public EventController() {
        this.eventDAO = new EventFileDAO();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Events API is running!");
    }
    
    @GetMapping
    public ResponseEntity<List<EventDTO>> getRecentEvents(@RequestParam(defaultValue = "50") int limit,
                                                          @RequestParam(required = false) String userId) {
        try {
            // Validate limit
            if (limit <= 0 || limit > 1000) {
                limit = 50;
            }
            
            List<Event> events = eventDAO.getRecentEvents(limit);
            
            // Filter events based on user context
            System.out.println("🔍 Filtering events for userId: " + userId + " (total events: " + events.size() + ")");
            
            List<EventDTO> eventDTOs = events.stream()
                    .filter(event -> {
                        // If userId is provided, only show user-specific events for that user
                        // or system events that are relevant to all users
                        if (userId != null && !userId.isEmpty()) {
                            boolean matches = (event.getUserId() != null && event.getUserId().equals(userId)) ||
                                             (event.getUserId() == null && "RULE_FIRED".equals(event.getEventType()));
                            
                            if (matches) {
                                System.out.println("  ✅ Event matches - userId: " + event.getUserId() + ", eventType: " + event.getEventType());
                            } else {
                                System.out.println("  ❌ Event filtered out - userId: " + event.getUserId() + ", eventType: " + event.getEventType());
                            }
                            return matches;
                        }
                        // If no userId, show all events (admin view)
                        return true;
                    })
                    .map(EventDTO::new)
                    .collect(Collectors.toList());
                    
            System.out.println("📊 Returning " + eventDTOs.size() + " filtered events");
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            System.err.println("Error getting recent events: " + e.getMessage());
            e.printStackTrace();
            // Return empty list instead of error when database is not connected
            return ResponseEntity.ok(new ArrayList<EventDTO>());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEventById(@PathVariable String id) {
        try {
            Event event = eventDAO.getEventById(id);
            if (event != null) {
                return ResponseEntity.ok(new EventDTO(event));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting event by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/rule/{ruleId}")
    public ResponseEntity<List<EventDTO>> getEventsByRuleId(@PathVariable String ruleId) {
        try {
            List<Event> events = eventDAO.getEventsByRuleId(ruleId);
            List<EventDTO> eventDTOs = events.stream()
                    .map(EventDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            System.err.println("Error getting events by rule ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/test-user-event")
    public ResponseEntity<String> createTestUserEvent(@RequestParam String userId, @RequestParam String message) {
        try {
            Event testEvent = new Event(
                new org.bson.types.ObjectId(), // dummy rule id
                "Test Emergency Rule",
                userId,
                "USER_ALERT_RECEIVED",
                message,
                new java.util.HashMap<>(),
                java.time.LocalDateTime.now()
            );
            
            eventDAO.createEvent(testEvent);
            return ResponseEntity.ok("Test user event created for: " + userId);
        } catch (Exception e) {
            System.err.println("Error creating test user event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-system-event")
    public ResponseEntity<String> createTestSystemEvent(@RequestParam(defaultValue = "Test System Alert") String message) {
        try {
            java.util.Map<String, Object> testPayload = new java.util.HashMap<>();
            testPayload.put("temp_c", 42.5);
            testPayload.put("humidity", 85);
            testPayload.put("condition", "Hot weather");
            
            Event testEvent = new Event(
                new org.bson.types.ObjectId(), // dummy rule id
                "Heat Wave Alert",
                testPayload,
                java.time.LocalDateTime.now(),
                3, // recipients
                new java.util.HashMap<String, Object>() {{
                    put("email", "success");
                    put("sms", "success");
                }}
            );
            
            eventDAO.createEvent(testEvent);
            return ResponseEntity.ok("Test system event created: " + message);
        } catch (Exception e) {
            System.err.println("Error creating test system event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/create-test-user-alert")
    public ResponseEntity<String> createTestUserAlert(@RequestParam(defaultValue = "disha.jadav@sjsu.edu") String userEmail) {
        try {
            java.util.Map<String, Object> testPayload = new java.util.HashMap<>();
            testPayload.put("temp_c", 45.2);
            testPayload.put("humidity", 75);
            testPayload.put("condition", "Very hot weather");
            
            String testMessage = "🚨 You received an email alert for: Extreme Heat Warning (Temperature: 45.2°C)";
            
            Event userEvent = new Event(
                new org.bson.types.ObjectId(), // dummy rule id
                "Heat Wave Alert",
                userEmail, // userId (email)
                "USER_ALERT_RECEIVED",
                testMessage,
                testPayload,
                java.time.LocalDateTime.now()
            );
            
            Event createdEvent = eventDAO.createEvent(userEvent);
            
            if (createdEvent != null) {
                return ResponseEntity.ok("✅ Test user alert created for: " + userEmail + ". Message: " + testMessage);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Failed to create user alert");
            }
        } catch (Exception e) {
            System.err.println("Error creating test user alert: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/debug-users")
    public ResponseEntity<String> debugUsers() {
        try {
            com.persistance.Users.UserDAO userDAO = new com.persistance.Users.UserFileDAO();
            java.util.List<com.model.User> users = userDAO.getAllUsers();
            
            StringBuilder debug = new StringBuilder();
            debug.append("Found ").append(users.size()).append(" users:\n");
            
            for (com.model.User user : users) {
                debug.append("- Username: ").append(user.getUsername())
                     .append(", Email: ").append(user.getEmail())
                     .append(", Active: ").append(user.isActive())
                     .append(", AllowAlerts: ").append(user.isAllowAlerts())
                     .append(", City: ").append(user.getCity()).append("\n");
            }
            
            return ResponseEntity.ok(debug.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/all-events-debug")
    public ResponseEntity<java.util.List<EventDTO>> getAllEventsDebug() {
        try {
            java.util.List<Event> events = eventDAO.getRecentEvents(100);
            
            System.out.println("📊 Debug: Found " + events.size() + " total events");
            
            for (Event event : events) {
                System.out.println(String.format("  Event: id=%s, userId=%s, eventType=%s, ruleName=%s, message=%s",
                    event.getId(), event.getUserId(), event.getEventType(), event.getRuleName(), 
                    event.getUserMessage() != null ? event.getUserMessage().substring(0, Math.min(50, event.getUserMessage().length())) + "..." : "null"));
            }
            
            java.util.List<EventDTO> eventDTOs = events.stream()
                    .map(EventDTO::new)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            System.err.println("Error getting all events for debug: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/system-status")
    public ResponseEntity<String> getSystemStatus() {
        try {
            StringBuilder status = new StringBuilder();
            status.append("=== EMERGENCY ALERT SYSTEM STATUS ===\n\n");
            
            // Check event storage
            java.util.List<Event> allEvents = eventDAO.getRecentEvents(100);
            status.append("📊 Events in storage: ").append(allEvents.size()).append("\n");
            
            if (allEvents.size() > 0) {
                status.append("\n📋 Recent events:\n");
                for (int i = 0; i < Math.min(5, allEvents.size()); i++) {
                    Event event = allEvents.get(i);
                    status.append(String.format("  %d. %s (UserID: %s, Type: %s)\n", 
                        i+1, event.getRuleName(), event.getUserId(), event.getEventType()));
                }
            }
            
            // Check user-specific events for target user
            String targetUser = "disha.jadav@sjsu.edu";
            long userEventCount = allEvents.stream()
                .filter(e -> targetUser.equals(e.getUserId()))
                .count();
            status.append("\n🎯 Events for ").append(targetUser).append(": ").append(userEventCount).append("\n");
            
            // Check InMemory event count if using fallback
            if (eventDAO instanceof com.persistance.Event.EventFileDAO) {
                int inMemoryCount = com.persistance.Event.InMemoryEventDAO.getEventCount();
                status.append("\n💾 InMemory events: ").append(inMemoryCount).append("\n");
            }
            
            return ResponseEntity.ok(status.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Error getting system status: " + e.getMessage());
        }
    }
    
    @PostMapping("/force-user-event-simple") 
    public ResponseEntity<String> forceSimpleUserEvent() {
        try {
            String userEmail = "disha.jadav@sjsu.edu";
            
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("temp_c", 19.0);
            payload.put("city", "San Jose");
            
            String message = "🚨 You received an email alert for: Cold Weather Warning (Temperature: 19°C)";
            
            Event userEvent = new Event(
                new org.bson.types.ObjectId(),
                "Cold Weather Alert", 
                userEmail,
                "USER_ALERT_RECEIVED",
                message,
                payload,
                java.time.LocalDateTime.now()
            );
            
            System.out.println("🔧 Force creating simple user event for: " + userEmail);
            Event created = eventDAO.createEvent(userEvent);
            
            if (created != null) {
                System.out.println("✅ User event created with ID: " + created.getId());
                return ResponseEntity.ok("✅ User event created for " + userEmail);
            } else {
                System.out.println("❌ Failed to create user event");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Failed to create event");
            }
        } catch (Exception e) {
            System.err.println("Error creating simple user event: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/create-demo-events")
    public ResponseEntity<String> createDemoEvents(@RequestParam(defaultValue = "disha.jadav@sjsu.edu") String userEmail) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("🎭 Creating demo events for presentation...\n\n");
            
            java.time.LocalDateTime baseTime = java.time.LocalDateTime.now().withHour(8).withMinute(0).withSecond(0);
            java.util.List<Event> demoEvents = new java.util.ArrayList<>();
            
            // 1. Cold Weather Alert (main demo event)
            java.util.Map<String, Object> coldPayload = new java.util.HashMap<>();
            coldPayload.put("temp_c", 18.5);
            coldPayload.put("temp_f", 65.3);
            coldPayload.put("humidity", 72);
            coldPayload.put("city", "San Jose");
            coldPayload.put("condition", "Cold");
            coldPayload.put("description", "Cold morning weather");
            
            Event coldEvent = new Event(
                new org.bson.types.ObjectId(),
                "Cold Weather Alert",
                userEmail,
                "USER_ALERT_RECEIVED",
                "🥶 You received an email alert for: Cold Weather Warning - Temperature dropped to 18.5°C (65.3°F) in San Jose. Please dress warmly and take necessary precautions.",
                coldPayload,
                baseTime.minusMinutes(5)
            );
            demoEvents.add(coldEvent);
            
            // 2. Heat Wave Alert 
            java.util.Map<String, Object> heatPayload = new java.util.HashMap<>();
            heatPayload.put("temp_c", 38.2);
            heatPayload.put("temp_f", 100.8);
            heatPayload.put("humidity", 85);
            heatPayload.put("city", "San Jose");
            heatPayload.put("condition", "Very Hot");
            
            Event heatEvent = new Event(
                new org.bson.types.ObjectId(),
                "Heat Wave Alert",
                userEmail,
                "USER_ALERT_RECEIVED",
                "🌡️ You received an email and SMS alert for: Extreme Heat Warning - Temperature reached 38.2°C (100.8°F) in San Jose. Stay hydrated and avoid outdoor activities.",
                heatPayload,
                baseTime.minusHours(2)
            );
            demoEvents.add(heatEvent);
            
            // 3. Fire Risk Alert
            java.util.Map<String, Object> firePayload = new java.util.HashMap<>();
            firePayload.put("temp_c", 35.0);
            firePayload.put("humidity", 15);
            firePayload.put("wind_speed", 25);
            firePayload.put("city", "San Jose");
            firePayload.put("condition", "Dry and Windy");
            
            Event fireEvent = new Event(
                new org.bson.types.ObjectId(),
                "Fire Risk - Dry Conditions",
                userEmail,
                "USER_ALERT_RECEIVED",
                "🔥 You received an email alert for: Fire Risk Warning - Low humidity (15%) and high winds detected in San Jose. Extreme fire danger conditions present.",
                firePayload,
                baseTime.minusHours(4)
            );
            demoEvents.add(fireEvent);
            
            // 4. Air Quality Alert
            java.util.Map<String, Object> airPayload = new java.util.HashMap<>();
            airPayload.put("aqi", 155);
            airPayload.put("pm25", 65.4);
            airPayload.put("city", "San Jose");
            airPayload.put("condition", "Unhealthy");
            
            Event airEvent = new Event(
                new org.bson.types.ObjectId(),
                "Air Quality Alert",
                userEmail,
                "USER_ALERT_RECEIVED",
                "💨 You received an email alert for: Air Quality Warning - AQI reached 155 (Unhealthy) in San Jose. Limit outdoor activities and use air purifiers indoors.",
                airPayload,
                baseTime.minusHours(6)
            );
            demoEvents.add(airEvent);
            
            // 5. Storm Warning
            java.util.Map<String, Object> stormPayload = new java.util.HashMap<>();
            stormPayload.put("wind_speed", 45);
            stormPayload.put("precipitation", 85);
            stormPayload.put("city", "San Jose");
            stormPayload.put("condition", "Severe Thunderstorm");
            
            Event stormEvent = new Event(
                new org.bson.types.ObjectId(),
                "Severe Storm Warning",
                userEmail,
                "USER_ALERT_RECEIVED",
                "🌧️ You received an email and SMS alert for: Severe Storm Warning - High winds (45 mph) and heavy rain expected in San Jose. Avoid travel and secure outdoor items.",
                stormPayload,
                baseTime.minusHours(8)
            );
            demoEvents.add(stormEvent);
            
            // Create all demo events
            int created = 0;
            for (Event event : demoEvents) {
                Event savedEvent = eventDAO.createEvent(event);
                if (savedEvent != null) {
                    created++;
                    result.append("✅ Created: ").append(event.getRuleName()).append("\n");
                } else {
                    result.append("❌ Failed: ").append(event.getRuleName()).append("\n");
                }
            }
            
            result.append("\n🎯 Demo Events Summary:\n");
            result.append("📧 User: ").append(userEmail).append("\n");
            result.append("📅 Time Range: 8:00 AM - Present\n");
            result.append("✅ Created: ").append(created).append(" out of ").append(demoEvents.size()).append(" events\n");
            result.append("\n🔗 Test the user portal now to see your emergency alerts!\n");
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Error creating demo events: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-complete-flow")
    public ResponseEntity<String> testCompleteFlow() {
        try {
            String userEmail = "disha.jadav@sjsu.edu";
            StringBuilder result = new StringBuilder();
            result.append("=== TESTING COMPLETE EVENT FLOW ===\n\n");
            
            // Step 1: Check initial state
            java.util.List<Event> initialEvents = eventDAO.getRecentEvents(100);
            result.append("1️⃣ Initial events in storage: ").append(initialEvents.size()).append("\n");
            
            // Step 2: Create a user event
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("temp_c", 19.0);
            payload.put("city", "San Jose");
            
            String message = "🚨 TESTING - You received an email alert for: Cold Weather Warning (Temperature: 19°C)";
            
            Event userEvent = new Event(
                new org.bson.types.ObjectId(),
                "TEST - Cold Weather Alert", 
                userEmail,
                "USER_ALERT_RECEIVED",
                message,
                payload,
                java.time.LocalDateTime.now()
            );
            
            result.append("\n2️⃣ Creating user event for: ").append(userEmail).append("\n");
            Event created = eventDAO.createEvent(userEvent);
            
            if (created != null) {
                result.append("✅ Event created with ID: ").append(created.getId()).append("\n");
            } else {
                result.append("❌ Failed to create event\n");
            }
            
            // Step 3: Verify storage
            java.util.List<Event> afterEvents = eventDAO.getRecentEvents(100);
            result.append("\n3️⃣ Events after creation: ").append(afterEvents.size()).append("\n");
            
            // Step 4: Test filtering
            java.util.List<Event> userEvents = afterEvents.stream()
                .filter(e -> userEmail.equals(e.getUserId()))
                .collect(Collectors.toList());
            result.append("\n4️⃣ Events filtered for user: ").append(userEvents.size()).append("\n");
            
            // Step 5: Test API endpoint
            result.append("\n5️⃣ Testing API endpoint...\n");
            try {
                ResponseEntity<java.util.List<EventDTO>> apiResponse = getRecentEvents(50, userEmail);
                java.util.List<EventDTO> apiEvents = apiResponse.getBody();
                result.append("✅ API returned: ").append(apiEvents != null ? apiEvents.size() : 0).append(" events\n");
            } catch (Exception e) {
                result.append("❌ API test failed: ").append(e.getMessage()).append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Error in complete flow test: " + e.getMessage());
        }
    }
}