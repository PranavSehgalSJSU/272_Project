package com.persistance.Sms;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : SmsFileDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Sends free SMS via email-to-SMS gateways for all 4 major carriers.
///////////////////////////////////////////////////////////////////////////////////////////////////////


import java.util.List;
import java.util.Arrays;
import com.persistance.Email.EmailFileDAO;

public class SmsFileDAO implements SmsDAO {

    private final EmailFileDAO emailDAO = new EmailFileDAO();

    private static final List<String> carriers = Arrays.asList(
        "@txt.att.net",
        "@mail.usmobile.com",
        "@tmomail.net",
        "@vtext.com",
        "@messaging.sprintpcs.com",
        "@sms.myboostmobile.com",
        "@sms.cricketwireless.net",
        "@mymetropcs.com",
        "@email.uscc.net",
        "@vmobl.com",
        "@mmst5.tracfone.com",
        "@message.ting.com",
        "@msg.fi.google.com",
        "@mailmymobile.net",
        "@text.republicwireless.com",
        "@cspire1.com",
        "@smtext.com",
        "@text.freedompop.com",
        "@vtext.com"
    );

    @Override
    public void sendSmsTo(String phoneNumber, String message) {
        //if (!isValidPhone(phoneNumber)) {return;}

        for (String domain : carriers) {
            String smsEmail = phoneNumber + domain;
            System.out.println(smsEmail);
            try {
                emailDAO.sendEmailTo(smsEmail, message, "");
            } catch (Exception e) {
                System.err.println("Unable to send with: "+domain);
            }
        }
        System.out.println("Send End?");
    }

    @Override
    public void sendVerificationSms(String username, String phoneNumber) {
        String verificationLink = "http://localhost:8080/auth/verify?username=" + username + "&type=phone";
        String body = "Hey, " + username + ",\n\n"
                + "This is your test verification SMS!\n"
                + "Click below to verify your phone number:\n\n"
                + verificationLink + "\n\n"
                + "If you didn’t request this, please ignore it.\n\n"
                + "Yours Sincerely\n,"
                + "Pranav Sehgal Security System Incorporated\n";
            
        System.out.println(body);
        sendSmsTo(phoneNumber, body);
    }

    @Override
    public boolean isValidPhone(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+?[0-9]{10,15}$");
    }
}
