package com.alert.audience;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AudienceResolver.java
//  AUTHOR : Emergency Alert System
//  DESCRIPTION: Resolves rule audience to list of users
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.Rule;
import com.model.User;
import com.persistance.Users.UserDAO;
import com.persistance.Users.UserFileDAO;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class AudienceResolver {
    
    private final UserDAO userDAO;
    
    public AudienceResolver() {
        this.userDAO = new UserFileDAO();
    }
    
    public AudienceResolver(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * Resolve audience specification to list of eligible users
     * @param audience The audience specification from the rule
     * @return List of users matching the audience criteria
     */
    public List<User> resolveAudience(Rule.RuleAudience audience) {
        if (audience == null) {
            return new ArrayList<>();
        }
        
        // Get all active users who allow alerts
        List<User> allUsers = getAllActiveUsers();
        
        // Filter by city if specified
        List<User> filteredUsers = allUsers;
        if (audience.getCity() != null && !audience.getCity().trim().isEmpty()) {
            filteredUsers = filterByCity(filteredUsers, audience.getCity().trim());
        }
        
        // Filter by tags if specified
        if (audience.getTags() != null && !audience.getTags().isEmpty()) {
            filteredUsers = filterByTags(filteredUsers, audience.getTags());
        }
        
        return filteredUsers;
    }
    
    /**
     * Get all users who are active and allow alerts
     */
    public List<User> getAllActiveUsers() {
        try {
            List<User> allUsers = userDAO.getAllUsers();
            
            return allUsers.stream()
                    .filter(user -> user.isActive())
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            System.err.println("Error getting users: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Filter users by city
     */
    private List<User> filterByCity(List<User> users, String targetCity) {
        return users.stream()
                .filter(user -> {
                    String userCity = user.getCity();
                    return userCity != null && userCity.trim().equalsIgnoreCase(targetCity);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Filter users by tags - user must have at least one matching tag
     */
    private List<User> filterByTags(List<User> users, List<String> requiredTags) {
        return users.stream()
                .filter(user -> {
                    List<String> userTags = user.getTags();
                    if (userTags == null || userTags.isEmpty()) {
                        return false;
                    }
                    
                    // Check if user has any of the required tags
                    return requiredTags.stream()
                            .anyMatch(requiredTag -> userTags.stream()
                                    .anyMatch(userTag -> userTag.equalsIgnoreCase(requiredTag)));
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Count how many users would be targeted by this audience
     */
    public int countAudience(Rule.RuleAudience audience) {
        return resolveAudience(audience).size();
    }
}