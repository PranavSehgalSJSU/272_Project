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

import com.model.User;
import com.persistance.Database.MongoDBConn;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;



public class UserFileDAO implements UserDAO {
    private final MongoCollection<Document> users = MongoDBConn.getDatabase().getCollection("users");

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
                .append("allowAlerts", user.isAllowAlerts());
        users.insertOne(doc);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByUsername(String username) {
        Document doc = users.find(Filters.eq("username", username)).first();
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
        return user;
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
                        .append("allowAlerts", user.isAllowAlerts())));
    }
}
