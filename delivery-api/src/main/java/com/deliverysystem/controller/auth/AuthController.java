package com.deliverysystem.controller.auth;

import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CurrentUserDto;
import com.deliverysystem.dto.LoginRequest;
import com.deliverysystem.dto.LoginResponse;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    
    public AuthController(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        if (!userService.validatePassword(user, request.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalStateException("User account is not active");
        }
        
        String token = tokenProvider.generateToken(
            user.getUsername(),
            user.getRole().name(),
            user.getDepotId()
        );
        
        CurrentUserDto currentUser = new CurrentUserDto();
        currentUser.setName(user.getName());
        currentUser.setRole(user.getRole().name());
        currentUser.setDepotId(user.getDepotId());
        // Generate initials from name
        String[] nameParts = user.getName().split(" ");
        if (nameParts.length >= 2) {
            currentUser.setInitials(nameParts[0].substring(0, 1) + nameParts[1].substring(0, 1));
        } else if (nameParts.length == 1) {
            currentUser.setInitials(nameParts[0].substring(0, 1));
        }
        
        userService.updateLastLogin(user.getId());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(currentUser);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserDto>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        CurrentUserDto currentUser = new CurrentUserDto();
        currentUser.setName(user.getName());
        currentUser.setRole(user.getRole().name());
        currentUser.setDepotId(user.getDepotId());
        String[] nameParts = user.getName().split(" ");
        if (nameParts.length >= 2) {
            currentUser.setInitials(nameParts[0].substring(0, 1) + nameParts[1].substring(0, 1));
        } else if (nameParts.length == 1) {
            currentUser.setInitials(nameParts[0].substring(0, 1));
        }
        
        return ResponseEntity.ok(ApiResponse.success(currentUser));
    }
}
