package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.UserDto;
import com.deliverysystem.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
    
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<UserDto> userDtos = users.stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole().name());
            dto.setDepotId(user.getDepotId());
            
            // Format last login
            if (user.getLastLogin() != null) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime lastLogin = user.getLastLogin();
                
                if (lastLogin.toLocalDate().equals(now.toLocalDate())) {
                    dto.setLastLogin("Today " + lastLogin.format(DateTimeFormatter.ofPattern("HH:mm")));
                } else if (lastLogin.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                    dto.setLastLogin("Yesterday");
                } else {
                    dto.setLastLogin(lastLogin.format(DATE_FORMATTER));
                }
            } else {
                dto.setLastLogin("Never");
            }
            
            dto.setStatus(user.getStatus());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(userDtos));
    }
}
