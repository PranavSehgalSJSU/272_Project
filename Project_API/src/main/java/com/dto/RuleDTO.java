package com.dto;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : RuleDTO.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Data Transfer Object for Rule with string ID
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Rule;
import java.time.LocalDateTime;
import java.util.Map;

public class RuleDTO {
    private String id;
    private String name;
    private String source;
    private Map<String, Object> params;
    private String condition;
    private Rule.RuleMessage message;
    private Rule.RuleAudience audience;
    private int cooldownMinutes;
    private LocalDateTime lastFiredAt;
    private boolean enabled;

    public RuleDTO() {}

    public RuleDTO(Rule rule) {
        this.id = rule.getIdAsString();
        this.name = rule.getName();
        this.source = rule.getSource().toString();
        this.params = rule.getParams();
        this.condition = rule.getCondition();
        this.message = rule.getMessage();
        this.audience = rule.getAudience();
        this.cooldownMinutes = rule.getCooldownMinutes();
        this.lastFiredAt = rule.getLastFiredAt();
        this.enabled = rule.isEnabled();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Rule.RuleMessage getMessage() { return message; }
    public void setMessage(Rule.RuleMessage message) { this.message = message; }

    public Rule.RuleAudience getAudience() { return audience; }
    public void setAudience(Rule.RuleAudience audience) { this.audience = audience; }

    public int getCooldownMinutes() { return cooldownMinutes; }
    public void setCooldownMinutes(int cooldownMinutes) { this.cooldownMinutes = cooldownMinutes; }

    public LocalDateTime getLastFiredAt() { return lastFiredAt; }
    public void setLastFiredAt(LocalDateTime lastFiredAt) { this.lastFiredAt = lastFiredAt; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}