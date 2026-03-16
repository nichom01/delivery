import { apiClient } from '../apiClient';
import type { AuditEventDto } from '../types';

export const auditService = {
  getAuditEvents: async (depotId?: string): Promise<AuditEventDto[]> => {
    const params: Record<string, string> = {};
    if (depotId) {
      params.depotId = depotId;
    }
    
    const response = await apiClient.get<AuditEventDto[]>('/audit', params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch audit events');
    }
    
    return response.data;
  }
};
