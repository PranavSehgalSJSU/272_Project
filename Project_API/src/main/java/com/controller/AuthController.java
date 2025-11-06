package com.controller;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : AuthController.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//
//  DESCRIPTION: Controller file to listen and redirect all requests for /auth
///////////////////////////////////////////////////////////////////////////////////////////////////////
import com.model.User;
import com.persistance.Users.UserDAO;
import com.persistance.Users.UserFileDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserDAO userDAO = new UserFileDAO();
    
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password are required");
        }

        if (userDAO.getUserByUsername(user.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        userDAO.createUser(user);
        return ResponseEntity.ok("User registered successfully. Please verify email/phone.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username and password are required");
        }

        User storedUser = userDAO.getUserByUsername(loginRequest.getUsername());
        if (storedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        if (!storedUser.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        // TODO: Integrate with Auth0 API here to validate credentials and generate JWT token
        // For now, simulate token generation:
        String fakeJwt = "fake-jwt-token-for-" + storedUser.getUsername();

        return ResponseEntity.ok("{\"token\": \"" + fakeJwt + "\"}");
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String username, @RequestParam String type) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        if ("email".equalsIgnoreCase(type)) user.setVerifiedEmail(true);
        else if ("phone".equalsIgnoreCase(type)) user.setVerifiedPhone(true);
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid type");

        userDAO.updateUser(user);
        return ResponseEntity.ok("Verification successful for " + type);
    }
}
