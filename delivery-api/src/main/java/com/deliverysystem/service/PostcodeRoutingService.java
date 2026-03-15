package com.deliverysystem.service;

import com.deliverysystem.domain.PostcodeRule;
import com.deliverysystem.domain.Route;
import com.deliverysystem.repository.PostcodeRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostcodeRoutingService {
    
    private static final Logger log = LoggerFactory.getLogger(PostcodeRoutingService.class);
    private final PostcodeRuleRepository postcodeRuleRepository;
    
    public PostcodeRoutingService(PostcodeRuleRepository postcodeRuleRepository) {
        this.postcodeRuleRepository = postcodeRuleRepository;
    }
    
    /**
     * Resolves a postcode to a route using hierarchical longest-match-wins algorithm.
     * 
     * @param postcode The delivery postcode (e.g., "SW1A 1AA")
     * @param date The date to evaluate rules for (defaults to today)
     * @return The route assigned to this postcode, or empty if no match found
     */
    @Transactional(readOnly = true)
    public Optional<Route> resolvePostcodeToRoute(String postcode, LocalDate date) {
        if (postcode == null || postcode.trim().isEmpty()) {
            return Optional.empty();
        }
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Normalize postcode: remove spaces, convert to uppercase
        String normalizedPostcode = normalizePostcode(postcode);
        
        // Generate all possible prefixes in order of specificity
        List<String> prefixes = generatePostcodePrefixes(normalizedPostcode);
        
        // Find matching rules for each prefix level
        for (String prefix : prefixes) {
            List<PostcodeRule> matchingRules = postcodeRuleRepository.findMatchingRules(prefix, date);
            
            if (!matchingRules.isEmpty()) {
                // Return the first match (already sorted by specificity)
                PostcodeRule matchedRule = matchingRules.get(0);
                log.debug("Postcode {} matched rule {} at level {} for route {}", 
                    postcode, matchedRule.getPattern(), matchedRule.getLevel(), matchedRule.getRoute().getId());
                return Optional.of(matchedRule.getRoute());
            }
        }
        
        log.warn("No route found for postcode: {}", postcode);
        return Optional.empty();
    }
    
    /**
     * Gets the hierarchy of matching rules for a postcode (for display/debugging).
     */
    @Transactional(readOnly = true)
    public List<PostcodeRule> getPostcodeHierarchy(String postcode, LocalDate date) {
        if (postcode == null || postcode.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        String normalizedPostcode = normalizePostcode(postcode);
        List<String> prefixes = generatePostcodePrefixes(normalizedPostcode);
        
        List<PostcodeRule> hierarchy = new ArrayList<>();
        for (String prefix : prefixes) {
            List<PostcodeRule> matchingRules = postcodeRuleRepository.findMatchingRules(prefix, date);
            if (!matchingRules.isEmpty()) {
                hierarchy.add(matchingRules.get(0));
            }
        }
        
        return hierarchy;
    }
    
    /**
     * Normalizes a UK postcode by removing spaces and converting to uppercase.
     */
    private String normalizePostcode(String postcode) {
        return postcode.replaceAll("\\s+", "").toUpperCase();
    }
    
    /**
     * Generates all possible postcode prefixes in order of specificity (most specific first).
     * 
     * Example: "SW1A1AA" generates:
     * 1. SW1A1AA (full)
     * 2. SW1A1 (sector)
     * 3. SW1A (district)
     * 4. SW (area)
     * 5. S (letter)
     */
    private List<String> generatePostcodePrefixes(String normalizedPostcode) {
        List<String> prefixes = new ArrayList<>();
        
        if (normalizedPostcode.length() >= 7) {
            // Full postcode: SW1A1AA
            prefixes.add(normalizedPostcode);
        }
        if (normalizedPostcode.length() >= 5) {
            // Sector: SW1A1
            prefixes.add(normalizedPostcode.substring(0, 5));
        }
        if (normalizedPostcode.length() >= 4) {
            // District: SW1A
            prefixes.add(normalizedPostcode.substring(0, 4));
        }
        if (normalizedPostcode.length() >= 2) {
            // Area: SW
            prefixes.add(normalizedPostcode.substring(0, 2));
        }
        if (normalizedPostcode.length() >= 1) {
            // Letter: S
            prefixes.add(normalizedPostcode.substring(0, 1));
        }
        
        return prefixes;
    }
    
    /**
     * Seeds A-Z fallback rules for a given route.
     * This ensures every postcode can be resolved.
     */
    @Transactional
    public void seedFallbackRules(Route route) {
        LocalDate effectiveFrom = LocalDate.now();
        
        for (char letter = 'A'; letter <= 'Z'; letter++) {
            String pattern = String.valueOf(letter);
            
            // Check if rule already exists
            boolean exists = postcodeRuleRepository.findAll().stream()
                .anyMatch(rule -> rule.getPattern().equals(pattern) 
                    && rule.getLevel() == PostcodeRule.PostcodeLevel.LETTER
                    && rule.getEffectiveTo() == null);
            
            if (!exists) {
                PostcodeRule rule = new PostcodeRule();
                rule.setPattern(pattern);
                rule.setLevel(PostcodeRule.PostcodeLevel.LETTER);
                rule.setRoute(route);
                rule.setEffectiveFrom(effectiveFrom);
                rule.setEffectiveTo(null);
                
                postcodeRuleRepository.save(rule);
                log.info("Seeded fallback rule: {} -> route {}", pattern, route.getId());
            }
        }
    }
}
