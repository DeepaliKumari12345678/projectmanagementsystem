package com.zosh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zosh.exception.UserException;
import com.zosh.model.PasswordResetToken;
import com.zosh.model.User;
import com.zosh.request.ResetPasswordRequest;
import com.zosh.response.ApiResponse;
import com.zosh.service.PasswordResetTokenService;
import com.zosh.service.UserService;

@RestController
@RequestMapping("/reset-password")
public class ResetPasswordController {

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private UserService userService;

    // Method to handle actual password reset via token
    @PostMapping
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestBody ResetPasswordRequest req) throws UserException {
        
        PasswordResetToken resetToken = passwordResetTokenService.findByToken(req.getToken());

        // Check if the token is valid
        if (resetToken == null) {
            throw new UserException("Invalid or missing token.");
        }
        
        // Check if the token is expired
        if (resetToken.isExpired()) {
            passwordResetTokenService.delete(resetToken);
            throw new UserException("Token has expired.");
        }

        // Update user's password
        User user = resetToken.getUser();
        userService.updatePassword(user, req.getPassword());

        // Delete the token after a successful password reset
        passwordResetTokenService.delete(resetToken);
        
        // Prepare a successful response
        ApiResponse res = new ApiResponse("Password updated successfully." ,true);
        res.setMessage("Password updated successfully.");
        res.setStatus(true);

        return ResponseEntity.ok(res);
    }
    
    // Method to trigger password reset email
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> resetPassword1(@RequestParam("email") String email) throws UserException {
        User user = userService.findUserByEmail(email);

        if (user == null) {
            throw new UserException("User not found with this email address.");
        }

        // Send password reset email
        userService.sendPasswordResetEmail(user);

        // Prepare a successful response
        ApiResponse res = new ApiResponse("Password reset email sent successfully." , true);
        res.setMessage("Password reset email sent successfully.");
        res.setStatus(true);

        return ResponseEntity.ok(res);
    }
}
