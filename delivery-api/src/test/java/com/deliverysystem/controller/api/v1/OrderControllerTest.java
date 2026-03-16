package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Box;
import com.deliverysystem.domain.Order;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.repository.BoxRepository;
import com.deliverysystem.repository.OrderRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.DashboardService;
import com.deliverysystem.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = OrderController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private DashboardService dashboardService;

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-1");
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setRole(User.UserRole.DEPOT_MANAGER);

        validToken = "valid-jwt-token";

        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void receiveBox_ShouldUpdateBoxStatusToReceived() throws Exception {
        // Given
        String boxId = "box-123";
        Box box = new Box();
        box.setId(boxId);
        box.setIdentifier("BOX-001");
        box.setStatus(Box.BoxStatus.RECEIVED);
        box.setReceivedAt(java.time.LocalDateTime.now());

        when(orderService.receiveBox(boxId, testUser)).thenReturn(box);

        // When & Then
        mockMvc.perform(post("/api/v1/orders/boxes/{boxId}/receive", boxId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderService).receiveBox(boxId, testUser);
    }

    @Test
    void receiveBox_ShouldReturn404WhenBoxNotFound() throws Exception {
        // Given
        String boxId = "non-existent-box";
        when(orderService.receiveBox(boxId, testUser))
            .thenThrow(new IllegalArgumentException("Box not found: " + boxId));

        // When & Then
        mockMvc.perform(post("/api/v1/orders/boxes/{boxId}/receive", boxId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService).receiveBox(boxId, testUser);
    }

    @Test
    void receiveBox_ShouldReturn400WhenBoxAlreadyReceived() throws Exception {
        // Given
        String boxId = "box-123";
        when(orderService.receiveBox(boxId, testUser))
            .thenThrow(new IllegalStateException("Box " + boxId + " has already been received"));

        // When & Then
        mockMvc.perform(post("/api/v1/orders/boxes/{boxId}/receive", boxId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService).receiveBox(boxId, testUser);
    }

    @Test
    void receiveBox_ShouldRequireAuthentication() throws Exception {
        // Given
        String boxId = "box-123";

        // When & Then
        mockMvc.perform(post("/api/v1/orders/boxes/{boxId}/receive", boxId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).receiveBox(anyString(), any());
    }

    @Test
    void flagException_ShouldFlagOrderWithExceptionReason() throws Exception {
        // Given
        String orderId = "order-123";
        String reason = "Missing boxes";
        
        Order order = new Order();
        order.setId(orderId);
        order.setOrderId("ORD-001");
        order.setStatus("EXCEPTION");

        when(orderService.flagException(orderId, reason, testUser)).thenReturn(order);

        // When & Then
        mockMvc.perform(post("/api/v1/orders/{orderId}/flag-exception", orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"" + reason + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderService).flagException(orderId, reason, testUser);
    }

    @Test
    void flagException_ShouldReturn404WhenOrderNotFound() throws Exception {
        // Given
        String orderId = "non-existent-order";
        when(orderService.flagException(orderId, "Test reason", testUser))
            .thenThrow(new IllegalArgumentException("Order not found: " + orderId));

        // When & Then
        mockMvc.perform(post("/api/v1/orders/{orderId}/flag-exception", orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Test reason\"}"))
                .andExpect(status().isBadRequest());

        verify(orderService).flagException(orderId, "Test reason", testUser);
    }

    @Test
    void flagException_ShouldRequireAuthentication() throws Exception {
        // Given
        String orderId = "order-123";

        // When & Then
        mockMvc.perform(post("/api/v1/orders/{orderId}/flag-exception", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Test reason\"}"))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).flagException(anyString(), anyString(), any());
    }

    @Test
    void markReadyForManifest_ShouldMarkOrderAsReady() throws Exception {
        // Given
        String orderId = "order-123";
        
        Order order = new Order();
        order.setId(orderId);
        order.setOrderId("ORD-001");
        order.setStatus("READY_FOR_MANIFEST");

        when(orderService.markReadyForManifest(orderId, testUser)).thenReturn(order);

        // When & Then
        mockMvc.perform(post("/api/v1/orders/{orderId}/ready-for-manifest", orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(orderService).markReadyForManifest(orderId, testUser);
    }

    @Test
    void markReadyForManifest_ShouldReturn404WhenOrderNotFound() throws Exception {
        // Given
        String orderId = "non-existent-order";
        when(orderService.markReadyForManifest(orderId, testUser))
            .thenThrow(new IllegalArgumentException("Order not found: " + orderId));

        // When & Then
        mockMvc.perform(post("/api/v1/orders/{orderId}/ready-for-manifest", orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(orderService).markReadyForManifest(orderId, testUser);
    }

    @Test
    void markReadyForManifest_ShouldRequireAuthentication() throws Exception {
        // Given
        String orderId = "order-123";

        // When & Then
        mockMvc.perform(post("/api/v1/orders/{orderId}/ready-for-manifest", orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).markReadyForManifest(anyString(), any());
    }
}
