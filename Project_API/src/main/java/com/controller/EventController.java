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
    public ResponseEntity<List<EventDTO>> getRecentEvents(@RequestParam(defaultValue = "50") int limit) {
        try {
            // Validate limit
            if (limit <= 0 || limit > 1000) {
                limit = 50;
            }
            
            List<Event> events = eventDAO.getRecentEvents(limit);
            List<EventDTO> eventDTOs = events.stream()
                    .map(EventDTO::new)
                    .collect(Collectors.toList());
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
}