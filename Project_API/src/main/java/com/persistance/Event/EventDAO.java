package com.persistance.Event;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : EventDAO.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Interface for Event data access operations
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Event;
import org.bson.types.ObjectId;
import java.util.List;

public interface EventDAO {
    Event createEvent(Event event);
    Event getEventById(ObjectId id);
    Event getEventById(String id);
    List<Event> getRecentEvents(int limit);
    List<Event> getEventsByRuleId(ObjectId ruleId);
    List<Event> getEventsByRuleId(String ruleId);
}