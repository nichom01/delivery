import { apiClient } from '../apiClient';
import type { PostcodeRuleDto, CreatePostcodeRuleRequest, UpdatePostcodeRuleRequest, PostcodeLookupDto } from '../types';

export const postcodeService = {
  getPostcodeRules: async (depotId?: string): Promise<PostcodeRuleDto[]> => {
    const params: Record<string, string> = {};
    if (depotId) {
      params.depotId = depotId;
    }
    
    const response = await apiClient.get<PostcodeRuleDto[]>('/postcode-rules', params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch postcode rules');
    }
    
    return response.data;
  },
  
  createPostcodeRule: async (request: CreatePostcodeRuleRequest): Promise<PostcodeRuleDto> => {
    const response = await apiClient.post<PostcodeRuleDto>('/postcode-rules', request);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to create postcode rule');
    }
    
    return response.data;
  },
  
  updatePostcodeRule: async (ruleId: string, request: UpdatePostcodeRuleRequest): Promise<PostcodeRuleDto> => {
    const response = await apiClient.put<PostcodeRuleDto>(`/postcode-rules/${ruleId}`, request);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to update postcode rule');
    }
    
    return response.data;
  },
  
  lookupPostcode: async (postcode: string): Promise<PostcodeLookupDto> => {
    const response = await apiClient.get<PostcodeLookupDto>('/postcode-rules/lookup', { postcode });
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to lookup postcode');
    }
    
    return response.data;
  }
};
