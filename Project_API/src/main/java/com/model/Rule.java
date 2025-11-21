package com.model;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : Rule.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Rule model for emergency alert system
///////////////////////////////////////////////////////////////////////////////////////////////////////

import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

public class Rule {
    public enum Source {
        WEATHER, STATUS, CUSTOM
    }
    
    private ObjectId id;
    private String name;
    private Source source;
    private Map<String, Object> params; // JSON params for source adapter
    private String condition; // condition string like "temp_c > 40"
    private RuleMessage message; // header, content, channels
    private RuleAudience audience; // targeting rules
    private int cooldownMinutes;
    private LocalDateTime lastFiredAt;
    private boolean enabled;

    public Rule() {
        this.enabled = true;
        this.cooldownMinutes = 60; // default 1 hour cooldown
    }

    public Rule(String name, Source source, Map<String, Object> params, String condition, 
                RuleMessage message, RuleAudience audience, int cooldownMinutes, boolean enabled) {
        this.name = name;
        this.source = source;
        this.params = params;
        this.condition = condition;
        this.message = message;
        this.audience = audience;
        this.cooldownMinutes = cooldownMinutes;
        this.enabled = enabled;
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    
    // Helper method to get ID as string for API responses
    public String getIdAsString() { 
        return id != null ? id.toHexString() : null; 
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public RuleMessage getMessage() { return message; }
    public void setMessage(RuleMessage message) { this.message = message; }

    public RuleAudience getAudience() { return audience; }
    public void setAudience(RuleAudience audience) { this.audience = audience; }

    public int getCooldownMinutes() { return cooldownMinutes; }
    public void setCooldownMinutes(int cooldownMinutes) { this.cooldownMinutes = cooldownMinutes; }

    public LocalDateTime getLastFiredAt() { return lastFiredAt; }
    public void setLastFiredAt(LocalDateTime lastFiredAt) { this.lastFiredAt = lastFiredAt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Document getDoc() {
        Document doc = new Document("name", this.getName())
                .append("source", this.getSource().toString())
                .append("params", this.getParams())
                .append("condition", this.getCondition())
                .append("cooldownMinutes", this.getCooldownMinutes())
                .append("enabled", this.isEnabled());

        if (this.getMessage() != null) {
            doc.append("message", this.getMessage().getDoc());
        }
        if (this.getAudience() != null) {
            doc.append("audience", this.getAudience().getDoc());
        }
        if (this.getLastFiredAt() != null) {
            doc.append("lastFiredAt", this.getLastFiredAt().toString());
        }

        return doc;
    }

    // Nested classes for structured data
    public static class RuleMessage {
        private String header;
        private String content;
        private List<String> channels; // ["email", "sms"]

        public RuleMessage() {}

        public RuleMessage(String header, String content, List<String> channels) {
            this.header = header;
            this.content = content;
            this.channels = channels;
        }

        public String getHeader() { return header; }
        public void setHeader(String header) { this.header = header; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public List<String> getChannels() { return channels; }
        public void setChannels(List<String> channels) { this.channels = channels; }

        public Document getDoc() {
            return new Document("header", this.getHeader())
                    .append("content", this.getContent())
                    .append("channels", this.getChannels());
        }
    }

    public static class RuleAudience {
        private List<String> tags; // must contain these tags
        private String city; // must be in this city
        
        public RuleAudience() {}

        public RuleAudience(List<String> tags, String city) {
            this.tags = tags;
            this.city = city;
        }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public Document getDoc() {
            return new Document("tags", this.getTags())
                    .append("city", this.getCity());
        }
    }
}