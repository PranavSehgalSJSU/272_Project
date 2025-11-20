package com.persistance.Users;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : UserFileDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: IMPLEMENTS UserDAO interface
//               DEFINES functions declared in the DAO file with @override
//               DECLARE functions and variables not in FileDAO as private
//               DECLARE functions and variables not in FileDAO as public static if they need to be
//                      accessed elsewher
//
///////////////////////////////////////////////////////////////////////////////////////////////////////

import org.bson.Document;

import com.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.persistance.Database.MongoConn;



public class UserFileDAO implements UserDAO {
    private final MongoCollection<Document> users = MongoConn.getDatabase().getCollection("users");

    /**
     * {@inheritDoc}
     */
    @Override
    public User createUser(User user) {
        Document doc = new Document("username", user.getUsername())
                .append("password", user.getPassword())
                .append("email", user.getEmail())
                .append("phone", user.getPhone())
                .append("pushId", user.getPushId())
                .append("verifiedEmail", false)
                .append("verifiedPhone", false)
                .append("allowAlerts", user.isAllowAlerts())
                .append("isAdmin", user.isAdmin())
                .append("city", user.getCity())
                .append("tags", user.getTags())
                .append("isActive", user.isActive());
        users.insertOne(doc);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByUsername(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
        if (doc == null){return null;}
        User user = new User();
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setPhone(doc.getString("phone"));
        user.setPushId(doc.getString("pushId"));
        user.setVerifiedEmail(doc.getBoolean("verifiedEmail", false));
        user.setVerifiedPhone(doc.getBoolean("verifiedPhone", false));
        user.setAllowAlerts(doc.getBoolean("allowAlerts", true));
        user.setAdmin(doc.getBoolean("isAdmin", false));
        user.setCity(doc.getString("city"));
        user.setActive(doc.getBoolean("isActive", true));
        
        // Handle tags list
        java.util.List<String> tags = doc.getList("tags", String.class);
        if (tags != null) {
            user.setTags(tags);
        }
        
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean emailIsInUse(String email) {
        Document doc = users.find(Filters.eq("email", email)).first();
        if (doc != null){
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean phoneIsInUse(String phone) {
        Document doc = users.find(Filters.eq("phone", phone)).first();
        if (doc != null){
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(User user) {
        users.updateOne(Filters.eq("username", user.getUsername()),
                new Document("$set", new Document()
                        .append("verifiedEmail", user.isVerifiedEmail())
                        .append("verifiedPhone", user.isVerifiedPhone())
                        .append("allowAlerts", user.isAllowAlerts())
                        .append("isAdmin", user.isAdmin())
                        .append("city", user.getCity())
                        .append("tags", user.getTags())
                        .append("isActive", user.isActive())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String chooseMode(User user, String mode) {
        if (!(mode == null || mode.isBlank())){
            return mode.toLowerCase();
        }else if (user.getEmail() != null && user.isVerifiedEmail()){
            return "email";
        }else if (user.getPhone() != null && user.isVerifiedPhone()){ 
            return "phone";
        }else if (user.getPushId() != null){
            return "push";
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.List<User> getAllUsers() {
        java.util.List<User> usersList = new java.util.ArrayList<>();
        try {
            for (Document doc : users.find()) {
                User user = documentToUser(doc);
                if (user != null) {
                    usersList.add(user);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return usersList;
    }
    
    private User documentToUser(Document doc) {
        if (doc == null) return null;
        
        User user = new User();
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setEmail(doc.getString("email"));
        user.setPhone(doc.getString("phone"));
        user.setPushId(doc.getString("pushId"));
        user.setVerifiedEmail(doc.getBoolean("verifiedEmail", false));
        user.setVerifiedPhone(doc.getBoolean("verifiedPhone", false));
        user.setAllowAlerts(doc.getBoolean("allowAlerts", true));
        user.setAdmin(doc.getBoolean("isAdmin", false));
        user.setCity(doc.getString("city"));
        user.setActive(doc.getBoolean("isActive", true));
        
        // Handle tags list
        java.util.List<String> tags = doc.getList("tags", String.class);
        if (tags != null) {
            user.setTags(tags);
        }
        
        return user;
    }
}
