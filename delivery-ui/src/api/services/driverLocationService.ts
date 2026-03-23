import { apiClient } from '../apiClient';
import type { DriverLocationSampleDto } from '../types';

export const driverLocationService = {
  listForUser: async (
    userId: string,
    date?: string
  ): Promise<DriverLocationSampleDto[]> => {
    const params: Record<string, string> = { userId };
    if (date) {
      params.date = date;
    }
    const response = await apiClient.get<DriverLocationSampleDto[]>('/driver-locations', params);
    if (!response.success) {
      throw new Error(response.message);
    }
    return response.data;
  },
};
