package com.deliverysystem.service;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.PostcodeRule;
import com.deliverysystem.domain.PostcodeRule.PostcodeLevel;
import com.deliverysystem.domain.Route;
import com.deliverysystem.repository.PostcodeRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostcodeRoutingService.
 *
 * Key invariants under test:
 *  - Longest / most-specific match wins (FULL > SECTOR > DISTRICT > AREA > LETTER)
 *  - A rule must not match an input that is shorter than the rule pattern
 *    (e.g. input "NR14" must NOT match a FULL rule "NR146HF")
 *  - Input postcodes are normalised (spaces stripped, uppercased) before matching
 */
@ExtendWith(MockitoExtension.class)
class PostcodeRoutingServiceTest {

    @Mock
    private PostcodeRuleRepository postcodeRuleRepository;

    @InjectMocks
    private PostcodeRoutingService service;

    private Route routeA;
    private Route routeB;
    private final LocalDate TODAY = LocalDate.of(2026, 3, 17);

    @BeforeEach
    void setUp() {
        Depot depot = new Depot();
        depot.setId("depot-1");
        depot.setName("Test Depot");

        routeA = new Route();
        routeA.setId("route-a");
        routeA.setCode("5001");
        routeA.setName("Route A");
        routeA.setDepot(depot);

        routeB = new Route();
        routeB.setId("route-b");
        routeB.setCode("5002");
        routeB.setName("Route B");
        routeB.setDepot(depot);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private PostcodeRule rule(String pattern, PostcodeLevel level, Route route) {
        PostcodeRule r = new PostcodeRule();
        r.setId(java.util.UUID.randomUUID().toString());
        r.setPattern(pattern);
        r.setLevel(level);
        r.setRoute(route);
        r.setEffectiveFrom(TODAY.minusDays(1));
        return r;
    }

    // ------------------------------------------------------------------
    // Normalisation
    // ------------------------------------------------------------------

    @Test
    void resolvePostcodeToRoute_NormalisesInputByStrippingSpaces() {
        PostcodeRule fullRule = rule("SW1A1AA", PostcodeLevel.FULL, routeA);
        when(postcodeRuleRepository.findMatchingRules(eq("SW1A1AA"), any()))
                .thenReturn(List.of(fullRule));

        // Input has spaces and lowercase — should still resolve
        Optional<Route> result = service.resolvePostcodeToRoute("sw1a 1aa", TODAY);

        assertThat(result).isPresent().contains(routeA);
    }

    @Test
    void resolvePostcodeToRoute_NormalisesInputByUppercasing() {
        PostcodeRule districtRule = rule("NR14", PostcodeLevel.DISTRICT, routeA);
        when(postcodeRuleRepository.findMatchingRules(eq("NR14"), any()))
                .thenReturn(List.of(districtRule));

        Optional<Route> result = service.resolvePostcodeToRoute("nr14", TODAY);

        assertThat(result).isPresent().contains(routeA);
    }

    // ------------------------------------------------------------------
    // Exact / most-specific match
    // ------------------------------------------------------------------

    @Test
    void resolvePostcodeToRoute_FullPostcodeMatchesFullRule() {
        PostcodeRule fullRule     = rule("NR146HF", PostcodeLevel.FULL,     routeA);
        PostcodeRule districtRule = rule("NR14",    PostcodeLevel.DISTRICT, routeB);

        // First prefix tried is the full normalised postcode; the repo returns both
        // rules ordered FULL first — service should stop here and return routeA.
        when(postcodeRuleRepository.findMatchingRules(eq("NR146HF"), any()))
                .thenReturn(List.of(fullRule, districtRule));

        Optional<Route> result = service.resolvePostcodeToRoute("NR14 6HF", TODAY);

        assertThat(result).isPresent().contains(routeA);
        // Subsequent (shorter) prefixes must not be queried because we already matched
        verify(postcodeRuleRepository, times(1)).findMatchingRules(any(), any());
    }

    @Test
    void resolvePostcodeToRoute_DistrictInputMatchesDistrictRuleOnly() {
        // BUG REGRESSION: input "NR14" must NOT match a FULL rule "NR146HF".
        // The corrected query only returns rules where the stored pattern is a
        // prefix of the input, so "NR146HF" is absent from results for input "NR14".
        PostcodeRule districtRule = rule("NR14", PostcodeLevel.DISTRICT, routeA);

        when(postcodeRuleRepository.findMatchingRules(eq("NR14"), any()))
                .thenReturn(List.of(districtRule));

        Optional<Route> result = service.resolvePostcodeToRoute("NR14", TODAY);

        assertThat(result).isPresent().contains(routeA);
    }

    @Test
    void resolvePostcodeToRoute_FallsBackToDistrictWhenNoFullMatch() {
        PostcodeRule districtRule = rule("NR14", PostcodeLevel.DISTRICT, routeA);

        when(postcodeRuleRepository.findMatchingRules(eq("NR146HF"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR146"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR14"), any()))
                .thenReturn(List.of(districtRule));

        Optional<Route> result = service.resolvePostcodeToRoute("NR14 6HF", TODAY);

        assertThat(result).isPresent().contains(routeA);
    }

    @Test
    void resolvePostcodeToRoute_FallsBackToAreaWhenNoDistrictMatch() {
        PostcodeRule areaRule = rule("NR", PostcodeLevel.AREA, routeA);

        when(postcodeRuleRepository.findMatchingRules(eq("NR146HF"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR146"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR14"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR"), any()))
                .thenReturn(List.of(areaRule));

        Optional<Route> result = service.resolvePostcodeToRoute("NR14 6HF", TODAY);

        assertThat(result).isPresent().contains(routeA);
    }

    @Test
    void resolvePostcodeToRoute_FallsBackToLetterWhenNoAreaMatch() {
        PostcodeRule letterRule = rule("N", PostcodeLevel.LETTER, routeA);

        when(postcodeRuleRepository.findMatchingRules(eq("NR146HF"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR146"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR14"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("NR"), any()))
                .thenReturn(Collections.emptyList());
        when(postcodeRuleRepository.findMatchingRules(eq("N"), any()))
                .thenReturn(List.of(letterRule));

        Optional<Route> result = service.resolvePostcodeToRoute("NR14 6HF", TODAY);

        assertThat(result).isPresent().contains(routeA);
    }

    // ------------------------------------------------------------------
    // No match
    // ------------------------------------------------------------------

    @Test
    void resolvePostcodeToRoute_ReturnsEmptyWhenNoRuleFound() {
        when(postcodeRuleRepository.findMatchingRules(any(), any()))
                .thenReturn(Collections.emptyList());

        Optional<Route> result = service.resolvePostcodeToRoute("ZZ99 9ZZ", TODAY);

        assertThat(result).isEmpty();
    }

    @Test
    void resolvePostcodeToRoute_ReturnsEmptyForNullInput() {
        Optional<Route> result = service.resolvePostcodeToRoute(null, TODAY);

        assertThat(result).isEmpty();
        verifyNoInteractions(postcodeRuleRepository);
    }

    @Test
    void resolvePostcodeToRoute_ReturnsEmptyForBlankInput() {
        Optional<Route> result = service.resolvePostcodeToRoute("   ", TODAY);

        assertThat(result).isEmpty();
        verifyNoInteractions(postcodeRuleRepository);
    }

    // ------------------------------------------------------------------
    // Default date
    // ------------------------------------------------------------------

    @Test
    void resolvePostcodeToRoute_UsesTodayWhenDateIsNull() {
        PostcodeRule districtRule = rule("SW1", PostcodeLevel.DISTRICT, routeA);
        when(postcodeRuleRepository.findMatchingRules(any(), any()))
                .thenReturn(List.of(districtRule));

        Optional<Route> result = service.resolvePostcodeToRoute("SW1A 1AA", null);

        assertThat(result).isPresent();
        // Verify it called the repo with a non-null date (today)
        verify(postcodeRuleRepository, atLeastOnce())
                .findMatchingRules(any(), eq(LocalDate.now()));
    }
}
