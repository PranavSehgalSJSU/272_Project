package com.model;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : Event.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Event model for tracking alert rule firings
///////////////////////////////////////////////////////////////////////////////////////////////////////

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.Map;

public class Event {
    private ObjectId id;
    private ObjectId ruleId; // foreign key to rule
    private String ruleName; // cached rule name for easy display
    private Map<String, Object> payload; // snapshot of fetched data when fired
    private LocalDateTime firedAt;
    private int recipients; // number of recipients
    private Map<String, Object> channelResults; // per-channel success/failure stats
    private String userId; // for user-specific events
    private String eventType; // RULE_FIRED, USER_ALERT_RECEIVED, etc.
    private String userMessage; // personalized message for user

    public Event() {}

    public Event(ObjectId ruleId, String ruleName, Map<String, Object> payload, 
                 LocalDateTime firedAt, int recipients, Map<String, Object> channelResults) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.payload = payload;
        this.firedAt = firedAt;
        this.recipients = recipients;
        this.channelResults = channelResults;
        this.eventType = "RULE_FIRED";
    }

    // Constructor for user-specific events
    public Event(ObjectId ruleId, String ruleName, String userId, String eventType, 
                 String userMessage, Map<String, Object> payload, LocalDateTime firedAt) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.userId = userId;
        this.eventType = eventType;
        this.userMessage = userMessage;
        this.payload = payload;
        this.firedAt = firedAt;
        this.recipients = 1; // single user
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public ObjectId getRuleId() { return ruleId; }
    public void setRuleId(ObjectId ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public LocalDateTime getFiredAt() { return firedAt; }
    public void setFiredAt(LocalDateTime firedAt) { this.firedAt = firedAt; }

    public int getRecipients() { return recipients; }
    public void setRecipients(int recipients) { this.recipients = recipients; }

    public Map<String, Object> getChannelResults() { return channelResults; }
    public void setChannelResults(Map<String, Object> channelResults) { this.channelResults = channelResults; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }

    public Document getDoc() {
        Document doc = new Document("ruleId", this.getRuleId())
                .append("ruleName", this.getRuleName())
                .append("payload", this.getPayload())
                .append("firedAt", this.getFiredAt().toString())
                .append("recipients", this.getRecipients())
                .append("channelResults", this.getChannelResults())
                .append("userId", this.getUserId())
                .append("eventType", this.getEventType())
                .append("userMessage", this.getUserMessage());

        return doc;
    }
}