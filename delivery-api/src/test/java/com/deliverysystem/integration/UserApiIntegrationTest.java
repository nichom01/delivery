package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User endpoints
 */
class UserApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void testGetAllUsers_Success() throws Exception {
        System.out.println("\n=== Test: Get all users ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved all users");
    }

    @Test
    void testGetAllUsers_Unauthorized() throws Exception {
        System.out.println("\n=== Test: Get all users without auth ===");
        
        MvcResult result = mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andReturn();
        
        System.out.println("✓ Correctly rejected unauthorized request");
    }
}
