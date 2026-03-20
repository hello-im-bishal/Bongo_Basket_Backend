package com.example.demo.admincontrollers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.adminservices.AdminUserService;
import com.example.demo.entity.User;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin(origins = "https://hello-im-bishal.github.io")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    // ==========================================
    // MODIFY USER ROLE (ADMIN <-> CUSTOMER)
    // ==========================================
    @PutMapping("/modify")
    public ResponseEntity<?> modifyUser(@RequestBody Map<String, Object> userRequest) {
        try {
            // Safe extraction of userId and roleName
            Integer userId = userRequest.get("userId") != null 
                ? Integer.valueOf(userRequest.get("userId").toString()) 
                : null;
                
            String role = userRequest.get("role") != null 
                ? userRequest.get("role").toString() 
                : null;

            // This service call now allows ADMIN -> CUSTOMER and vice-versa
            User updatedUser = adminUserService.modifyUser(userId, role);

            // Clean response object for the frontend
            Map<String, Object> response = new HashMap<>();
            response.put("userId", updatedUser.getUserId());
            response.put("role", updatedUser.getRole().name()); // Will return "ADMIN" or "CUSTOMER"
            response.put("updatedAt", updatedUser.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // This will return the "Invalid role... Use ADMIN or CUSTOMER" message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while updating the user role");
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId) {
        try {
            User user = adminUserService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        try {
            adminUserService.deleteUserById(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }
}