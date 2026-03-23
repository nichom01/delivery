package com.deliverysystem.integration;

import com.deliverysystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DriverLocationApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

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

    @Test
    void listSamples_asDepotManager_afterDriverPosts_returnsPoints() throws Exception {
        String driverToken = getDriverToken();
        String managerToken = getDepotManagerToken();
        String driverUserId = userRepository.findByUsername("driver1").orElseThrow().getId();
        String recordedAt = Instant.now().toString();
        String postBody = """
            {
                "locations": [
                    { "latitude": 12.3456, "longitude": -0.1278, "recordedAt": "%s" }
                ]
            }
            """.formatted(recordedAt);

        mockMvc.perform(post("/api/v1/driver-locations")
                .header("Authorization", "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(postBody))
            .andExpect(status().isOk());

        String today = LocalDate.now(ZoneOffset.UTC).toString();
        mockMvc.perform(get("/api/v1/driver-locations")
                .param("userId", driverUserId)
                .param("date", today)
                .header("Authorization", "Bearer " + managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].latitude", hasItem(12.3456)));
    }

    @Test
    void listSamples_asDriver_forSelf_ok() throws Exception {
        String driverToken = getDriverToken();
        String driverUserId = userRepository.findByUsername("driver1").orElseThrow().getId();
        String recordedAt = Instant.now().toString();
        String postBody = """
            {
                "locations": [
                    { "latitude": 53.9876, "longitude": -1.0, "recordedAt": "%s" }
                ]
            }
            """.formatted(recordedAt);

        mockMvc.perform(post("/api/v1/driver-locations")
                .header("Authorization", "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(postBody))
            .andExpect(status().isOk());

        String today = LocalDate.now(ZoneOffset.UTC).toString();
        mockMvc.perform(get("/api/v1/driver-locations")
                .param("userId", driverUserId)
                .param("date", today)
                .header("Authorization", "Bearer " + driverToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].latitude", hasItem(53.9876)));
    }

    @Test
    void listSamples_otherDepotManager_forbidden() throws Exception {
        String loginRequest = """
            {
                "username": "depot2",
                "password": "password"
            }
            """;
        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
            .andReturn();
        String depot2Token = extractToken(loginResult.getResponse().getContentAsString());
        String driverUserId = userRepository.findByUsername("driver1").orElseThrow().getId();
        String today = LocalDate.now(ZoneOffset.UTC).toString();

        mockMvc.perform(get("/api/v1/driver-locations")
                .param("userId", driverUserId)
                .param("date", today)
                .header("Authorization", "Bearer " + depot2Token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void listSamples_unauthenticated_unauthorized() throws Exception {
        String driverUserId = userRepository.findByUsername("driver1").orElseThrow().getId();
        mockMvc.perform(get("/api/v1/driver-locations").param("userId", driverUserId))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }
}
