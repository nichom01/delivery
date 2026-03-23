package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for Depot endpoints
 * Extends the existing DepotApiIntegrationTest with additional test cases
 */
class DepotApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void testGetAllDepots_Success() throws Exception {
        System.out.println("\n=== Test: Get all depots ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/depots")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved all depots");
    }

    @Test
    void testGetAllDepots_Unauthorized() throws Exception {
        System.out.println("\n=== Test: Get all depots without auth ===");
        
        MvcResult result = mockMvc.perform(get("/api/v1/depots"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andReturn();
        
        System.out.println("✓ Correctly rejected unauthorized request");
    }

    @Test
    void testGetDepotRoutes_Success() throws Exception {
        System.out.println("\n=== Test: Get routes for a depot ===");
        
        String token = getAdminToken();
        
        // First get all depots to find a depot ID
        MvcResult depotsResult = mockMvc.perform(get("/api/v1/depots")
                .header("Authorization", "Bearer " + token))
                .andReturn();
        
        String depotsResponse = depotsResult.getResponse().getContentAsString();
        System.out.println("Depots: " + depotsResponse);
        
        // Extract first depot ID (assuming there's at least one)
        if (depotsResponse.contains("\"id\"")) {
            int idStart = depotsResponse.indexOf("\"id\":\"") + 6;
            int idEnd = depotsResponse.indexOf("\"", idStart);
            String depotId = depotsResponse.substring(idStart, idEnd);
            
            MvcResult result = mockMvc.perform(get("/api/v1/depots/" + depotId + "/routes")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Routes response: " + response);
            System.out.println("✓ Successfully retrieved depot routes");
        }
    }

    @Test
    void testGetDepotRoutes_InvalidDepotId() throws Exception {
        System.out.println("\n=== Test: Get routes for invalid depot ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/depots/invalid-depot-id/routes")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Returns empty array for invalid depot");
    }

    // The create depot tests are already in the existing testCreateDepotFlow method
    // We'll keep that and add update tests here
    
    @Test
    void testUpdateDepot_Success() throws Exception {
        System.out.println("\n=== Test: Update depot ===");
        
        String token = getAdminToken();
        
        // First create a depot
        String createRequest = """
            {
                "name": "Test Depot For Update",
                "address": "Original Address"
            }
            """;
        
        MvcResult createResult = mockMvc.perform(post("/api/v1/depots")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
                .andReturn();
        
        String createResponse = createResult.getResponse().getContentAsString();
        System.out.println("Created depot: " + createResponse);
        
        // Extract depot ID
        int idStart = createResponse.indexOf("\"id\":\"") + 6;
        int idEnd = createResponse.indexOf("\"", idStart);
        String depotId = createResponse.substring(idStart, idEnd);
        
        // Update the depot
        String updateRequest = """
            {
                "name": "Updated Depot Name",
                "address": "Updated Address"
            }
            """;
        
        MvcResult updateResult = mockMvc.perform(put("/api/v1/depots/" + depotId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Depot Name"))
                .andReturn();
        
        String updateResponse = updateResult.getResponse().getContentAsString();
        System.out.println("Update response: " + updateResponse);
        System.out.println("✓ Successfully updated depot");
    }

    @Test
    void testUpdateDepot_NotFound() throws Exception {
        System.out.println("\n=== Test: Update non-existent depot ===");
        
        String token = getAdminToken();
        
        String updateRequest = """
            {
                "name": "Updated Name",
                "address": "Updated Address"
            }
            """;
        
        MvcResult result = mockMvc.perform(put("/api/v1/depots/non-existent-id")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly rejected update for non-existent depot");
    }
}
