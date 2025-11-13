package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AuthController.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Controller file to listen and redirect all requests for /auth
///////////////////////////////////////////////////////////////////////////////////////////////////////

import com.model.User;
import java.util.HashMap;
import com.model.Auth0Model;
import com.persistance.Sms.*;
import com.persistance.Email.*;
import com.persistance.Users.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
///////////////////////////////////////////////////////////////////////////////////////////////////////

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final SmsDAO smsDAO = new SmsFileDAO();
    private final UserDAO userDAO = new UserFileDAO();
    private final EmailDAO emailDAO = new EmailFileDAO();
    
    private static HashMap<String, String> tokenMap = new HashMap<>();

    /**
     * 
     * @param user
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user){
        
        String email = user.getEmail();
        String phone = user.getPhone();
        String username = user.getUsername();
        System.out.println("/auth/signup/ \t"+username);

        if (username == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password are required");
        }else if (userDAO.getUserByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }else if(userDAO.emailIsInUse(email)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }else if(userDAO.phoneIsInUse(phone)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Phone already in use");
        }
        user.setAllowAlerts(true);
        userDAO.createUser(user);

        if(emailDAO.isValidEmail(email)){
            emailDAO.sendVerificationEmail(username, email);
        }
        if(phone!=null && !phone.isBlank()){
            smsDAO.sendVerificationSms(username, phone);
        }
        
        return ResponseEntity.ok("User registered successfully. Please verify email/phone.");
    }

    /**
     * 
     * @param user
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        String username = user.getUsername();
        System.out.println("/auth/login/ \t"+username);
        if (username == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password are required");
        }

        User storedUser = userDAO.getUserByUsername(user.getUsername());
        if (storedUser == null || !storedUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        // Generate or reuse token
        String token = tokenMap.get(user.getUsername());
        if (token == null) {
            token = Auth0Model.getToken();
            tokenMap.put(user.getUsername(), token);
        }

        return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
    }

    /**
     * 
     * @param username
     * @param type
     * @return
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String username, @RequestParam String type) {
        System.out.println("/auth/verify/ \t"+username);
        User user = userDAO.getUserByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        if ("email".equalsIgnoreCase(type)) user.setVerifiedEmail(true);
        else if ("phone".equalsIgnoreCase(type)) user.setVerifiedPhone(true);
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid type");

        userDAO.updateUser(user);
        return ResponseEntity.ok("Verification successful for " + type);
    }

    public boolean isValidToken(String username, String token) {
        return tokenMap.containsKey(username) && tokenMap.get(username).equals(token);
    }
}
