package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Postcode Rule endpoints
 */
class PostcodeRuleApiIntegrationTest extends BaseIntegrationTest {

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
    void testGetPostcodeRules_Success() throws Exception {
        System.out.println("\n=== Test: Get all postcode rules ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/postcode-rules")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved postcode rules");
    }

    @Test
    void testGetPostcodeRules_WithDepotFilter() throws Exception {
        System.out.println("\n=== Test: Get postcode rules filtered by depot ===");
        
        String token = getAdminToken();
        String depotId = getDepotId();
        
        if (depotId != null) {
            MvcResult result = mockMvc.perform(get("/api/v1/postcode-rules")
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
    void testCreatePostcodeRule_Success() throws Exception {
        System.out.println("\n=== Test: Create postcode rule ===");
        
        String token = getAdminToken();
        String routeId = getRouteId();
        
        if (routeId != null) {
            String createRequest = """
                {
                    "pattern": "SW1",
                    "level": "area",
                    "routeId": "%s",
                    "effectiveFrom": "%s"
                }
                """.formatted(routeId, LocalDate.now().toString());
            
            MvcResult result = mockMvc.perform(post("/api/v1/postcode-rules")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.pattern").value("SW1"))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            System.out.println("Response: " + response);
            System.out.println("✓ Successfully created postcode rule");
        }
    }

    @Test
    void testLookupPostcode_Success() throws Exception {
        System.out.println("\n=== Test: Lookup postcode ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/postcode-rules/lookup")
                .param("postcode", "SW1A 1AA")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hierarchy").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully looked up postcode");
    }

    @Test
    void testUpdatePostcodeRule_Success() throws Exception {
        System.out.println("\n=== Test: Update postcode rule ===");
        
        String token = getAdminToken();
        String routeId = getRouteId();
        
        if (routeId != null) {
            // First create a rule
            String createRequest = """
                {
                    "pattern": "SW2",
                    "level": "area",
                    "routeId": "%s",
                    "effectiveFrom": "%s"
                }
                """.formatted(routeId, LocalDate.now().toString());
            
            MvcResult createResult = mockMvc.perform(post("/api/v1/postcode-rules")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andReturn();
            
            String createResponse = createResult.getResponse().getContentAsString();
            int idStart = createResponse.indexOf("\"id\":\"") + 6;
            int idEnd = createResponse.indexOf("\"", idStart);
            String ruleId = createResponse.substring(idStart, idEnd);
            
            // Update the rule
            String updateRequest = """
                {
                    "pattern": "SW2A",
                    "level": "district"
                }
                """;
            
            MvcResult updateResult = mockMvc.perform(put("/api/v1/postcode-rules/" + ruleId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();
            
            String updateResponse = updateResult.getResponse().getContentAsString();
            System.out.println("Update response: " + updateResponse);
            System.out.println("✓ Successfully updated postcode rule");
        }
    }
}
