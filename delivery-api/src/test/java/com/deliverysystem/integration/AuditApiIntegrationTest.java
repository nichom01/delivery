package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Audit endpoints
 */
class AuditApiIntegrationTest extends BaseIntegrationTest {

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
    void testGetAuditEvents_Success() throws Exception {
        System.out.println("\n=== Test: Get all audit events ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/audit")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved audit events");
    }

    @Test
    void testGetAuditEvents_WithDepotFilter() throws Exception {
        System.out.println("\n=== Test: Get audit events filtered by depot ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            MvcResult result = mockMvc.perform(get("/api/v1/audit")
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
}
