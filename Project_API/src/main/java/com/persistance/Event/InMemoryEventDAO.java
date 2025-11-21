package com.persistance.Event;

import com.model.Event;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of EventDAO for testing without database
 */
public class InMemoryEventDAO implements EventDAO {
    
    private static final ConcurrentHashMap<ObjectId, Event> events = new ConcurrentHashMap<>();
    
    @Override
    public Event createEvent(Event event) {
        try {
            // Generate a new ObjectId if not set
            if (event.getId() == null) {
                event.setId(new ObjectId());
            }
            
            // Set fired time if not set
            if (event.getFiredAt() == null) {
                event.setFiredAt(LocalDateTime.now());
            }
            
            events.put(event.getId(), event);
            System.out.println("✅ Event created in memory: " + event.getRuleName() + 
                             " (ID: " + event.getId() + ", UserID: " + event.getUserId() + 
                             ", Type: " + event.getEventType() + ")");
            System.out.println("📊 Total events in memory: " + events.size());
            return event;
        } catch (Exception e) {
            System.err.println("Error creating event in memory: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public Event getEventById(ObjectId id) {
        return events.get(id);
    }
    
    @Override
    public Event getEventById(String id) {
        try {
            return getEventById(new ObjectId(id));
        } catch (Exception e) {
            System.err.println("Error getting event by string ID: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public List<Event> getRecentEvents(int limit) {
        List<Event> result = events.values().stream()
                .sorted(Comparator.comparing(Event::getFiredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .collect(Collectors.toList());
        
        System.out.println("🔍 InMemory getRecentEvents: Found " + events.size() + " total, returning " + result.size());
        for (Event event : result) {
            System.out.println("  - " + event.getRuleName() + " (UserID: " + event.getUserId() + ", Type: " + event.getEventType() + ")");
        }
        
        return result;
    }
    
    @Override
    public List<Event> getEventsByRuleId(ObjectId ruleId) {
        return events.values().stream()
                .filter(event -> ruleId.equals(event.getRuleId()))
                .sorted(Comparator.comparing(Event::getFiredAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Event> getEventsByRuleId(String ruleId) {
        try {
            return getEventsByRuleId(new ObjectId(ruleId));
        } catch (Exception e) {
            System.err.println("Error getting events by string rule ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Helper method to get all events (for debugging)
    public static int getEventCount() {
        return events.size();
    }
    
    // Helper method to clear all events (for testing)
    public static void clearAllEvents() {
        events.clear();
    }
}