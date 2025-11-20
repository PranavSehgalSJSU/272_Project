package com.dto;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : EventDTO.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Data Transfer Object for Event API responses
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Event;
import java.time.LocalDateTime;

public class EventDTO {
    private String id;
    private String timestamp;
    private String type;
    private String message;
    private String ruleId;
    private String ruleName;

    public EventDTO() {}

    public EventDTO(Event event) {
        this.id = event.getId() != null ? event.getId().toHexString() : null;
        this.timestamp = event.getFiredAt() != null ? event.getFiredAt().toString() : null;
        this.type = "RULE_FIRED";
        this.ruleId = event.getRuleId() != null ? event.getRuleId().toHexString() : null;
        this.ruleName = event.getRuleName();
        
        // Create a readable message
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Alert rule '").append(event.getRuleName()).append("' was triggered");
        if (event.getRecipients() > 0) {
            messageBuilder.append(" and sent to ").append(event.getRecipients()).append(" recipient(s)");
        }
        
        // Add payload info if available
        if (event.getPayload() != null && !event.getPayload().isEmpty()) {
            messageBuilder.append(". Data: ");
            event.getPayload().entrySet().stream()
                    .limit(3) // Show first 3 data points
                    .forEach(entry -> messageBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(" "));
        }
        
        this.message = messageBuilder.toString().trim();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
}