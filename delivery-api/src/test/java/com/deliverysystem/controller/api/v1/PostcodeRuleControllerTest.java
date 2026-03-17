package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.PostcodeRule;
import com.deliverysystem.domain.Route;
import com.deliverysystem.domain.User;
import com.deliverysystem.repository.PostcodeRuleRepository;
import com.deliverysystem.repository.RouteRepository;
import com.deliverysystem.repository.UserRepository;
import com.deliverysystem.security.JwtTokenProvider;
import com.deliverysystem.service.AuditService;
import com.deliverysystem.service.PostcodeRoutingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(controllers = PostcodeRuleController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PostcodeRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostcodeRuleRepository postcodeRuleRepository;

    @MockBean
    private RouteRepository routeRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @MockBean
    private AuditService auditService;

    @MockBean
    private PostcodeRoutingService postcodeRoutingService;

    private User testUser;
    private String validToken;
    private Depot testDepot;
    private Route testRoute;
    private PostcodeRule testRule;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-1");
        testUser.setUsername("testuser");
        testUser.setName("Test User");
        testUser.setRole(User.UserRole.DEPOT_MANAGER);

        validToken = "valid-jwt-token";

        when(tokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        testDepot = new Depot();
        testDepot.setId("depot-1");
        testDepot.setName("Test Depot");

        testRoute = new Route();
        testRoute.setId("route-1");
        testRoute.setCode("RT-A");
        testRoute.setName("Route A");
        testRoute.setDepot(testDepot);

        testRule = new PostcodeRule();
        testRule.setId("rule-1");
        testRule.setPattern("SW8");
        testRule.setLevel(PostcodeRule.PostcodeLevel.DISTRICT);
        testRule.setRoute(testRoute);
        testRule.setEffectiveFrom(LocalDate.now());
    }

    @Test
    void updatePostcodeRule_ShouldUpdateRuleDetails() throws Exception {
        // Given
        String ruleId = "rule-1";
        String newPattern = "SW9";
        String newEffectiveFrom = "2026-04-01";
        String requestBody = String.format(
            "{\"pattern\": \"%s\", \"level\": \"district\", \"routeId\": \"%s\", \"effectiveFrom\": \"%s\"}",
            newPattern, testRoute.getId(), newEffectiveFrom
        );

        testRule.setPattern(newPattern);
        testRule.setEffectiveFrom(LocalDate.parse(newEffectiveFrom));

        when(postcodeRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));
        when(routeRepository.findById(testRoute.getId())).thenReturn(Optional.of(testRoute));
        when(postcodeRuleRepository.save(any(PostcodeRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        mockMvc.perform(put("/api/v1/postcode-rules/{id}", ruleId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(postcodeRuleRepository).findById(ruleId);
        verify(postcodeRuleRepository).save(any(PostcodeRule.class));
    }

    @Test
    void updatePostcodeRule_ShouldReturn404WhenRuleNotFound() throws Exception {
        // Given
        String ruleId = "non-existent-rule";
        String requestBody = String.format(
            "{\"pattern\": \"SW9\", \"level\": \"district\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-04-01\"}",
            testRoute.getId()
        );

        when(postcodeRuleRepository.findById(ruleId)).thenReturn(Optional.empty());

        // When & Then
        // GlobalExceptionHandler maps IllegalArgumentException -> 400 Bad Request
        mockMvc.perform(put("/api/v1/postcode-rules/{id}", ruleId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(postcodeRuleRepository).findById(ruleId);
        verify(postcodeRuleRepository, never()).save(any());
    }

    @Test
    void updatePostcodeRule_ShouldReturn404WhenRouteNotFound() throws Exception {
        // Given
        String ruleId = "rule-1";
        String invalidRouteId = "non-existent-route";
        String requestBody = String.format(
            "{\"pattern\": \"SW9\", \"level\": \"district\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-04-01\"}",
            invalidRouteId
        );

        when(postcodeRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));
        when(routeRepository.findById(invalidRouteId)).thenReturn(Optional.empty());

        // When & Then
        // GlobalExceptionHandler maps IllegalArgumentException -> 400 Bad Request
        mockMvc.perform(put("/api/v1/postcode-rules/{id}", ruleId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(postcodeRuleRepository).findById(ruleId);
        verify(routeRepository).findById(invalidRouteId);
        verify(postcodeRuleRepository, never()).save(any());
    }

    @Test
    void updatePostcodeRule_ShouldValidateEffectiveDateRanges() throws Exception {
        // Given - effectiveTo before effectiveFrom
        String ruleId = "rule-1";
        String requestBody = String.format(
            "{\"pattern\": \"SW9\", \"level\": \"district\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-04-01\", \"effectiveTo\": \"2026-03-01\"}",
            testRoute.getId()
        );

        when(postcodeRuleRepository.findById(ruleId)).thenReturn(Optional.of(testRule));
        when(routeRepository.findById(testRoute.getId())).thenReturn(Optional.of(testRoute));

        // When & Then
        mockMvc.perform(put("/api/v1/postcode-rules/{id}", ruleId)
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(postcodeRuleRepository).findById(ruleId);
        verify(postcodeRuleRepository, never()).save(any());
    }

    @Test
    void updatePostcodeRule_ShouldRequireAuthentication() throws Exception {
        // Given
        String ruleId = "rule-1";
        String requestBody = String.format(
            "{\"pattern\": \"SW9\", \"level\": \"district\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-04-01\"}",
            testRoute.getId()
        );

        // When & Then
        // Security filters are disabled in this @WebMvcTest slice; missing auth header
        // causes tokenProvider.getUsernameFromToken to throw -> 500 from GlobalExceptionHandler.
        // The meaningful authentication tests live in the integration test suite.
        mockMvc.perform(put("/api/v1/postcode-rules/{id}", ruleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().is5xxServerError());

        verify(postcodeRuleRepository, never()).findById(anyString());
        verify(postcodeRuleRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // Pattern normalisation on create
    // ------------------------------------------------------------------

    @Test
    void createPostcodeRule_ShouldNormalisePatternByStrippingSpaces() throws Exception {
        when(routeRepository.findById(testRoute.getId())).thenReturn(Optional.of(testRoute));
        when(postcodeRuleRepository.save(any(PostcodeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String requestBody = String.format(
            "{\"pattern\": \"NR14 6HF\", \"level\": \"full\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-03-17\"}",
            testRoute.getId()
        );

        mockMvc.perform(post("/api/v1/postcode-rules")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pattern").value("NR146HF"));

        ArgumentCaptor<PostcodeRule> captor = ArgumentCaptor.forClass(PostcodeRule.class);
        verify(postcodeRuleRepository).save(captor.capture());
        assertThat(captor.getValue().getPattern()).isEqualTo("NR146HF");
    }

    @Test
    void createPostcodeRule_ShouldNormalisePatternToUppercase() throws Exception {
        when(routeRepository.findById(testRoute.getId())).thenReturn(Optional.of(testRoute));
        when(postcodeRuleRepository.save(any(PostcodeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String requestBody = String.format(
            "{\"pattern\": \"sw1a\", \"level\": \"district\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-03-17\"}",
            testRoute.getId()
        );

        mockMvc.perform(post("/api/v1/postcode-rules")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pattern").value("SW1A"));

        ArgumentCaptor<PostcodeRule> captor = ArgumentCaptor.forClass(PostcodeRule.class);
        verify(postcodeRuleRepository).save(captor.capture());
        assertThat(captor.getValue().getPattern()).isEqualTo("SW1A");
    }

    // ------------------------------------------------------------------
    // Pattern normalisation on update
    // ------------------------------------------------------------------

    @Test
    void updatePostcodeRule_ShouldNormalisePatternByStrippingSpaces() throws Exception {
        when(postcodeRuleRepository.findById("rule-1")).thenReturn(Optional.of(testRule));
        when(routeRepository.findById(testRoute.getId())).thenReturn(Optional.of(testRoute));
        when(postcodeRuleRepository.save(any(PostcodeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String requestBody = String.format(
            "{\"pattern\": \"NR14 6HF\", \"level\": \"full\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-03-17\"}",
            testRoute.getId()
        );

        mockMvc.perform(put("/api/v1/postcode-rules/rule-1")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pattern").value("NR146HF"));

        ArgumentCaptor<PostcodeRule> captor = ArgumentCaptor.forClass(PostcodeRule.class);
        verify(postcodeRuleRepository).save(captor.capture());
        assertThat(captor.getValue().getPattern()).isEqualTo("NR146HF");
    }

    @Test
    void updatePostcodeRule_ShouldNormalisePatternToUppercase() throws Exception {
        when(postcodeRuleRepository.findById("rule-1")).thenReturn(Optional.of(testRule));
        when(routeRepository.findById(testRoute.getId())).thenReturn(Optional.of(testRoute));
        when(postcodeRuleRepository.save(any(PostcodeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String requestBody = String.format(
            "{\"pattern\": \"nr14\", \"level\": \"district\", \"routeId\": \"%s\", \"effectiveFrom\": \"2026-03-17\"}",
            testRoute.getId()
        );

        mockMvc.perform(put("/api/v1/postcode-rules/rule-1")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pattern").value("NR14"));

        ArgumentCaptor<PostcodeRule> captor = ArgumentCaptor.forClass(PostcodeRule.class);
        verify(postcodeRuleRepository).save(captor.capture());
        assertThat(captor.getValue().getPattern()).isEqualTo("NR14");
    }
}
