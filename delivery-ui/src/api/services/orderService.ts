import { apiClient } from '../apiClient';
import type { CreateOrderRequest, RouteDto, OrderAwaitingGoodsDto, BoxDto, OrderDto } from '../types';

export const orderService = {
  createOrder: async (request: CreateOrderRequest): Promise<RouteDto> => {
    const response = await apiClient.post<RouteDto>('/orders', request);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to create order');
    }
    
    return response.data;
  },
  
  getOrdersAwaitingGoods: async (depotId?: string): Promise<OrderAwaitingGoodsDto[]> => {
    const params: Record<string, string> = {};
    if (depotId) {
      params.depotId = depotId;
    }
    
    const response = await apiClient.get<OrderAwaitingGoodsDto[]>('/orders/awaiting-goods', params);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to fetch orders awaiting goods');
    }
    
    return response.data;
  },
  
  receiveBox: async (boxId: string): Promise<BoxDto> => {
    const response = await apiClient.post<BoxDto>(`/orders/boxes/${boxId}/receive`);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to receive box');
    }
    
    return response.data;
  },
  
  flagException: async (orderId: string, reason: string): Promise<OrderDto> => {
    const response = await apiClient.post<OrderDto>(`/orders/${orderId}/flag-exception`, { reason });
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to flag exception');
    }
    
    return response.data;
  },
  
  markReadyForManifest: async (orderId: string): Promise<OrderDto> => {
    const response = await apiClient.post<OrderDto>(`/orders/${orderId}/ready-for-manifest`);
    
    if (!response.success) {
      throw new Error(response.message || 'Failed to mark order as ready for manifest');
    }
    
    return response.data;
  }
};
