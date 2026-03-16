package com.deliverysystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for integration tests with common utilities
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Extract JWT token from login response JSON
     */
    protected String extractToken(String jsonResponse) {
        try {
            int tokenStart = jsonResponse.indexOf("\"token\":\"") + 9;
            if (tokenStart < 9) return null;
            int tokenEnd = jsonResponse.indexOf("\"", tokenStart);
            if (tokenEnd < tokenStart) return null;
            return jsonResponse.substring(tokenStart, tokenEnd);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get authentication token for admin user
     */
    protected String getAdminToken() throws Exception {
        String loginRequest = """
            {
                "username": "admin",
                "password": "password"
            }
            """;
        
        var result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(loginRequest)
        ).andReturn();
        
        String response = result.getResponse().getContentAsString();
        String token = extractToken(response);
        
        if (token == null) {
            throw new RuntimeException("Failed to get admin token");
        }
        
        return token;
    }

    /**
     * Get authentication token for depot manager user
     */
    protected String getDepotManagerToken() throws Exception {
        String loginRequest = """
            {
                "username": "depot1",
                "password": "password"
            }
            """;
        
        var result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(loginRequest)
        ).andReturn();
        
        String response = result.getResponse().getContentAsString();
        String token = extractToken(response);
        
        if (token == null) {
            throw new RuntimeException("Failed to get depot manager token");
        }
        
        return token;
    }
}
