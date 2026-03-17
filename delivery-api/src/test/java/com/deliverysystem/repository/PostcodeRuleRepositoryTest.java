package com.deliverysystem.repository;

import com.deliverysystem.domain.Depot;
import com.deliverysystem.domain.PostcodeRule;
import com.deliverysystem.domain.PostcodeRule.PostcodeLevel;
import com.deliverysystem.domain.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer tests for PostcodeRuleRepository.findMatchingRules().
 *
 * Runs against an in-memory H2 database with the JPA schema auto-created.
 *
 * Key invariants verified:
 *  - A stored pattern is only returned when it IS a prefix of the input
 *    (longest-prefix semantics: stored "NR14" matches input "NR146HF" ✓)
 *  - A stored pattern that is MORE SPECIFIC than the input is NOT returned
 *    (stored "NR146HF" must NOT match input "NR14" ✗ — the previously buggy OR condition)
 *  - When both a FULL and a DISTRICT rule match, FULL is returned first
 *  - Inactive rules (outside effective date range) are excluded
 */
@DataJpaTest
@ActiveProfiles("test")
class PostcodeRuleRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PostcodeRuleRepository repository;

    private Route routeA;
    private Route routeB;
    private final LocalDate TODAY = LocalDate.of(2026, 3, 17);

    @BeforeEach
    void setUp() {
        Depot depot = new Depot();
        depot.setName("Test Depot");
        depot.setAddress("1 Test Street, Norwich");
        em.persist(depot);

        routeA = new Route();
        routeA.setCode("5001");
        routeA.setName("Route A");
        routeA.setDepot(depot);
        em.persist(routeA);

        routeB = new Route();
        routeB.setCode("5002");
        routeB.setName("Route B");
        routeB.setDepot(depot);
        em.persist(routeB);

        em.flush();
    }

    private PostcodeRule persist(String pattern, PostcodeLevel level, Route route,
                                 LocalDate from, LocalDate to) {
        PostcodeRule rule = new PostcodeRule();
        rule.setPattern(pattern);
        rule.setLevel(level);
        rule.setRoute(route);
        rule.setEffectiveFrom(from);
        rule.setEffectiveTo(to);
        em.persist(rule);
        em.flush();
        return rule;
    }

    private PostcodeRule persist(String pattern, PostcodeLevel level, Route route) {
        return persist(pattern, level, route, TODAY.minusDays(30), null);
    }

    // ------------------------------------------------------------------
    // Core longest-prefix semantics
    // ------------------------------------------------------------------

    @Test
    void findMatchingRules_DistrictRuleMatchesFullPostcodeInput() {
        // Stored "NR14" should match when input is the full postcode "NR146HF"
        persist("NR14", PostcodeLevel.DISTRICT, routeA);

        List<PostcodeRule> results = repository.findMatchingRules("NR146HF", TODAY);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPattern()).isEqualTo("NR14");
    }

    @Test
    void findMatchingRules_FullRuleMatchesFullPostcodeInput() {
        persist("NR146HF", PostcodeLevel.FULL, routeA);

        List<PostcodeRule> results = repository.findMatchingRules("NR146HF", TODAY);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPattern()).isEqualTo("NR146HF");
    }

    // ------------------------------------------------------------------
    // Bug regression: more-specific rule must NOT match shorter input
    // ------------------------------------------------------------------

    @Test
    void findMatchingRules_FullRuleDoesNotMatchDistrictInput() {
        // BUG: previously the OR condition made "NR146HF" match input "NR14"
        persist("NR146HF", PostcodeLevel.FULL, routeB);

        List<PostcodeRule> results = repository.findMatchingRules("NR14", TODAY);

        assertThat(results).isEmpty();
    }

    @Test
    void findMatchingRules_SectorRuleDoesNotMatchDistrictInput() {
        persist("NR146", PostcodeLevel.SECTOR, routeB);

        List<PostcodeRule> results = repository.findMatchingRules("NR14", TODAY);

        assertThat(results).isEmpty();
    }

    // ------------------------------------------------------------------
    // Ordering: most specific rule first
    // ------------------------------------------------------------------

    @Test
    void findMatchingRules_FullRuleRanksBeforeDistrictRule() {
        PostcodeRule district = persist("NR14",    PostcodeLevel.DISTRICT, routeB);
        PostcodeRule full     = persist("NR146HF", PostcodeLevel.FULL,     routeA);

        List<PostcodeRule> results = repository.findMatchingRules("NR146HF", TODAY);

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results.get(0).getId()).isEqualTo(full.getId());
    }

    @Test
    void findMatchingRules_SectorRuleRanksBeforeDistrictRule() {
        PostcodeRule district = persist("NR14",  PostcodeLevel.DISTRICT, routeB);
        PostcodeRule sector   = persist("NR146", PostcodeLevel.SECTOR,   routeA);

        List<PostcodeRule> results = repository.findMatchingRules("NR146HF", TODAY);

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results.get(0).getId()).isEqualTo(sector.getId());
    }

    @Test
    void findMatchingRules_DistrictInputReturnsOnlyDistrictWhenBothExist() {
        // "NR14" as input: district rule should match, full rule should NOT
        PostcodeRule district = persist("NR14",    PostcodeLevel.DISTRICT, routeA);
        PostcodeRule full     = persist("NR146HF", PostcodeLevel.FULL,     routeB);

        List<PostcodeRule> results = repository.findMatchingRules("NR14", TODAY);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(district.getId());
    }

    // ------------------------------------------------------------------
    // Effective date filtering
    // ------------------------------------------------------------------

    @Test
    void findMatchingRules_ExcludesExpiredRules() {
        // Rule expired yesterday — should not be returned
        persist("SW1", PostcodeLevel.DISTRICT, routeA, TODAY.minusDays(30), TODAY.minusDays(1));

        List<PostcodeRule> results = repository.findMatchingRules("SW1A1AA", TODAY);

        assertThat(results).isEmpty();
    }

    @Test
    void findMatchingRules_ExcludesRulesNotYetEffective() {
        // Rule starts tomorrow
        persist("SW1", PostcodeLevel.DISTRICT, routeA, TODAY.plusDays(1), null);

        List<PostcodeRule> results = repository.findMatchingRules("SW1A1AA", TODAY);

        assertThat(results).isEmpty();
    }

    @Test
    void findMatchingRules_IncludesRuleActiveOnDate() {
        persist("SW1", PostcodeLevel.DISTRICT, routeA, TODAY, TODAY);

        List<PostcodeRule> results = repository.findMatchingRules("SW1A1AA", TODAY);

        assertThat(results).hasSize(1);
    }

    @Test
    void findMatchingRules_IncludesRuleWithNoEndDate() {
        persist("SW1", PostcodeLevel.DISTRICT, routeA, TODAY.minusDays(1), null);

        List<PostcodeRule> results = repository.findMatchingRules("SW1A1AA", TODAY);

        assertThat(results).hasSize(1);
    }

    // ------------------------------------------------------------------
    // Area and letter fallback rules
    // ------------------------------------------------------------------

    @Test
    void findMatchingRules_AreaRuleMatchesFullPostcode() {
        persist("SW", PostcodeLevel.AREA, routeA);

        List<PostcodeRule> results = repository.findMatchingRules("SW1A1AA", TODAY);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPattern()).isEqualTo("SW");
    }

    @Test
    void findMatchingRules_LetterRuleMatchesFullPostcode() {
        persist("S", PostcodeLevel.LETTER, routeA);

        List<PostcodeRule> results = repository.findMatchingRules("SW1A1AA", TODAY);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPattern()).isEqualTo("S");
    }

    @Test
    void findMatchingRules_ReturnsEmptyWhenNoRulesMatch() {
        persist("SW1", PostcodeLevel.DISTRICT, routeA);

        List<PostcodeRule> results = repository.findMatchingRules("NR146HF", TODAY);

        assertThat(results).isEmpty();
    }
}
