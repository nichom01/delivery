package com.deliverysystem.integration;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DriverLocationApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void submitLocations_asDriver_succeeds() throws Exception {
        String token = getDriverToken();
        String recordedAt = Instant.now().toString();
        String body = """
            {
                "locations": [
                    { "latitude": 51.5074, "longitude": -0.1278, "recordedAt": "%s" }
                ]
            }
            """.formatted(recordedAt);

        mockMvc.perform(post("/api/v1/driver-locations")
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.savedCount").value(1));
    }

    @Test
    void submitLocations_asAdmin_forbidden() throws Exception {
        String token = getAdminToken();
        String recordedAt = Instant.now().toString();
        String body = """
            {
                "locations": [
                    { "latitude": 51.5074, "longitude": -0.1278, "recordedAt": "%s" }
                ]
            }
            """.formatted(recordedAt);

        mockMvc.perform(post("/api/v1/driver-locations")
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void submitLocations_unauthenticated_unauthorized() throws Exception {
        String recordedAt = Instant.now().toString();
        String body = """
            {
                "locations": [
                    { "latitude": 51.5074, "longitude": -0.1278, "recordedAt": "%s" }
                ]
            }
            """.formatted(recordedAt);

        mockMvc.perform(post("/api/v1/driver-locations")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void submitLocations_invalidLatitude_badRequest() throws Exception {
        String token = getDriverToken();
        String recordedAt = Instant.now().toString();
        String body = """
            {
                "locations": [
                    { "latitude": 91.0, "longitude": -0.1278, "recordedAt": "%s" }
                ]
            }
            """.formatted(recordedAt);

        mockMvc.perform(post("/api/v1/driver-locations")
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void submitLocations_recordedAtTooOld_badRequest() throws Exception {
        String token = getDriverToken();
        String recordedAt = Instant.now().minusSeconds(60L * 60 * 24 * 40).toString();
        String body = """
            {
                "locations": [
                    { "latitude": 51.5074, "longitude": -0.1278, "recordedAt": "%s" }
                ]
            }
            """.formatted(recordedAt);

        mockMvc.perform(post("/api/v1/driver-locations")
                .header("Authorization", "Bearer " + token)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
