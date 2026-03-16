package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Manifest endpoints
 */
class ManifestApiIntegrationTest extends BaseIntegrationTest {

    private String getRouteId() throws Exception {
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            MvcResult result = mockMvc.perform(get("/api/v1/depots/" + depotId + "/routes")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            if (response.contains("\"id\"")) {
                int idStart = response.indexOf("\"id\":\"") + 6;
                int idEnd = response.indexOf("\"", idStart);
                return response.substring(idStart, idEnd);
            }
        }
        return null;
    }

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
    void testGetManifests_Success() throws Exception {
        System.out.println("\n=== Test: Get all manifests ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/manifests")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved manifests");
    }

    @Test
    void testGetManifests_WithFilters() throws Exception {
        System.out.println("\n=== Test: Get manifests with filters ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            MvcResult result = mockMvc.perform(get("/api/v1/manifests")
                    .param("depotId", depotId)
                    .param("date", LocalDate.now().toString())
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Successfully filtered manifests");
        }
    }
}
