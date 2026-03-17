package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Box;
import com.deliverysystem.domain.Order;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.BoxDto;
import com.deliverysystem.dto.CreateOrderRequest;
import com.deliverysystem.dto.FlagExceptionRequest;
import com.deliverysystem.dto.OrderAwaitingGoodsDto;
import com.deliverysystem.dto.RerouteOrderRequest;
import com.deliverysystem.dto.RouteDto;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.DashboardService;
import com.deliverysystem.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final DashboardService dashboardService;
    
    public OrderController(OrderService orderService, UserRepository userRepository, JwtTokenProvider tokenProvider, DashboardService dashboardService) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.dashboardService = dashboardService;
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
    
    @GetMapping("/awaiting-goods")
    public ResponseEntity<ApiResponse<List<OrderAwaitingGoodsDto>>> getOrdersAwaitingGoods(
            @RequestParam(required = false) String depotId) {
        
        List<OrderAwaitingGoodsDto> orders = dashboardService.getOrdersAwaitingGoods(depotId);
        
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @PostMapping("/boxes/{boxId}/receive")
    public ResponseEntity<ApiResponse<BoxDto>> receiveBox(
            @PathVariable String boxId,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Box box = orderService.receiveBox(boxId, user);
        BoxDto boxDto = toBoxDto(box);
        
        return ResponseEntity.ok(ApiResponse.success("Box received successfully", boxDto));
    }
    
    /**
     * Convert Box entity to BoxDto
     */
    private BoxDto toBoxDto(Box box) {
        BoxDto dto = new BoxDto();
        dto.setId(box.getId());
        
        // Convert status enum to string format expected by frontend
        String status = box.getStatus().name().toLowerCase();
        if (status.equals("received") || status.equals("manifested")) {
            dto.setStatus("received");
        } else if (status.equals("expected")) {
            dto.setStatus("pending");
        } else {
            dto.setStatus("missing");
        }
        
        if (box.getReceivedAt() != null) {
            dto.setReceivedAt(box.getReceivedAt().toString());
        }
        
        return dto;
    }
    
    @PostMapping("/{orderId}/flag-exception")
    public ResponseEntity<ApiResponse<Order>> flagException(
            @PathVariable String orderId,
            @Valid @RequestBody FlagExceptionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Order order = orderService.flagException(orderId, request.getReason(), user);
        
        return ResponseEntity.ok(ApiResponse.success("Order flagged with exception", order));
    }
    
    @PostMapping("/{orderId}/ready-for-manifest")
    public ResponseEntity<ApiResponse<Order>> markReadyForManifest(
            @PathVariable String orderId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = orderService.markReadyForManifest(orderId, user);

        return ResponseEntity.ok(ApiResponse.success("Order marked as ready for manifest", order));
    }

    @PutMapping("/{orderId}/route")
    public ResponseEntity<ApiResponse<RouteDto>> rerouteOrder(
            @PathVariable String orderId,
            @Valid @RequestBody RerouteOrderRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = tokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = orderService.rerouteOrder(orderId, request.getRouteId(), request.getReason(), user);

        RouteDto routeDto = new RouteDto();
        routeDto.setId(order.getRoute().getId());
        routeDto.setCode(order.getRoute().getCode());
        routeDto.setName(order.getRoute().getName());
        routeDto.setDepotId(order.getRoute().getDepot().getId());

        return ResponseEntity.ok(ApiResponse.success("Order re-routed successfully", routeDto));
    }
}
