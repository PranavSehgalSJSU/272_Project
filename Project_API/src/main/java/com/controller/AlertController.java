package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AlertController.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Controller file to listen and redirect all requests for /alert
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.User;
import com.persistance.Sms.*;
import com.model.AlertRequest;
import com.persistance.Email.*;
import com.persistance.Users.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alert")
public class AlertController {
    private final SmsDAO smsDAO = new SmsFileDAO();
    private final UserDAO userDAO = new UserFileDAO();
    private final EmailDAO emailDAO = new EmailFileDAO();
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

        if (req.getReceivers() != null) {
            for (String name : req.getReceivers()) {
                if (name != null && !name.isBlank()){
                    targetUsernames.add(name);
                }
            }
        }

        List<User> finalTargets = new ArrayList<>();

        if (targetUsernames.contains("*")) {
            finalTargets.addAll(Arrays.asList(userDAO.getAlertingUsers()));
        } else {
            for (String username : targetUsernames) {
                User u = userDAO.getUserByUsername(username);
                if (u != null) {
                    finalTargets.add(u);
                }
            }
        }

        if (finalTargets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No valid receivers found");
        }

        String mode = req.getMode() != null ? req.getMode() : "";
        String header = emailDAO.checkHeader(req.getHeader());
        StringBuilder responseBuilder = new StringBuilder();

        for (User receiver : finalTargets) {


            String resStr = receiver.getUsername() + " hasn't verified this alert channel yet...";
            if(mode.equals("email") || mode.equals("*")){
                if (!receiver.isVerifiedEmail() && !targetUsernames.contains("*")) {
                    responseBuilder.append(resStr).append("\n");
                } else {
                    emailDAO.sendEmailFromUser(sender, receiver, req.getMessage(), header);
                    responseBuilder.append("Sent email to ").append(receiver.getUsername()).append("\n");
                }
            }
            if(mode.equals("phone") || mode.equals("*")){
                if (!receiver.isVerifiedPhone() && !targetUsernames.contains("*")) {
                    responseBuilder.append(resStr).append("\n");
                } else {
                    smsDAO.sendSmsTo(receiver.getPhone(), req.getMessage());
                    responseBuilder.append("Sent SMS to ").append(receiver.getUsername()).append("\n");
                }
            }
        }
        return ResponseEntity.ok(responseBuilder.toString());
    }
}