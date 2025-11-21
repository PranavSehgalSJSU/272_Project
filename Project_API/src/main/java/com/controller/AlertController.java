package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AlertController.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Controller file to listen and redirect all requests for /alert
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.User;
import com.model.Event;
import com.persistance.Sms.*;
import com.model.AlertRequest;
import com.persistance.Email.*;
import com.persistance.Users.*;
import com.persistance.Event.EventDAO;
import com.persistance.Event.EventFileDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alert")
public class AlertController {
    private final SmsDAO smsDAO = new SmsFileDAO();
    private final UserDAO userDAO = new UserFileDAO();
    private final EmailDAO emailDAO = new EmailFileDAO();
    private final EventDAO eventDAO = new EventFileDAO();
    private final AuthController authController = new AuthController();

    @PostMapping("/sendFromUser")
    public ResponseEntity<String> sendAlert(@RequestBody AlertRequest req) {

        User sender = userDAO.getUserByUsername(req.getSender());
        if (sender == null || !authController.isValidToken(sender.getUsername(), req.getToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid sender or token");
        }

        Set<String> targetUsernames = new HashSet<>();
        
        if (req.getReceiver() != null && !req.getReceiver().isBlank()) {
            targetUsernames.add(req.getReceiver());
        }

        boolean alertSent = false;
        Map<String, Object> channelResults = new HashMap<>();

        switch (mode) {
            case "email" -> {
                if (!receiver.isVerifiedEmail()){
                    return ResponseEntity.badRequest().body(resStr);
                }else{
                    emailDAO.sendEmailFromUser(sender,receiver,req.getMessage(), header);
                    channelResults.put("email", "sent");
                    alertSent = true;
                } 
            }case "phone" -> {
                if (!receiver.isVerifiedPhone()){
                    return ResponseEntity.badRequest().body(resStr);
                }else{
                    smsDAO.sendSmsTo(receiver.getPhone(), req.getMessage());
                    channelResults.put("sms", "sent");
                    alertSent = true;
                } 
            }case "push" -> {
                if (receiver.getPushId() == null){
                    return ResponseEntity.badRequest().body(resStr);
                } else {
                    // Push notification logic would go here
                    channelResults.put("push", "sent");
                    alertSent = true;
                }
            }
        }

        // Create event record for manual user alert
        if (alertSent) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("sender", sender.getUsername());
                payload.put("receiver", receiver.getUsername());
                payload.put("message", req.getMessage());
                payload.put("mode", mode);
                payload.put("type", "user_to_user");

                Event event = new Event(
                    null, // No rule ID for manual alerts
                    "User Alert: " + sender.getUsername() + " → " + receiver.getUsername(),
                    payload,
                    LocalDateTime.now(),
                    1, // One recipient
                    channelResults
                );
                eventDAO.createEvent(event);
            } catch (Exception e) {
                System.err.println("Failed to create event record: " + e.getMessage());
                // Don't fail the alert if event creation fails
            }
        }

        return ResponseEntity.ok("Alert sent to " + receiver.getUsername() + " via " + mode);
    }
}