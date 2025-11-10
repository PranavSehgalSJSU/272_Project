package com.persistance.Sms;

///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : SmsDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Interface for sending and verifying SMS messages
///////////////////////////////////////////////////////////////////////////////////////////////////////

public interface SmsDAO {
    void sendSmsTo(String phoneNumber, String content);
    void sendVerificationSms(String username, String phoneNumber);
}
