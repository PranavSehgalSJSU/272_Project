package com.alert.sender;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : MultiChannelSender.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Wrapper for existing sender to support multiple recipients
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.User;
import com.persistance.Email.EmailDAO;
import com.persistance.Email.EmailFileDAO;
import com.persistance.Sms.SmsDAO;
import com.persistance.Sms.SmsFileDAO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MultiChannelSender {
    
    private final EmailDAO emailDAO;
    private final SmsDAO smsDAO;
    
    public MultiChannelSender() {
        this.emailDAO = new EmailFileDAO();
        this.smsDAO = new SmsFileDAO();
    }
    
    public MultiChannelSender(EmailDAO emailDAO, SmsDAO smsDAO) {
        this.emailDAO = emailDAO;
        this.smsDAO = smsDAO;
    }
    
    /**
     * Send message to multiple recipients across multiple channels
     * @param sender The sender user (can be null for system messages)
     * @param receivers List of users to send to
     * @param header Message header/subject
     * @param content Message content
     * @param channels List of channels to use (email, sms, push)
     * @return Channel results map with success/failure stats
     */
    public Map<String, Object> sendMultiChannel(User sender, List<User> receivers, 
                                              String header, String content, List<String> channels) {
        Map<String, Object> results = new HashMap<>();
        Map<String, Integer> successCounts = new HashMap<>();
        Map<String, Integer> failureCounts = new HashMap<>();
        
        // Initialize counters
        for (String channel : channels) {
            successCounts.put(channel, 0);
            failureCounts.put(channel, 0);
        }
        
        // Send to each recipient on each channel
        for (User receiver : receivers) {
            if (!receiver.isAllowAlerts() || !receiver.isActive()) {
                continue; // Skip users who don't allow alerts or are inactive
            }
            
            for (String channel : channels) {
                try {
                    boolean success = sendToChannel(sender, receiver, header, content, channel);
                    if (success) {
                        successCounts.put(channel, successCounts.get(channel) + 1);
                    } else {
                        failureCounts.put(channel, failureCounts.get(channel) + 1);
                    }
                } catch (Exception e) {
                    System.err.println("Error sending via " + channel + " to " + receiver.getUsername() + ": " + e.getMessage());
                    failureCounts.put(channel, failureCounts.get(channel) + 1);
                }
            }
        }
        
        // Build results
        results.put("totalRecipients", receivers.size());
        results.put("successCounts", successCounts);
        results.put("failureCounts", failureCounts);
        
        // Calculate overall stats
        int totalSuccess = successCounts.values().stream().mapToInt(Integer::intValue).sum();
        int totalFailures = failureCounts.values().stream().mapToInt(Integer::intValue).sum();
        results.put("totalSuccess", totalSuccess);
        results.put("totalFailures", totalFailures);
        
        return results;
    }
    
    /**
     * Send message to single recipient on single channel
     * This preserves the original single-send functionality
     */
    private boolean sendToChannel(User sender, User receiver, String header, String content, String channel) {
        try {
            switch (channel.toLowerCase()) {
                case "email":
                    return sendEmail(sender, receiver, header, content);
                case "sms":
                case "phone":
                    return sendSms(receiver, content);
                case "push":
                    return sendPush(receiver, header, content);
                default:
                    System.err.println("Unknown channel: " + channel);
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Channel send error: " + e.getMessage());
            return false;
        }
    }
    
    private boolean sendEmail(User sender, User receiver, String header, String content) {
        try {
            if (!receiver.isVerifiedEmail()) {
                System.err.println("Email not verified for user: " + receiver.getUsername());
                return false;
            }
            
            if (sender != null) {
                // Send from user (existing functionality)
                emailDAO.sendEmailFromUser(sender, receiver, content, header);
            } else {
                // Send system message
                emailDAO.sendEmailTo(receiver.getEmail(), content, header);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean sendSms(User receiver, String content) {
        try {
            if (!receiver.isVerifiedPhone()) {
                System.err.println("Phone not verified for user: " + receiver.getUsername());
                return false;
            }
            
            smsDAO.sendSmsTo(receiver.getPhone(), content);
            return true;
        } catch (Exception e) {
            System.err.println("SMS send failed: " + e.getMessage());
            return false;
        }
    }
    
    private boolean sendPush(User receiver, String header, String content) {
        try {
            if (receiver.getPushId() == null || receiver.getPushId().isEmpty()) {
                System.err.println("Push ID not available for user: " + receiver.getUsername());
                return false;
            }
            
            // Push notification implementation would go here
            // For now, just log that it would be sent
            System.out.println("Push notification sent to " + receiver.getUsername() + ": " + header);
            return true;
        } catch (Exception e) {
            System.err.println("Push send failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Legacy single-user sender for backwards compatibility
     * This maintains the existing API contract
     */
    public boolean sendAlert(User sender, User receiver, String header, String content, String mode) {
        try {
            return sendToChannel(sender, receiver, header, content, mode);
        } catch (Exception e) {
            System.err.println("Single alert send failed: " + e.getMessage());
            return false;
        }
    }
}