package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Vehicle endpoints
 */
class VehicleApiIntegrationTest extends BaseIntegrationTest {

    private String getDepotId() throws Exception {
        String token = getAdminToken();
        MvcResult result = mockMvc.perform(get("/api/v1/depots")
                .header("Authorization", "Bearer " + token))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        if (response.contains("\"id\"")) {
            int idStart = response.indexOf("\"id\":\"") + 6;
            int idEnd = response.indexOf("\"", idStart);
            return response.substring(idStart, idEnd);
        }
        return null;
    }

    @Test
    void testGetVehicles_Success() throws Exception {
        System.out.println("\n=== Test: Get all vehicles ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/vehicles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved vehicles");
    }

    @Test
    void testGetVehicles_WithDepotFilter() throws Exception {
        System.out.println("\n=== Test: Get vehicles filtered by depot ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            MvcResult result = mockMvc.perform(get("/api/v1/vehicles")
                    .param("depotId", depotId)
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Successfully filtered by depot");
        }
    }

    @Test
    void testCreateVehicle_Success() throws Exception {
        System.out.println("\n=== Test: Create vehicle ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            String createRequest = """
                {
                    "registration": "TEST-001",
                    "make": "Ford",
                    "model": "Transit",
                    "capacity": "3.5t / 12m³",
                    "depotId": "%s"
                }
                """.formatted(depotId);
            
            MvcResult result = mockMvc.perform(post("/api/v1/vehicles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.registration").value("TEST-001"))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Successfully created vehicle");
        }
    }

    @Test
    void testCreateVehicle_MissingRegistration() throws Exception {
        System.out.println("\n=== Test: Create vehicle with missing registration ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            String createRequest = """
                {
                    "make": "Ford",
                    "depotId": "%s"
                }
                """.formatted(depotId);
            
            MvcResult result = mockMvc.perform(post("/api/v1/vehicles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isBadRequest())
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Correctly rejected missing registration");
        }
    }

    @Test
    void testCreateVehicle_DuplicateRegistration() throws Exception {
        System.out.println("\n=== Test: Create vehicle with duplicate registration ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            // First create a vehicle
            String createRequest1 = """
                {
                    "registration": "DUPLICATE-001",
                    "make": "Ford",
                    "depotId": "%s"
                }
                """.formatted(depotId);
            
            mockMvc.perform(post("/api/v1/vehicles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(createRequest1))
                    .andReturn();
            
            // Try to create another with same registration
            MvcResult result = mockMvc.perform(post("/api/v1/vehicles")
                    .header("Authorization", "Bearer " + token)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(createRequest1))
                    .andExpect(status().isBadRequest())
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Correctly rejected duplicate registration");
        }
    }
}
