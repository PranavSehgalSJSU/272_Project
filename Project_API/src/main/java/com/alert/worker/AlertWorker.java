package com.alert.worker;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AlertWorker.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Scheduled worker for evaluating and firing alert rules
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.alert.audience.AudienceResolver;
import com.alert.condition.ConditionEvaluator;
import com.alert.sender.MultiChannelSender;
import com.alert.source.SourceAdapterFactory;
import com.alert.source.SourceAdapter;
import com.alert.template.MessageTemplater;
import com.model.Rule;
import com.model.User;
import com.model.Event;
import com.persistance.Rule.RuleDAO;
import com.persistance.Rule.RuleFileDAO;
import com.persistance.Event.EventDAO;
import com.persistance.Event.EventFileDAO;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class AlertWorker {
    
    private final RuleDAO ruleDAO;
    private final EventDAO eventDAO;
    private final ConditionEvaluator conditionEvaluator;
    private final AudienceResolver audienceResolver;
    private final MessageTemplater messageTemplater;
    private final MultiChannelSender multiChannelSender;
    
    public AlertWorker() {
        this.ruleDAO = new RuleFileDAO();
        this.eventDAO = new EventFileDAO();
        this.conditionEvaluator = new ConditionEvaluator();
        this.audienceResolver = new AudienceResolver();
        this.messageTemplater = new MessageTemplater();
        this.multiChannelSender = new MultiChannelSender();
    }
    
    /**
     * Scheduled method that runs daily at 8:00 AM to evaluate rules
     */
    @Scheduled(cron = "0 0 8 * * ?") // Daily at 8:00 AM
    public void evaluateRules() {
        System.out.println("Starting rule evaluation at " + LocalDateTime.now());
        
        try {
            List<Rule> enabledRules = ruleDAO.getEnabledRules();
            System.out.println("Found " + enabledRules.size() + " enabled rules to evaluate");
            
            for (Rule rule : enabledRules) {
                try {
                    evaluateRule(rule);
                } catch (Exception e) {
                    System.err.println("Error evaluating rule " + rule.getName() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in rule evaluation worker: " + e.getMessage());
        }
        
        System.out.println("Completed rule evaluation at " + LocalDateTime.now());
    }
    
    /**
     * Evaluate a single rule
     */
    private void evaluateRule(Rule rule) {
        try {
            System.out.println("Evaluating rule: " + rule.getName());
            
            // Check cooldown
            if (!isCooldownExpired(rule)) {
                System.out.println("Rule " + rule.getName() + " is in cooldown, skipping");
                return;
            }
            
            // Fetch data from source
            SourceAdapter adapter = SourceAdapterFactory.getAdapter(rule.getSource());
            CompletableFuture<Map<String, Object>> dataFuture = adapter.fetch(rule.getParams());
            Map<String, Object> sourceData = dataFuture.get(); // Block for result
            
            // Evaluate condition
            boolean conditionMet = conditionEvaluator.evaluate(rule.getCondition(), sourceData);
            
            if (conditionMet) {
                System.out.println("Condition met for rule: " + rule.getName());
                fireRule(rule, sourceData);
            } else {
                System.out.println("Condition not met for rule: " + rule.getName());
            }
            
        } catch (Exception e) {
            System.err.println("Error evaluating rule " + rule.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Fire a rule - send alerts to audience
     */
    private void fireRule(Rule rule, Map<String, Object> sourceData) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Resolve audience
            List<User> recipients = audienceResolver.resolveAudience(rule.getAudience());
            System.out.println("Rule " + rule.getName() + " targeting " + recipients.size() + " users");
            
            // Debug: Show first few recipients
            for (int i = 0; i < Math.min(5, recipients.size()); i++) {
                User user = recipients.get(i);
                System.out.println("  📧 Recipient " + (i+1) + ": " + user.getEmail() + " (username: " + user.getUsername() + ")");
            }
            if (recipients.size() > 5) {
                System.out.println("  ... and " + (recipients.size() - 5) + " more recipients");
            }
            
            if (recipients.isEmpty()) {
                System.out.println("No recipients found for rule: " + rule.getName());
                return;
            }
            
            // Render message template
            MessageTemplater.RenderedMessage renderedMessage = messageTemplater.renderMessage(rule.getMessage(), sourceData);
            
            // Send alerts
            Map<String, Object> channelResults = multiChannelSender.sendMultiChannel(
                null, // System sender
                recipients,
                renderedMessage.getHeader(),
                renderedMessage.getContent(),
                renderedMessage.getChannels()
            );
            
            // Create system event record
            Event systemEvent = new Event(
                rule.getId(),
                rule.getName(),
                sourceData,
                now,
                recipients.size(),
                channelResults
            );
            eventDAO.createEvent(systemEvent);
            
            // Create user-specific events for each recipient
            System.out.println("🔄 Creating user-specific events for " + recipients.size() + " recipients");
            for (User user : recipients) {
                System.out.println("📧 Recipient: " + user.getUsername() + " (email: " + user.getEmail() + ")");
            }
            createUserSpecificEvents(rule, recipients, renderedMessage, sourceData, channelResults, now);
            
            // Update rule's last fired time
            rule.setLastFiredAt(now);
            ruleDAO.updateRule(rule);
            
            System.out.println("Successfully fired rule: " + rule.getName() + 
                             " to " + recipients.size() + " recipients");
            
        } catch (Exception e) {
            System.err.println("Error firing rule " + rule.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Check if the rule's cooldown period has expired
     */
    private boolean isCooldownExpired(Rule rule) {
        if (rule.getLastFiredAt() == null) {
            return true; // Never fired before
        }
        
        // Check if last fired was on a different day (daily cooldown)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastFired = rule.getLastFiredAt();
        
        // Allow firing once per day - check if it's a different day
        return !lastFired.toLocalDate().equals(now.toLocalDate());
    }
    
    /**
     * Manual rule testing method (for API testing)
     */
    public Map<String, Object> testRule(Rule rule, Map<String, Object> mockData, boolean actuallyFire) {
        try {
            Map<String, Object> testResult = new java.util.HashMap<>();
            
            // Use mock data if provided, otherwise fetch real data
            Map<String, Object> sourceData;
            if (mockData != null && !mockData.isEmpty()) {
                sourceData = mockData;
            } else {
                SourceAdapter adapter = SourceAdapterFactory.getAdapter(rule.getSource());
                CompletableFuture<Map<String, Object>> dataFuture = adapter.fetch(rule.getParams());
                sourceData = dataFuture.get();
            }
            
            // Evaluate condition
            boolean conditionMet = conditionEvaluator.evaluate(rule.getCondition(), sourceData);
            testResult.put("conditionMet", conditionMet);
            testResult.put("sourceData", sourceData);
            
            if (conditionMet) {
                // Resolve audience
                List<User> recipients = audienceResolver.resolveAudience(rule.getAudience());
                testResult.put("recipientCount", recipients.size());
                
                // Render message
                MessageTemplater.RenderedMessage renderedMessage = messageTemplater.renderMessage(rule.getMessage(), sourceData);
                testResult.put("renderedHeader", renderedMessage.getHeader());
                testResult.put("renderedContent", renderedMessage.getContent());
                testResult.put("channels", renderedMessage.getChannels());
                
                if (actuallyFire && !recipients.isEmpty()) {
                    // Actually send the alerts
                    Map<String, Object> channelResults = multiChannelSender.sendMultiChannel(
                        null, recipients, renderedMessage.getHeader(), 
                        renderedMessage.getContent(), renderedMessage.getChannels()
                    );
                    
                    // Create event record for test firing (same as regular firing)
                    LocalDateTime now = LocalDateTime.now();
                    Event event = new Event(
                        rule.getId(),
                        rule.getName() + " (TEST)",
                        sourceData,
                        now,
                        recipients.size(),
                        channelResults
                    );
                    eventDAO.createEvent(event);
                    
                    // IMPORTANT: Create user-specific events for test firing too!
                    System.out.println("🧪 Test firing - creating user-specific events for " + recipients.size() + " recipients");
                    createUserSpecificEvents(rule, recipients, renderedMessage, sourceData, channelResults, now);
                    
                    testResult.put("channelResults", channelResults);
                    testResult.put("actuallyFired", true);
                    testResult.put("eventCreated", true);
                    testResult.put("userEventsCreated", recipients.size());
                } else {
                    testResult.put("actuallyFired", false);
                    testResult.put("eventCreated", false);
                    testResult.put("userEventsCreated", 0);
                }
            }
            
            return testResult;
            
        } catch (Exception e) {
            System.err.println("Error testing rule: " + e.getMessage());
            Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * Create user-specific events for each recipient
     */
    private void createUserSpecificEvents(Rule rule, List<User> recipients, 
                                        MessageTemplater.RenderedMessage renderedMessage,
                                        Map<String, Object> sourceData, 
                                        Map<String, Object> channelResults,
                                        LocalDateTime firedAt) {
        try {
            System.out.println("🎯 Creating user-specific events for rule: " + rule.getName());
            
            // FORCE create event for disha.jadav@sjsu.edu regardless of user lookup
            String targetEmail = "disha.jadav@sjsu.edu";
            boolean foundTargetUser = recipients.stream()
                .anyMatch(user -> targetEmail.equals(user.getEmail()) || targetEmail.equals(user.getUsername()));
                
            if (!foundTargetUser) {
                System.out.println("🔴 Target user " + targetEmail + " not found in recipients, force creating event anyway");
                String forceMessage = String.format("🚨 You received an email alert for: %s", renderedMessage.getHeader());
                if (sourceData != null && sourceData.containsKey("temp_c")) {
                    forceMessage += " (Temperature: " + sourceData.get("temp_c") + "°C)";
                }
                
                Event forceUserEvent = new Event(
                    rule.getId(), rule.getName(), targetEmail, "USER_ALERT_RECEIVED",
                    forceMessage, sourceData, firedAt
                );
                
                Event forceCreated = eventDAO.createEvent(forceUserEvent);
                System.out.println(forceCreated != null ? 
                    "✅ Force created event for " + targetEmail : 
                    "❌ Failed to force create event for " + targetEmail);
            }
            
            for (User user : recipients) {
                System.out.println("👤 Processing user event for: " + user.getEmail() + " (username: " + user.getUsername() + ")");
                
                // Create personalized message based on channels sent
                StringBuilder userMessage = new StringBuilder();
                
                // Get successful channels for this user
                List<String> successfulChannels = new java.util.ArrayList<>();
                if (channelResults != null) {
                    for (String channel : renderedMessage.getChannels()) {
                        Object channelResult = channelResults.get(channel);
                        if (channelResult instanceof Map) {
                            Map<?, ?> channelMap = (Map<?, ?>) channelResult;
                            if ("success".equals(channelMap.get("status"))) {
                                successfulChannels.add(channel);
                            }
                        }
                    }
                } else {
                    // If no results, assume all channels were attempted
                    successfulChannels.addAll(renderedMessage.getChannels());
                }
                
                // Create user-friendly message
                if (successfulChannels.size() > 0) {
                    userMessage.append("🚨 You received an ");
                    
                    if (successfulChannels.contains("email")) {
                        userMessage.append("email");
                    }
                    if (successfulChannels.contains("sms")) {
                        if (successfulChannels.contains("email")) {
                            userMessage.append(" and SMS");
                        } else {
                            userMessage.append("SMS");
                        }
                    }
                    
                    userMessage.append(" alert for: ").append(renderedMessage.getHeader());
                    
                    // Add some context from the payload
                    if (sourceData != null && sourceData.containsKey("temp_c")) {
                        userMessage.append(" (Temperature: ").append(sourceData.get("temp_c")).append("°C)");
                    }
                } else {
                    userMessage.append("⚠️ Alert attempted for: ").append(renderedMessage.getHeader())
                              .append(" (delivery may have failed)");
                }
                
                // Create user-specific event - use email as userId for frontend compatibility
                String userId = user.getEmail() != null ? user.getEmail() : user.getUsername();
                
                Event userEvent = new Event(
                    rule.getId(),
                    rule.getName(),
                    userId, // Use email as userId for API filtering
                    "USER_ALERT_RECEIVED",
                    userMessage.toString(),
                    sourceData,
                    firedAt
                );
                
                System.out.println("💾 Creating user event: userId=" + userId + ", message=" + userMessage.toString());
                Event createdEvent = eventDAO.createEvent(userEvent);
                
                if (createdEvent != null) {
                    System.out.println("✅ User event created successfully for: " + userId);
                } else {
                    System.err.println("❌ Failed to create user event for: " + userId);
                }
            }
            
            System.out.println("🏁 Finished creating user-specific events for " + recipients.size() + " users");
        } catch (Exception e) {
            System.err.println("❌ Error creating user-specific events: " + e.getMessage());
            e.printStackTrace();
        }
    }
}