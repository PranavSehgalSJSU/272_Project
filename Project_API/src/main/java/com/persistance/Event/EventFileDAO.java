package com.persistance.Event;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : EventFileDAO.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: MongoDB implementation for Event data access
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Event;
import com.persistance.Database.MongoConn;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;

public class EventFileDAO implements EventDAO {
    
    private final MongoCollection<Document> collection;
    
    public EventFileDAO() {
        MongoDatabase database = MongoConn.getDatabase();
        this.collection = database.getCollection("events");
    }
    
    @Override
    public Event createEvent(Event event) {
        try {
            Document doc = event.getDoc();
            collection.insertOne(doc);
            event.setId(doc.getObjectId("_id"));
            return event;
        } catch (Exception e) {
            System.err.println("Error creating event: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Event getEventById(ObjectId id) {
        try {
            Document doc = collection.find(eq("_id", id)).first();
            return doc != null ? documentToEvent(doc) : null;
        } catch (Exception e) {
            System.err.println("Error getting event by ID: " + e.getMessage());
            return null;
        }
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
        try {
            List<Event> events = new ArrayList<>();
            for (Document doc : collection.find().sort(descending("firedAt")).limit(limit)) {
                Event event = documentToEvent(doc);
                if (event != null) {
                    events.add(event);
                }
            }
            return events;
        } catch (Exception e) {
            System.err.println("Error getting recent events: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Event> getEventsByRuleId(ObjectId ruleId) {
        try {
            List<Event> events = new ArrayList<>();
            for (Document doc : collection.find(eq("ruleId", ruleId)).sort(descending("firedAt"))) {
                Event event = documentToEvent(doc);
                if (event != null) {
                    events.add(event);
                }
            }
            return events;
        } catch (Exception e) {
            System.err.println("Error getting events by rule ID: " + e.getMessage());
            return new ArrayList<>();
        }
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
    
    private Event documentToEvent(Document doc) {
        try {
            Event event = new Event();
            event.setId(doc.getObjectId("_id"));
            event.setRuleId(doc.getObjectId("ruleId"));
            event.setRuleName(doc.getString("ruleName"));
            event.setPayload((Map<String, Object>) doc.get("payload", Map.class));
            event.setRecipients(doc.getInteger("recipients", 0));
            event.setChannelResults((Map<String, Object>) doc.get("channelResults", Map.class));
            
            // Parse firedAt
            String firedAtStr = doc.getString("firedAt");
            if (firedAtStr != null && !firedAtStr.isEmpty()) {
                event.setFiredAt(LocalDateTime.parse(firedAtStr));
            }
            
            return event;
        } catch (Exception e) {
            System.err.println("Error converting document to event: " + e.getMessage());
            return null;
        }
    }
}