package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AlertController.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Controller file to listen and redirect all requests for /alert
///////////////////////////////////////////////////////////////////////////////////////////////////////
import com.model.AlertRequest;
import com.model.User;
import com.persistance.Users.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alert")
public class AlertController {
    private final UserDAO userDAO = new UserFileDAO();

    @PostMapping("/send")
    public ResponseEntity<String> sendAlert(@RequestBody AlertRequest req) {
        User sender = userDAO.getUserByUsername(req.getSender());
        if (sender == null || !req.getToken().equals("auth0|" + sender.getUsername() + "|mock-jwt-token")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid sender or token");
        }
        User receiver = userDAO.getUserByUsername(req.getReceiver());
        if (receiver == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver not found");

        if (!receiver.isAllowAlerts())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(receiver.getUsername() + " has opted out of alerts");

        String mode = req.getMode();
        if (mode == null || mode.isBlank()) {
            if (receiver.getEmail() != null && receiver.isVerifiedEmail()) mode = "email";
            else if (receiver.getPhone() != null && receiver.isVerifiedPhone()) mode = "phone";
            else if (receiver.getPushId() != null) mode = "push";
            else return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(receiver.getUsername() + " has no verified communication method");
        }

        switch (mode.toLowerCase()) {
            case "email":
                if (!receiver.isVerifiedEmail())
                    return ResponseEntity.badRequest().body(receiver.getUsername() + " hasn't verified this alert channel yet...");
                break;
            case "phone":
                if (!receiver.isVerifiedPhone())
                    return ResponseEntity.badRequest().body(receiver.getUsername() + " hasn't verified this alert channel yet...");
                break;
            case "push":
                if (receiver.getPushId() == null)
                    return ResponseEntity.badRequest().body(receiver.getUsername() + " hasn't verified this alert channel yet...");
                break;
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid mode");
        }
        return ResponseEntity.ok("Alert sent to " + receiver.getUsername() + " via " + mode);
    }
}
