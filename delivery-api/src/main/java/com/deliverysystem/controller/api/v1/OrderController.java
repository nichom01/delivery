package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Order;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.CreateOrderRequest;
import com.deliverysystem.dto.RouteDto;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    
    public OrderController(OrderService orderService, UserRepository userRepository, JwtTokenProvider tokenProvider) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<RouteDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Get user from token or use API user
        User user = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = tokenProvider.getUsernameFromToken(token);
            user = userRepository.findByUsername(username).orElse(null);
        }
        
        // For API key authentication, create a system user
        if (user == null) {
            user = new User();
            user.setId("api-user");
            user.setName("API User");
            user.setRole(User.UserRole.CENTRAL_ADMIN);
        }
        
        Order order = orderService.createOrder(request, user);
        
        RouteDto routeDto = new RouteDto();
        routeDto.setId(order.getRoute().getId());
        routeDto.setCode(order.getRoute().getCode());
        routeDto.setName(order.getRoute().getName());
        routeDto.setDepotId(order.getRoute().getDepot().getId());
        
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", routeDto));
    }
}
