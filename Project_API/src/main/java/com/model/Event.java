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

    public Event() {}

    public Event(ObjectId ruleId, String ruleName, Map<String, Object> payload, 
                 LocalDateTime firedAt, int recipients, Map<String, Object> channelResults) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.payload = payload;
        this.firedAt = firedAt;
        this.recipients = recipients;
        this.channelResults = channelResults;
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

    public Document getDoc() {
        Document doc = new Document("ruleId", this.getRuleId())
                .append("ruleName", this.getRuleName())
                .append("payload", this.getPayload())
                .append("firedAt", this.getFiredAt().toString())
                .append("recipients", this.getRecipients())
                .append("channelResults", this.getChannelResults());

        return doc;
    }
}