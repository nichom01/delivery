package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Driver endpoints
 */
class DriverApiIntegrationTest extends BaseIntegrationTest {

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
    void testGetDrivers_Success() throws Exception {
        System.out.println("\n=== Test: Get all drivers ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/drivers")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved drivers");
    }

    @Test
    void testGetDrivers_WithDepotFilter() throws Exception {
        System.out.println("\n=== Test: Get drivers filtered by depot ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            MvcResult result = mockMvc.perform(get("/api/v1/drivers")
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
    void testCreateDriver_Success() throws Exception {
        System.out.println("\n=== Test: Create driver ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            String createRequest = """
                {
                    "name": "Test Driver",
                    "contact": "test@example.com",
                    "licenceNumber": "DRIV123456",
                    "depotId": "%s"
                }
                """.formatted(depotId);
            
            MvcResult result = mockMvc.perform(post("/api/v1/drivers")
                    .header("Authorization", "Bearer " + token)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Test Driver"))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Successfully created driver");
        }
    }

    @Test
    void testCreateDriver_MissingName() throws Exception {
        System.out.println("\n=== Test: Create driver with missing name ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            String createRequest = """
                {
                    "contact": "test@example.com",
                    "depotId": "%s"
                }
                """.formatted(depotId);
            
            MvcResult result = mockMvc.perform(post("/api/v1/drivers")
                    .header("Authorization", "Bearer " + token)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isBadRequest())
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Correctly rejected missing name");
        }
    }
}
