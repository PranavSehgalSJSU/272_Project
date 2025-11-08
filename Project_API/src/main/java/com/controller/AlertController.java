package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AlertController.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Controller file to listen and redirect all requests for /alert
///////////////////////////////////////////////////////////////////////////////////////////////////////

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.model.AlertRequest;
import com.model.User;
import com.persistance.Email.EmailDAO;
import com.persistance.Email.EmailFileDAO;
import com.persistance.Users.UserDAO;
import com.persistance.Users.UserFileDAO;

@RestController
@RequestMapping("/alert")
public class AlertController {
    private final UserDAO userDAO = new UserFileDAO();
    private final EmailDAO emailDAO = new EmailFileDAO();
    private final AuthController authController = new AuthController();

    @PostMapping("/sendFromUser")
    public ResponseEntity<String> sendAlert(@RequestBody AlertRequest req) {
    
        User sender = userDAO.getUserByUsername(req.getSender());
        User receiver = userDAO.getUserByUsername(req.getReceiver());

        if (sender == null || !authController.isValidToken(sender.getUsername(), req.getToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid sender or token");
        }else if (receiver == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver not found");
        }else if (!receiver.isAllowAlerts()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(receiver.getUsername() + " has opted out of alerts");
        }  

        String mode   = userDAO.chooseMode(receiver, req.getMode());
        String header = emailDAO.checkHeader(req.getHeader());
        String resStr = (receiver.getUsername() + " hasn't verified this alert channel yet...");

        switch (mode) {
            case "email" -> {
                if (!receiver.isVerifiedEmail()){
                    return ResponseEntity.badRequest().body(resStr);
                }else{
                    emailDAO.sendEmailFromUser(sender,receiver,req.getMessage(), header);
                } 
            }case "phone" -> {
                if (!receiver.isVerifiedPhone()){
                    return ResponseEntity.badRequest().body(resStr);
                }
            }case "push" -> {
                if (receiver.getPushId() == null){
                    return ResponseEntity.badRequest().body(resStr);
                }
            }default -> {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid mode");
            }
        }
        return ResponseEntity.ok("Alert sent to " + receiver.getUsername() + " via " + mode);
    }
}
