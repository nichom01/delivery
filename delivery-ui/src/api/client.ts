import { mockData } from './mock-data';
import type { MockApiPayload } from './types';

/**
 * Single API function that returns all mock data.
 * Simulates a network delay for realism.
 */
export async function getMockData(): Promise<MockApiPayload> {
  // Simulate network delay
  await new Promise(resolve => setTimeout(resolve, 100));
  return mockData;
}

/**
 * Helper function to perform postcode lookup and return hierarchy
 */
export function lookupPostcode(postcode: string, rules: MockApiPayload['postcodeRules']): {
  hierarchy: Array<{ level: string; pattern: string; routeName: string; isMatch: boolean }>;
  matchedRoute?: string;
} {
  const normalized = postcode.toUpperCase().trim();
  const parts = normalized.split(' ');
  
  // Build hierarchy: full -> sector -> district -> area -> letter
  const full = normalized;
  const sector = parts[0] + (parts[1] ? ' ' + parts[1].substring(0, 1) : '');
  const district = parts[0];
  const area = parts[0].substring(0, 2);
  const letter = parts[0].substring(0, 1);
  
  const levels = [
    { level: 'Full Postcode', pattern: full },
    { level: 'Sector', pattern: sector },
    { level: 'District', pattern: district },
    { level: 'Area', pattern: area },
    { level: 'Letter', pattern: letter },
  ];
  
  let matchedRoute: string | undefined;
  let matchedLevel: string | undefined;
  
  // Find match using longest-match-wins
  for (const level of levels) {
    const rule = rules.find(r => 
      r.pattern.toUpperCase() === level.pattern.toUpperCase() &&
      (!r.effectiveTo || new Date(r.effectiveTo) >= new Date())
    );
    if (rule) {
      matchedRoute = rule.routeName;
      matchedLevel = level.level;
      break;
    }
  }
  
  const hierarchy = levels.map(level => {
    const rule = rules.find(r => 
      r.pattern.toUpperCase() === level.pattern.toUpperCase() &&
      (!r.effectiveTo || new Date(r.effectiveTo) >= new Date())
    );
    return {
      level: level.level,
      pattern: level.pattern,
      routeName: rule?.routeName || '',
      isMatch: level.level === matchedLevel,
    };
  });
  
  return { hierarchy, matchedRoute };
}
