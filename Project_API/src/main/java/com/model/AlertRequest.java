package com.model;
///////////////////////////////////////////////////////////////////////////////////////////////////////    
//  FILE : User.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Is a User Model to encapsulate data 
///////////////////////////////////////////////////////////////////////////////////////////////////////
public class AlertRequest {
    private String sender;
    private String token;
    private String receiver;
    private String message;
    private String mode;

    public AlertRequest() {}

    // Getters & Setters

    public String getSender() { return sender;}
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getToken() { return token;}
    public void setToken(String token) {
        this.token = token;
    }

    public String getReceiver() { return receiver;}
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    
    public String getMessage() { return message;}
    public void setMessage(String message) {
        this.message = message;
    }

    
    public String getMode() { return mode;}
    public void setMode(String mode) {
        this.mode = mode;
    }
}
