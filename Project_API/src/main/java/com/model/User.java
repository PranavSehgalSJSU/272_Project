package com.model;
///////////////////////////////////////////////////////////////////////////////////////////////////////    
//  FILE : User.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Is a User Model to encapsulate data 
///////////////////////////////////////////////////////////////////////////////////////////////////////
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.List;
import java.util.ArrayList;

public class User {
    private ObjectId id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String pushId;
    private boolean verifiedEmail;
    private boolean verifiedPhone;
    private boolean allowAlerts; // true = can receive alerts from others
    private String city; // city for location-based alerts
    private List<String> tags; // tags for categorizing users (e.g., "delhi", "vip")
    private boolean isActive; // active user flag for alert targeting
    private boolean isAdmin; // admin role flag

    public User() {}

    public User(String username, String password, String email, String phone, String pushId, Boolean allowAlerts) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.pushId = pushId;
        this.allowAlerts = (allowAlerts != null) ? allowAlerts : true;
        this.verifiedEmail = false;
        this.verifiedPhone = false;
        this.city = "";
        this.tags = new ArrayList<>();
        this.isActive = true;
        this.isAdmin = false;
    }

    //All getters & setter: No i'm not gonne spend an eternity documenting each function...

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() { return username;}
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() { return password;}
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() { return email;}
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() { return phone;}
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPushId() { return pushId;}
    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public boolean isVerifiedEmail() { return verifiedEmail;}
    public void setVerifiedEmail(boolean verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }


    public boolean isVerifiedPhone() { return verifiedPhone;}
    public void setVerifiedPhone(boolean verifiedPhone) {
        this.verifiedPhone = verifiedPhone;
    }

    public boolean isAllowAlerts() { return allowAlerts;}
    public void setAllowAlerts(boolean allowAlerts) {
        this.allowAlerts = allowAlerts;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }

    public Document getDoc() {
        return new Document("username", this.getUsername())
                .append("password", this.getPassword())
                .append("email", this.getEmail())
                .append("phone", this.getPhone())
                .append("pushId", this.getPushId())
                .append("verifiedEmail", this.isVerifiedEmail())
                .append("verifiedPhone", this.isVerifiedPhone())
                .append("allowAlerts", this.isAllowAlerts())
                .append("city", this.getCity())
                .append("tags", this.getTags())
                .append("isActive", this.isActive())
                .append("isAdmin", this.isAdmin());
    }
}
