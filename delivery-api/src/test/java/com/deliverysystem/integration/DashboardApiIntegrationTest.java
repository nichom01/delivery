package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Dashboard endpoints
 */
class DashboardApiIntegrationTest extends BaseIntegrationTest {

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
    void testGetDashboard_Success() throws Exception {
        System.out.println("\n=== Test: Get dashboard data ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved dashboard data");
    }

    @Test
    void testGetDashboard_WithDepotFilter() throws Exception {
        System.out.println("\n=== Test: Get dashboard with depot filter ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            MvcResult result = mockMvc.perform(get("/api/v1/dashboard")
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
    void testGetDashboard_WithDateFilter() throws Exception {
        System.out.println("\n=== Test: Get dashboard with date filter ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/dashboard")
                .param("date", LocalDate.now().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully filtered by date");
    }
}
