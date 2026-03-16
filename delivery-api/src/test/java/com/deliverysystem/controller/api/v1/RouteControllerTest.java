package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.repository.DepotRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.RouteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = RouteController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteService routeService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider tokenProvider;

    private User testUser;
    private String validToken;
    private Depot testDepot;
    private Route testRoute;

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

        testDepot = new Depot();
        testDepot.setId("depot-1");
        testDepot.setName("Test Depot");

        testRoute = new Route();
        testRoute.setId("route-1");
        testRoute.setCode("RT-A");
        testRoute.setName("Route A");
        testRoute.setDescription("Test Route Description");
        testRoute.setDepot(testDepot);
    }

    @Test
    void createRoute_ShouldCreateNewRoute() throws Exception {
        // Given
        String depotId = "depot-1";
        String requestBody = String.format(
            "{\"code\": \"RT-B\", \"name\": \"Route B\", \"description\": \"New Route\", \"depotId\": \"%s\"}",
            depotId
        );

        Route newRoute = new Route();
        newRoute.setId("route-new");
        newRoute.setCode("RT-B");
        newRoute.setName("Route B");
        newRoute.setDescription("New Route");
        newRoute.setDepot(testDepot);

        when(routeService.createRoute(eq("RT-B"), eq("Route B"), eq("New Route"), eq(depotId), eq(testUser)))
            .thenReturn(newRoute);

        // When & Then
        mockMvc.perform(post("/api/v1/routes")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());

        verify(routeService).createRoute(eq("RT-B"), eq("Route B"), eq("New Route"), eq(depotId), eq(testUser));
    }

    @Test
    void createRoute_ShouldReturn404WhenDepotNotFound() throws Exception {
        // Given
        String depotId = "non-existent-depot";
        String requestBody = String.format(
            "{\"code\": \"RT-B\", \"name\": \"Route B\", \"depotId\": \"%s\"}",
            depotId
        );

        when(routeService.createRoute(eq("RT-B"), eq("Route B"), any(), eq(depotId), eq(testUser)))
            .thenThrow(new IllegalArgumentException("Depot not found: " + depotId));

        // When & Then
        mockMvc.perform(post("/api/v1/routes")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(routeService).createRoute(eq("RT-B"), eq("Route B"), any(), eq(depotId), eq(testUser));
    }

    @Test
    void createRoute_ShouldValidateRequiredFields() throws Exception {
        // Given - missing required fields
        String requestBody = "{\"name\": \"Route B\"}"; // missing code and depotId

        // When & Then
        mockMvc.perform(post("/api/v1/routes")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(routeService, never()).createRoute(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void createRoute_ShouldRequireAuthentication() throws Exception {
        // Given
        String requestBody = "{\"code\": \"RT-B\", \"name\": \"Route B\", \"depotId\": \"depot-1\"}";

        // When & Then
        mockMvc.perform(post("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(routeService, never()).createRoute(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void updateRoute_ShouldUpdateRouteDetails() throws Exception {
        // Given
        String routeId = "route-1";
        String requestBody = "{\"name\": \"Updated Route Name\", \"description\": \"Updated Description\"}";

        testRoute.setName("Updated Route Name");
        testRoute.setDescription("Updated Description");

        when(routeService.updateRoute(eq(routeId), eq("Updated Route Name"), eq("Updated Description"), eq(testUser)))
            .thenReturn(testRoute);

        // When & Then
        mockMvc.perform(put("/api/v1/routes/{id}", routeId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(routeService).updateRoute(eq(routeId), eq("Updated Route Name"), eq("Updated Description"), eq(testUser));
    }

    @Test
    void updateRoute_ShouldReturn404WhenRouteNotFound() throws Exception {
        // Given
        String routeId = "non-existent-route";
        String requestBody = "{\"name\": \"Updated Route Name\"}";

        when(routeService.updateRoute(eq(routeId), eq("Updated Route Name"), any(), eq(testUser)))
            .thenThrow(new IllegalArgumentException("Route not found: " + routeId));

        // When & Then
        mockMvc.perform(put("/api/v1/routes/{id}", routeId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(routeService).updateRoute(eq(routeId), eq("Updated Route Name"), any(), eq(testUser));
    }

    @Test
    void updateRoute_ShouldRequireAuthentication() throws Exception {
        // Given
        String routeId = "route-1";
        String requestBody = "{\"name\": \"Updated Route Name\"}";

        // When & Then
        mockMvc.perform(put("/api/v1/routes/{id}", routeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(routeService, never()).updateRoute(anyString(), anyString(), anyString(), any());
    }
}
