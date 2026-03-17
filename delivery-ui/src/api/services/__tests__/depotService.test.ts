/**
 * Test file to recreate depot create/update issues
 * Run this in browser console or as a unit test
 */

import { depotService } from '../depotService';
import { apiClient } from '../../apiClient';

// Mock the apiClient
vi.mock('../../apiClient', () => ({
  apiClient: {
    post: vi.fn(),
    put: vi.fn(),
    get: vi.fn(),
  },
}));

describe('DepotService - Create Depot', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('should create depot successfully', async () => {
    const mockResponse = {
      success: true,
      message: 'Depot created successfully',
      data: {
        id: 'depot-1',
        name: 'Test Depot',
        location: '123 Test Street',
        routesCount: 0,
        vehiclesCount: 0,
        driversCount: 0,
        status: 'ACTIVE',
      },
    };

    (apiClient.post as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    const request = {
      name: 'Test Depot',
      address: '123 Test Street',
    };

    const result = await depotService.createDepot(request);

    expect(apiClient.post).toHaveBeenCalledWith('/depots', request);
    expect(result).toEqual(mockResponse.data);
  });

  test('should throw error when response.success is false', async () => {
    const mockErrorResponse = {
      success: false,
      message: 'Depot with name "Test Depot" already exists',
    };

    (apiClient.post as ReturnType<typeof vi.fn>).mockResolvedValue(mockErrorResponse);

    const request = {
      name: 'Test Depot',
      address: '123 Test Street',
    };

    await expect(depotService.createDepot(request)).rejects.toThrow(
      'Depot with name "Test Depot" already exists'
    );
  });

  test('should throw error when response.data is missing', async () => {
    const mockResponse = {
      success: true,
      message: 'Depot created successfully',
      // data is missing
    };

    (apiClient.post as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    const request = {
      name: 'Test Depot',
      address: '123 Test Street',
    };

    await expect(depotService.createDepot(request)).rejects.toThrow(
      'No data returned from server'
    );
  });

  test('should handle validation errors', async () => {
    const mockErrorResponse = {
      success: false,
      message: 'Validation failed: name Depot name is required, address Address is required',
    };

    (apiClient.post as ReturnType<typeof vi.fn>).mockResolvedValue(mockErrorResponse);

    const request = {
      name: '',
      address: '',
    };

    await expect(depotService.createDepot(request)).rejects.toThrow(
      'Validation failed: name Depot name is required, address Address is required'
    );
  });

  test('should handle HTTP errors', async () => {
    const mockErrorResponse = {
      success: false,
      message: 'HTTP error! status: 500',
    };

    (apiClient.post as ReturnType<typeof vi.fn>).mockResolvedValue(mockErrorResponse);

    const request = {
      name: 'Test Depot',
      address: '123 Test Street',
    };

    await expect(depotService.createDepot(request)).rejects.toThrow(
      'HTTP error! status: 500'
    );
  });
});

describe('DepotService - Update Depot', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('should update depot successfully', async () => {
    const mockResponse = {
      success: true,
      message: 'Depot updated successfully',
      data: {
        id: 'depot-1',
        name: 'Updated Depot',
        location: '456 Updated Street',
        routesCount: 5,
        vehiclesCount: 3,
        driversCount: 8,
        status: 'ACTIVE',
      },
    };

    (apiClient.put as ReturnType<typeof vi.fn>).mockResolvedValue(mockResponse);

    const request = {
      name: 'Updated Depot',
      address: '456 Updated Street',
    };

    const result = await depotService.updateDepot('depot-1', request);

    expect(apiClient.put).toHaveBeenCalledWith('/depots/depot-1', request);
    expect(result).toEqual(mockResponse.data);
  });

  test('should throw error when depot not found', async () => {
    const mockErrorResponse = {
      success: false,
      message: 'Depot not found: depot-999',
    };

    (apiClient.put as ReturnType<typeof vi.fn>).mockResolvedValue(mockErrorResponse);

    const request = {
      name: 'Updated Depot',
    };

    await expect(depotService.updateDepot('depot-999', request)).rejects.toThrow(
      'Depot not found: depot-999'
    );
  });
});

// Manual test function to run in browser console
export async function testCreateDepot() {
  console.log('Testing depot creation...');
  
  try {
    const request = {
      name: 'Test Depot ' + Date.now(),
      address: '123 Test Street',
    };
    
    console.log('Request:', request);
    const result = await depotService.createDepot(request);
    console.log('Success! Result:', result);
    return result;
  } catch (error) {
    console.error('Error creating depot:', error);
    console.error('Error message:', error instanceof Error ? error.message : String(error));
    console.error('Full error:', error);
    throw error;
  }
}

export async function testUpdateDepot(depotId: string) {
  console.log('Testing depot update...');
  
  try {
    const request = {
      name: 'Updated Depot ' + Date.now(),
      address: '456 Updated Street',
    };
    
    console.log('Request:', request);
    const result = await depotService.updateDepot(depotId, request);
    console.log('Success! Result:', result);
    return result;
  } catch (error) {
    console.error('Error updating depot:', error);
    console.error('Error message:', error instanceof Error ? error.message : String(error));
    console.error('Full error:', error);
    throw error;
  }
}
