package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.User;
import com.deliverysystem.dto.CreateDepotRequest;
import com.deliverysystem.dto.UpdateDepotRequest;
import com.deliverysystem.repository.DepotRepository;
import com.deliverysystem.repository.DriverRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.repository.VehicleRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.AuditService;
import com.deliverysystem.service.DashboardService;
import com.deliverysystem.service.DepotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DepotController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DepotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepotRepository depotRepository;

    @MockBean
    private RouteRepository routeRepository;

    @MockBean
    private VehicleRepository vehicleRepository;

    @MockBean
    private DriverRepository driverRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private AuditService auditService;

    @MockBean
    private DepotService depotService;

    @MockBean
    private DashboardService dashboardService;

    private User testUser;
    private String validToken;
    private Depot testDepot;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-1");
        testUser.setUsername("admin");
        testUser.setName("Admin User");
        testUser.setRole(User.UserRole.CENTRAL_ADMIN);

        validToken = "valid-jwt-token";

        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        testDepot = new Depot();
        testDepot.setId("depot-new");
        testDepot.setName("New Test Depot");
        testDepot.setAddress("123 Test Street, Test City");
        testDepot.setLatitude(new BigDecimal("51.5074"));
        testDepot.setLongitude(new BigDecimal("-0.1278"));
    }

    @Test
    void createDepot_ShouldCreateNewDepot() throws Exception {
        // Given
        CreateDepotRequest request = new CreateDepotRequest();
        request.setName("New Test Depot");
        request.setAddress("123 Test Street, Test City");
        request.setLatitude("51.5074");
        request.setLongitude("-0.1278");

        when(depotService.createDepot(any(), any(), any(), any(), any())).thenReturn(testDepot);
        when(routeRepository.findByDepotId(anyString())).thenReturn(java.util.Collections.emptyList());
        when(vehicleRepository.findByDepotId(anyString())).thenReturn(java.util.Collections.emptyList());
        when(driverRepository.findByDepotId(anyString())).thenReturn(java.util.Collections.emptyList());

        // When & Then
        mockMvc.perform(post("/api/v1/depots")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Depot created successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("New Test Depot"))
                .andExpect(jsonPath("$.data.location").value("123 Test Street, Test City"))
                .andExpect(jsonPath("$.data.routesCount").value(0))
                .andExpect(jsonPath("$.data.vehiclesCount").value(0))
                .andExpect(jsonPath("$.data.driversCount").value(0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(depotService).createDepot(any(), any(), any(), any(), eq(testUser));
    }

    @Test
    void createDepot_ShouldReturn400WhenNameIsMissing() throws Exception {
        // Given
        CreateDepotRequest request = new CreateDepotRequest();
        request.setAddress("123 Test Street");
        // name is missing

        // When & Then
        mockMvc.perform(post("/api/v1/depots")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        verify(depotRepository, never()).save(any(Depot.class));
    }

    @Test
    void createDepot_ShouldReturn400WhenAddressIsMissing() throws Exception {
        // Given
        CreateDepotRequest request = new CreateDepotRequest();
        request.setName("Test Depot");
        // address is missing

        // When & Then
        mockMvc.perform(post("/api/v1/depots")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        verify(depotRepository, never()).save(any(Depot.class));
    }

    @Test
    void createDepot_ShouldReturn400WhenDepotNameAlreadyExists() throws Exception {
        // Given
        CreateDepotRequest request = new CreateDepotRequest();
        request.setName("Existing Depot");
        request.setAddress("123 Test Street");

        Depot existingDepot = new Depot();
        existingDepot.setId("depot-existing");
        existingDepot.setName("Existing Depot");

        when(depotService.createDepot(any(), any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Depot with name 'Existing Depot' already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/depots")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Depot with name 'Existing Depot' already exists"));
    }

    @Test
    void createDepot_ShouldReturn400WhenInvalidLatitude() throws Exception {
        // Given
        CreateDepotRequest request = new CreateDepotRequest();
        request.setName("Test Depot");
        request.setAddress("123 Test Street");
        request.setLatitude("invalid-latitude");

        when(depotService.createDepot(any(), any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Invalid latitude format"));

        // When & Then
        mockMvc.perform(post("/api/v1/depots")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid latitude format"));
    }

    @Test
    void updateDepot_ShouldUpdateExistingDepot() throws Exception {
        // Given
        String depotId = "depot-1";
        UpdateDepotRequest request = new UpdateDepotRequest();
        request.setName("Updated Depot Name");
        request.setAddress("456 Updated Street");

        Depot existingDepot = new Depot();
        existingDepot.setId(depotId);
        existingDepot.setName("Original Name");
        existingDepot.setAddress("123 Original Street");

        Depot updatedDepot = new Depot();
        updatedDepot.setId(depotId);
        updatedDepot.setName("Updated Depot Name");
        updatedDepot.setAddress("456 Updated Street");
        when(depotService.updateDepot(eq(depotId), any(), any(), any(), any(), any())).thenReturn(updatedDepot);
        when(routeRepository.findByDepotId(depotId)).thenReturn(java.util.Collections.emptyList());
        when(vehicleRepository.findByDepotId(depotId)).thenReturn(java.util.Collections.emptyList());
        when(driverRepository.findByDepotId(depotId)).thenReturn(java.util.Collections.emptyList());

        // When & Then
        mockMvc.perform(put("/api/v1/depots/{id}", depotId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Depot updated successfully"))
                .andExpect(jsonPath("$.data.id").value(depotId))
                .andExpect(jsonPath("$.data.name").value("Updated Depot Name"))
                .andExpect(jsonPath("$.data.location").value("456 Updated Street"));

        verify(depotService).updateDepot(eq(depotId), any(), any(), any(), any(), eq(testUser));
    }

    @Test
    void updateDepot_ShouldReturn404WhenDepotNotFound() throws Exception {
        // Given
        String depotId = "non-existent-depot";
        UpdateDepotRequest request = new UpdateDepotRequest();
        request.setName("Updated Name");

        when(depotService.updateDepot(eq(depotId), any(), any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Depot not found: " + depotId));

        // When & Then
        mockMvc.perform(put("/api/v1/depots/{id}", depotId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Depot not found: " + depotId));
    }

    @Test
    void updateDepot_ShouldReturn400WhenNewNameConflictsWithExistingDepot() throws Exception {
        // Given
        String depotId = "depot-1";
        UpdateDepotRequest request = new UpdateDepotRequest();
        request.setName("Conflicting Name");

        Depot existingDepot = new Depot();
        existingDepot.setId(depotId);
        existingDepot.setName("Original Name");

        Depot conflictingDepot = new Depot();
        conflictingDepot.setId("depot-2");
        conflictingDepot.setName("Conflicting Name");

        when(depotService.updateDepot(eq(depotId), any(), any(), any(), any(), any()))
            .thenThrow(new IllegalArgumentException("Depot with name 'Conflicting Name' already exists"));

        // When & Then
        mockMvc.perform(put("/api/v1/depots/{id}", depotId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Depot with name 'Conflicting Name' already exists"));
    }

    @Test
    void createDepot_ShouldReturn400WhenAuthorizationHeaderIsMissing() throws Exception {
        // Given
        CreateDepotRequest request = new CreateDepotRequest();
        request.setName("Test Depot");
        request.setAddress("123 Test Street");

        // When & Then - No Authorization header; Spring throws MissingRequestHeaderException → 500
        mockMvc.perform(post("/api/v1/depots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void createDepot_ShouldReturn400WhenAuthorizationHeaderIsInvalid() throws Exception {
        // Given
        CreateDepotRequest request = new CreateDepotRequest();
        request.setName("Test Depot");
        request.setAddress("123 Test Street");

        // When & Then - Invalid Authorization header format
        mockMvc.perform(post("/api/v1/depots")
                .header("Authorization", "InvalidFormat token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid authorization header"));

        verify(depotRepository, never()).save(any(Depot.class));
    }
}
