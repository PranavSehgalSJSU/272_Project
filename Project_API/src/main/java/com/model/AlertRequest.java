package com.model;
///////////////////////////////////////////////////////////////////////////////////////////////////////    
//  FILE : User.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Is a User Model to encapsulate data 
///////////////////////////////////////////////////////////////////////////////////////////////////////

import java.util.List;

public class AlertRequest {    
    private String mode;
    private String token;
    private String header;
    private String sender;
    private String message;
    private String receiver;
    private List<String> receivers;

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

    public List<String> getReceivers() { return receivers;}
    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    
    public String getMessage() { return message;}

    
    public String getMode() { return mode;}
    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getHeader() { return header;}

}
