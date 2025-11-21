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

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.model.User;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.persistance.Database.MongoConn;



public class UserFileDAO implements UserDAO {
    private final MongoCollection<Document> users;
    private final boolean useInMemoryFallback;
    
    // In-memory fallback users when MongoDB is not connected
    private static final java.util.List<User> inMemoryUsers = new java.util.ArrayList<>();
    
    static {
        // Initialize some default users for testing when database is not connected
        User testUser1 = new User();
        testUser1.setUsername("disha.jadav@sjsu.edu");
        testUser1.setEmail("disha.jadav@sjsu.edu");
        testUser1.setPhone("+1234567890");
        testUser1.setCity("San Jose");
        testUser1.setAllowAlerts(true);
        testUser1.setActive(true);
        testUser1.setAdmin(false);
        testUser1.setTags(java.util.Arrays.asList("emergency", "weather"));
        inMemoryUsers.add(testUser1);
        
        User testUser2 = new User();
        testUser2.setUsername("admin@alert.com");
        testUser2.setEmail("admin@alert.com");
        testUser2.setPhone("+1234567891");
        testUser2.setCity("San Jose");
        testUser2.setAllowAlerts(true);
        testUser2.setActive(true);
        testUser2.setAdmin(true);
        testUser2.setTags(java.util.Arrays.asList("admin", "emergency"));
        inMemoryUsers.add(testUser2);
    }
    
    public UserFileDAO() {
        com.mongodb.client.MongoDatabase database = MongoConn.getDatabase();
        if (database != null) {
            this.users = database.getCollection("users");
            this.useInMemoryFallback = false;
            System.out.println("✅ UserDAO connected to MongoDB");
        } else {
            this.users = null;
            this.useInMemoryFallback = true;
            System.out.println("⚠️ UserDAO using in-memory fallback (" + inMemoryUsers.size() + " users)");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User createUser(User user) {
        if (useInMemoryFallback) {
            inMemoryUsers.add(user);
            System.out.println("➕ User added to in-memory storage: " + user.getUsername());
            return user;
        }
        
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
        if (useInMemoryFallback) {
            return inMemoryUsers.stream()
                    .filter(user -> username.equals(user.getUsername()))
                    .findFirst()
                    .orElse(null);
        }
        
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
        if (useInMemoryFallback) {
            return inMemoryUsers.stream()
                    .anyMatch(user -> email.equals(user.getEmail()));
        }
        
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
        if (useInMemoryFallback) {
            return inMemoryUsers.stream()
                    .anyMatch(user -> phone.equals(user.getPhone()));
        }
        
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
        if (useInMemoryFallback) {
            for (int i = 0; i < inMemoryUsers.size(); i++) {
                if (user.getUsername().equals(inMemoryUsers.get(i).getUsername())) {
                    inMemoryUsers.set(i, user);
                    System.out.println("🔄 Updated in-memory user: " + user.getUsername());
                    return;
                }
            }
            return;
        }
        
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
        if (useInMemoryFallback) {
            System.out.println("📋 Returning " + inMemoryUsers.size() + " in-memory users");
            return new java.util.ArrayList<>(inMemoryUsers);
        }
        
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
