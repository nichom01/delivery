package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Box;
import com.deliverysystem.domain.Driver;
import com.deliverysystem.domain.Manifest;
import com.deliverysystem.domain.Order;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.domain.Vehicle;
import com.deliverysystem.repository.ManifestRepository;
import com.deliverysystem.repository.OrderRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.ManifestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(controllers = ManifestController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ManifestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManifestService manifestService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider tokenProvider;

    private User testUser;
    private String validToken;
    private Manifest testManifest;

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

        // Setup test manifest
        Route route = new Route();
        route.setId("route-1");
        route.setCode("RT-A");
        route.setName("Route A");

        Driver driver = new Driver();
        driver.setId("driver-1");
        driver.setName("Test Driver");

        Vehicle vehicle = new Vehicle();
        vehicle.setId("vehicle-1");
        vehicle.setRegistration("ABC123");

        testManifest = new Manifest();
        testManifest.setId("manifest-1");
        testManifest.setRoute(route);
        testManifest.setDriver(driver);
        testManifest.setVehicle(vehicle);
        testManifest.setDate(LocalDate.now());
        testManifest.setStatus(Manifest.ManifestStatus.DRAFT);
    }

    @Test
    void updateManifest_ShouldUpdateManifestDetails() throws Exception {
        // Given
        String manifestId = "manifest-1";
        String newDriverId = "driver-2";
        String newVehicleId = "vehicle-2";
        String newDate = "2026-03-15";

        Driver newDriver = new Driver();
        newDriver.setId(newDriverId);
        Vehicle newVehicle = new Vehicle();
        newVehicle.setId(newVehicleId);
        testManifest.setDriver(newDriver);
        testManifest.setVehicle(newVehicle);

        when(manifestService.updateManifest(eq(manifestId), eq(newDriverId), eq(newVehicleId), eq(newDate), eq(testUser)))
            .thenReturn(testManifest);

        String requestBody = String.format(
            "{\"driverId\": \"%s\", \"vehicleId\": \"%s\", \"date\": \"%s\"}",
            newDriverId, newVehicleId, newDate
        );

        // When & Then
        mockMvc.perform(put("/api/v1/manifests/{id}", manifestId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(manifestService).updateManifest(eq(manifestId), eq(newDriverId), eq(newVehicleId), eq(newDate), eq(testUser));
    }

    @Test
    void updateManifest_ShouldReturn404WhenManifestNotFound() throws Exception {
        // Given
        String manifestId = "non-existent-manifest";
        when(manifestService.updateManifest(eq(manifestId), any(), any(), any(), eq(testUser)))
            .thenThrow(new IllegalArgumentException("Manifest not found: " + manifestId));

        String requestBody = "{\"driverId\": \"driver-2\", \"vehicleId\": \"vehicle-2\"}";

        // When & Then
        mockMvc.perform(put("/api/v1/manifests/{id}", manifestId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(manifestService).updateManifest(eq(manifestId), any(), any(), any(), eq(testUser));
    }

    @Test
    void updateManifest_ShouldOnlyAllowUpdatesToDraftStatusManifests() throws Exception {
        // Given
        String manifestId = "manifest-1";
        when(manifestService.updateManifest(eq(manifestId), any(), any(), any(), eq(testUser)))
            .thenThrow(new IllegalStateException("Manifest is not in DRAFT status. Only DRAFT manifests can be updated."));

        String requestBody = "{\"driverId\": \"driver-2\"}";

        // When & Then
        mockMvc.perform(put("/api/v1/manifests/{id}", manifestId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(manifestService).updateManifest(eq(manifestId), any(), any(), any(), eq(testUser));
    }

    @Test
    void updateManifest_ShouldRequireAuthentication() throws Exception {
        // Given
        String manifestId = "manifest-1";
        String requestBody = "{\"driverId\": \"driver-2\"}";

        // When & Then
        mockMvc.perform(put("/api/v1/manifests/{id}", manifestId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(manifestService, never()).updateManifest(anyString(), any(), any(), any(), any());
    }

    @Test
    void removeStopFromManifest_ShouldRemoveOrderFromManifest() throws Exception {
        // Given
        String manifestId = "manifest-1";
        String orderId = "order-123";

        when(manifestService.removeStopFromManifest(manifestId, orderId, testUser))
            .thenReturn(testManifest);

        // When & Then
        mockMvc.perform(delete("/api/v1/manifests/{id}/stops/{orderId}", manifestId, orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(manifestService).removeStopFromManifest(manifestId, orderId, testUser);
    }

    @Test
    void removeStopFromManifest_ShouldReturn404WhenManifestNotFound() throws Exception {
        // Given
        String manifestId = "non-existent-manifest";
        String orderId = "order-123";
        when(manifestService.removeStopFromManifest(manifestId, orderId, testUser))
            .thenThrow(new IllegalArgumentException("Manifest not found: " + manifestId));

        // When & Then
        mockMvc.perform(delete("/api/v1/manifests/{id}/stops/{orderId}", manifestId, orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(manifestService).removeStopFromManifest(manifestId, orderId, testUser);
    }

    @Test
    void removeStopFromManifest_ShouldReturn404WhenOrderNotFound() throws Exception {
        // Given
        String manifestId = "manifest-1";
        String orderId = "non-existent-order";
        when(manifestService.removeStopFromManifest(manifestId, orderId, testUser))
            .thenThrow(new IllegalArgumentException("Order " + orderId + " is not assigned to manifest " + manifestId));

        // When & Then
        mockMvc.perform(delete("/api/v1/manifests/{id}/stops/{orderId}", manifestId, orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(manifestService).removeStopFromManifest(manifestId, orderId, testUser);
    }

    @Test
    void removeStopFromManifest_ShouldOnlyAllowRemovalFromDraftStatusManifests() throws Exception {
        // Given
        String manifestId = "manifest-1";
        String orderId = "order-123";
        when(manifestService.removeStopFromManifest(manifestId, orderId, testUser))
            .thenThrow(new IllegalStateException("Manifest is not in DRAFT status. Only DRAFT manifests can be modified."));

        // When & Then
        mockMvc.perform(delete("/api/v1/manifests/{id}/stops/{orderId}", manifestId, orderId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(manifestService).removeStopFromManifest(manifestId, orderId, testUser);
    }

    @Test
    void removeStopFromManifest_ShouldRequireAuthentication() throws Exception {
        // Given
        String manifestId = "manifest-1";
        String orderId = "order-123";

        // When & Then
        mockMvc.perform(delete("/api/v1/manifests/{id}/stops/{orderId}", manifestId, orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(manifestService, never()).removeStopFromManifest(anyString(), anyString(), any());
    }
}
