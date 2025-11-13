///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : EmailFileDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Sends email using SMTP via PropertyReader
///////////////////////////////////////////////////////////////////////////////////////////////////////

package com.persistance.Email;

import java.util.Properties;
import java.util.regex.Pattern;

import com.model.User;
import com.persistance.Database.PropertyReader;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailFileDAO implements EmailDAO {

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void sendEmailTo(String email, String content, String header) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", PropertyReader.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", PropertyReader.getProperty("mail.smtp.port"));
        
        final String from = PropertyReader.getProperty("mail.smtp.username");
        final String password = PropertyReader.getProperty("mail.smtp.password");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(header);
            message.setText(content);

            Transport.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && Pattern.matches(emailRegex, email);
    }

    @Override
    public void sendVerificationEmail(String username, String email) {
        String apiLink = PropertyReader.getProperty("api.link");
        String verifyLink = (apiLink+"/auth/verify?username=" + username + "&type=email").replaceAll(" ", "%20");

        String mailBody = "Hello " + username + ",\n\n"
                + "Please verify your email address by clicking the link below:\n"
                + verifyLink + "\n\n"
                + "If you did not create this account, please ignore this email.\n\n"
                + "Thanks,\nME";

        sendEmailTo(email, mailBody,"Alerter Email Verification");
    }

    @Override
    public void sendEmailFromUser(User sender, User reciever, String message, String header) {
        message =   "The following message was sent from "
                    +sender.getUsername() + " : \n\n"
                    +message;
        sendEmailTo(reciever.getEmail(), message, header);
    }

    @Override
    public String checkHeader(String header) {
        if (!( (header==null) || (header.isBlank()))) {
            return header;
        }
        return "Alerter Email Ping";
    }
}
