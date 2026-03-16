package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Authentication endpoints
 */
class AuthApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void testLogin_Success() throws Exception {
        System.out.println("\n=== Test: Login with valid credentials ===");
        
        String loginRequest = """
            {
                "username": "admin",
                "password": "password"
            }
            """;
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.name").exists())
                .andExpect(jsonPath("$.data.user.role").value("CENTRAL_ADMIN"))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Login successful");
    }

    @Test
    void testLogin_InvalidUsername() throws Exception {
        System.out.println("\n=== Test: Login with invalid username ===");
        
        String loginRequest = """
            {
                "username": "nonexistent",
                "password": "password"
            }
            """;
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly rejected invalid username");
    }

    @Test
    void testLogin_InvalidPassword() throws Exception {
        System.out.println("\n=== Test: Login with invalid password ===");
        
        String loginRequest = """
            {
                "username": "admin",
                "password": "wrongpassword"
            }
            """;
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly rejected invalid password");
    }

    @Test
    void testLogin_MissingFields() throws Exception {
        System.out.println("\n=== Test: Login with missing fields ===");
        
        String loginRequest = """
            {
                "username": "admin"
            }
            """;
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly rejected missing password");
    }

    @Test
    void testGetCurrentUser_Success() throws Exception {
        System.out.println("\n=== Test: Get current user info ===");
        
        String token = getAdminToken();
        
        MvcResult result = mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.role").value("CENTRAL_ADMIN"))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Successfully retrieved current user");
    }

    @Test
    void testGetCurrentUser_InvalidToken() throws Exception {
        System.out.println("\n=== Test: Get current user with invalid token ===");
        
        MvcResult result = mockMvc.perform(get("/api/v1/auth/me")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().is5xxServerError())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly rejected invalid token");
    }

    @Test
    void testGetCurrentUser_MissingToken() throws Exception {
        System.out.println("\n=== Test: Get current user without token ===");
        
        MvcResult result = mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().is5xxServerError())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Correctly rejected missing token");
    }

    @Test
    void testLogin_DepotManager() throws Exception {
        System.out.println("\n=== Test: Login as depot manager ===");
        
        String loginRequest = """
            {
                "username": "depot1",
                "password": "password"
            }
            """;
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("DEPOT_MANAGER"))
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        System.out.println("Response: " + response);
        System.out.println("✓ Depot manager login successful");
    }
}
