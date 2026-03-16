import { apiClient } from '../apiClient';
import type { RouteDto, RouteDrilldownDto, CreateRouteRequest, UpdateRouteRequest } from '../types';

export const routeService = {
  getRoute: async (routeId: string): Promise<RouteDto> => {
    const response = await apiClient.get<RouteDto>(`/routes/${routeId}`);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch route');
    }
    
    return response.data;
  },
  
  getRouteDrilldown: async (routeId: string, date?: string): Promise<RouteDrilldownDto> => {
    const params: Record<string, string> = {};
    if (date) {
      params.date = date;
    }
    
    const response = await apiClient.get<RouteDrilldownDto>(`/routes/${routeId}/drilldown`, params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch route drilldown');
    }
    
    return response.data;
  },
  
  createRoute: async (request: CreateRouteRequest): Promise<RouteDto> => {
    const response = await apiClient.post<RouteDto>('/routes', request);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to create route');
    }
    
    return response.data;
  },
  
  updateRoute: async (routeId: string, request: UpdateRouteRequest): Promise<RouteDto> => {
    const response = await apiClient.put<RouteDto>(`/routes/${routeId}`, request);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to update route');
    }
    
    return response.data;
  }
};
