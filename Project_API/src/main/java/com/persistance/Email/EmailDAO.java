package com.persistance.Email;

///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : EmailDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Interface for sending emails
///////////////////////////////////////////////////////////////////////////////////////////////////////
import com.model.User;

public interface EmailDAO {
    boolean isValidEmail(String email);
    String checkHeader(String header);
    void sendEmailTo(String email, String content, String header);
    void sendVerificationEmail(String username, String email);
    void sendEmailFromUser(User sender, User reciever, String message, String header);
}
