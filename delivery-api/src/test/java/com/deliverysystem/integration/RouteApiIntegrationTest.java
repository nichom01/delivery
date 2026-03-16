package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Route endpoints
 */
class RouteApiIntegrationTest extends BaseIntegrationTest {

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
    void testGetRoute_Success() throws Exception {
        System.out.println("\n=== Test: Get route by ID ===");
        
        String token = getAdminToken();
        
        // First get routes from a depot
        String depotId = getDepotId();
        if (depotId != null) {
            MvcResult routesResult = mockMvc.perform(get("/api/v1/depots/" + depotId + "/routes")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();
            
            String routesResponse = routesResult.getResponse().getContentAsString();
            System.out.println("Routes: " + routesResponse);
            
            if (routesResponse.contains("\"id\"")) {
                int idStart = routesResponse.indexOf("\"id\":\"") + 6;
                int idEnd = routesResponse.indexOf("\"", idStart);
                String routeId = routesResponse.substring(idStart, idEnd);
                
                MvcResult result = mockMvc.perform(get("/api/v1/routes/" + routeId)
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.id").exists())
                        .andReturn();
                
                String response = result.getResponse().getContentAsString();
                System.out.println("Route details: " + response);
                System.out.println("✓ Successfully retrieved route");
            }
        }
    }

    @Test
    void testGetRoute_NotFound() throws Exception {
        System.out.println("\n=== Test: Get non-existent route ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/routes/non-existent-id")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly handled non-existent route");
    }

    @Test
    void testGetRouteDrilldown_Success() throws Exception {
        System.out.println("\n=== Test: Get route drilldown ===");
        
        String token = getAdminToken();
        
        String depotId = getDepotId();
        if (depotId != null) {
            MvcResult routesResult = mockMvc.perform(get("/api/v1/depots/" + depotId + "/routes")
                    .header("Authorization", "Bearer " + token))
                    .andReturn();
            
            String routesResponse = routesResult.getResponse().getContentAsString();
            if (routesResponse.contains("\"id\"")) {
                int idStart = routesResponse.indexOf("\"id\":\"") + 6;
                int idEnd = routesResponse.indexOf("\"", idStart);
                String routeId = routesResponse.substring(idStart, idEnd);
                
                MvcResult result = mockMvc.perform(get("/api/v1/routes/" + routeId + "/drilldown")
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andReturn();
                
                String response = result.getResponse().getContentAsString();
                System.out.println("Drilldown: " + response);
                System.out.println("✓ Successfully retrieved route drilldown");
            }
        }
    }

    @Test
    void testCreateRoute_Success() throws Exception {
        System.out.println("\n=== Test: Create route ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            String createRequest = """
                {
                    "code": "RT-TEST-001",
                    "name": "Test Route",
                    "description": "Test route description",
                    "depotId": "%s"
                }
                """.formatted(depotId);
            
            MvcResult result = mockMvc.perform(post("/api/v1/routes")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("RT-TEST-001"))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Successfully created route");
        }
    }

    @Test
    void testCreateRoute_MissingFields() throws Exception {
        System.out.println("\n=== Test: Create route with missing fields ===");
        
        String token = getAdminToken();
        
        String createRequest = """
            {
                "name": "Test Route"
            }
            """;
        
        MvcResult result = mockMvc.perform(post("/api/v1/routes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly rejected missing required fields");
    }

    @Test
    void testUpdateRoute_Success() throws Exception {
        System.out.println("\n=== Test: Update route ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            // First create a route
            String createRequest = """
                {
                    "code": "RT-UPDATE-001",
                    "name": "Route To Update",
                    "description": "Original description",
                    "depotId": "%s"
                }
                """.formatted(depotId);
            
            MvcResult createResult = mockMvc.perform(post("/api/v1/routes")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andReturn();
            
            String createResponse = createResult.getResponse().getContentAsString();
            int idStart = createResponse.indexOf("\"id\":\"") + 6;
            int idEnd = createResponse.indexOf("\"", idStart);
            String routeId = createResponse.substring(idStart, idEnd);
            
            // Update the route
            String updateRequest = """
                {
                    "name": "Updated Route Name",
                    "description": "Updated description"
                }
                """;
            
            MvcResult updateResult = mockMvc.perform(put("/api/v1/routes/" + routeId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("Updated Route Name"))
                    .andReturn();
            
            String updateResponse = updateResult.getResponse().getContentAsString();
            System.out.println("Update response: " + updateResponse);
            System.out.println("✓ Successfully updated route");
        }
    }
}
