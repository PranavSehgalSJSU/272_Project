package com.model;
///////////////////////////////////////////////////////////////////////////////////////////////////////    
//  FILE : User.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Is a User Model to encapsulate data 
///////////////////////////////////////////////////////////////////////////////////////////////////////
import org.bson.Document;
import org.bson.types.ObjectId;

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

    public Document getDoc() {
        return new Document("username", this.getUsername())
                .append("password", this.getPassword())
                .append("email", this.getEmail())
                .append("phone", this.getPhone())
                .append("pushId", this.getPushId())
                .append("verifiedEmail", this.isVerifiedEmail())
                .append("verifiedPhone", this.isVerifiedPhone())
                .append("allowAlerts", this.isAllowAlerts());
    }
}
