import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import Depots from '../Depots';
import { depotService } from '@/api/services/depotService';
import * as AppContext from '@/contexts/AppContext';

// Mock the dependencies
vi.mock('@/api/services/depotService');
vi.mock('@/contexts/AppContext', () => ({
  useApp: vi.fn(),
  AppProvider: ({ children }: { children: React.ReactNode }) => children,
}));
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

const mockDepotService = vi.mocked(depotService);
const mockUseApp = vi.mocked(AppContext.useApp);

describe('Depots Component - Form Submission', () => {
  const mockRefreshDepots = vi.fn();
  const mockSetSelectedDepotId = vi.fn();

  const mockDepots = [
    {
      id: 'depot-1',
      name: 'London Central',
      location: 'Vauxhall, London',
      routesCount: 5,
      vehiclesCount: 10,
      driversCount: 20,
      status: 'ACTIVE',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    
    // Setup default mock for useApp
    mockUseApp.mockReturnValue({
      currentUser: {
        name: 'Test User',
        role: 'CENTRAL_ADMIN',
        initials: 'TU',
        depotId: null,
      },
      selectedDepotId: null,
      setSelectedDepotId: mockSetSelectedDepotId,
      depots: mockDepots,
      data: {
        depots: mockDepots,
        users: [],
      },
      loading: false,
      getDepotById: vi.fn((id: string) => mockDepots.find(d => d.id === id)),
      refreshDepots: mockRefreshDepots,
    });
  });

  const renderDepots = () => {
    return render(
      <BrowserRouter>
        <Depots />
      </BrowserRouter>
    );
  };

  it('should call createDepot API when creating a new depot', async () => {
    const user = userEvent.setup();
    
    // Mock successful depot creation
    const mockCreatedDepot = {
      id: 'depot-new',
      name: 'New Test Depot',
      location: '123 Test Street',
      routesCount: 0,
      vehiclesCount: 0,
      driversCount: 0,
      status: 'ACTIVE',
    };

    mockDepotService.createDepot.mockResolvedValue(mockCreatedDepot);
    mockRefreshDepots.mockResolvedValue(undefined);

    renderDepots();

    // Click "Add Depot" button
    const addButton = screen.getByText('+ Add Depot');
    await user.click(addButton);

    // Wait for dialog to appear
    await waitFor(() => {
      expect(screen.getByText('Add Depot')).toBeInTheDocument();
    });

    // Fill in the form
    const nameInput = screen.getByPlaceholderText('e.g. London Central');
    const addressInput = screen.getByPlaceholderText('e.g. Vauxhall, London');
    const createButton = screen.getByText('Create');

    await user.type(nameInput, 'New Test Depot');
    await user.type(addressInput, '123 Test Street');

    // Submit the form
    await user.click(createButton);

    // Verify that createDepot was called with correct parameters
    await waitFor(() => {
      expect(mockDepotService.createDepot).toHaveBeenCalledTimes(1);
      expect(mockDepotService.createDepot).toHaveBeenCalledWith({
        name: 'New Test Depot',
        address: '123 Test Street',
      });
    });

    // Verify that refreshDepots was called
    await waitFor(() => {
      expect(mockRefreshDepots).toHaveBeenCalledTimes(1);
    });
  });

  it('should call createDepot API with coordinates when provided', async () => {
    const user = userEvent.setup();
    
    const mockCreatedDepot = {
      id: 'depot-new',
      name: 'New Test Depot',
      location: '123 Test Street',
      routesCount: 0,
      vehiclesCount: 0,
      driversCount: 0,
      status: 'ACTIVE',
    };

    mockDepotService.createDepot.mockResolvedValue(mockCreatedDepot);
    mockRefreshDepots.mockResolvedValue(undefined);

    renderDepots();

    // Open dialog
    const addButton = screen.getByText('+ Add Depot');
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add Depot')).toBeInTheDocument();
    });

    // Fill in all fields including coordinates
    const nameInput = screen.getByPlaceholderText('e.g. London Central');
    const addressInput = screen.getByPlaceholderText('e.g. Vauxhall, London');
    const latitudeInput = screen.getByPlaceholderText('e.g. 51.4865');
    const longitudeInput = screen.getByPlaceholderText('e.g. -0.1234');
    const createButton = screen.getByText('Create');

    await user.type(nameInput, 'New Test Depot');
    await user.type(addressInput, '123 Test Street');
    await user.type(latitudeInput, '51.5074');
    await user.type(longitudeInput, '-0.1278');

    // Submit the form
    await user.click(createButton);

    // Verify that createDepot was called with coordinates
    await waitFor(() => {
      expect(mockDepotService.createDepot).toHaveBeenCalledTimes(1);
      expect(mockDepotService.createDepot).toHaveBeenCalledWith({
        name: 'New Test Depot',
        address: '123 Test Street',
        latitude: '51.5074',
        longitude: '-0.1278',
      });
    });
  });

  it('should call updateDepot API when editing an existing depot', async () => {
    const user = userEvent.setup();
    
    const mockUpdatedDepot = {
      id: 'depot-1',
      name: 'Updated Depot Name',
      location: '456 Updated Street',
      routesCount: 5,
      vehiclesCount: 10,
      driversCount: 20,
      status: 'ACTIVE',
    };

    mockDepotService.updateDepot.mockResolvedValue(mockUpdatedDepot);
    mockRefreshDepots.mockResolvedValue(undefined);

    renderDepots();

    // Click "Edit" button for the first depot
    const editButtons = screen.getAllByText('Edit');
    await user.click(editButtons[0]);

    // Wait for dialog to appear with "Edit Depot" title
    await waitFor(() => {
      expect(screen.getByText('Edit Depot')).toBeInTheDocument();
    });

    // Update the form fields
    const nameInput = screen.getByDisplayValue('London Central');
    const addressInput = screen.getByDisplayValue('Vauxhall, London');
    const updateButton = screen.getByText('Update');

    // Clear and type new values
    await user.clear(nameInput);
    await user.type(nameInput, 'Updated Depot Name');
    await user.clear(addressInput);
    await user.type(addressInput, '456 Updated Street');

    // Submit the form
    await user.click(updateButton);

    // Verify that updateDepot was called with correct parameters
    await waitFor(() => {
      expect(mockDepotService.updateDepot).toHaveBeenCalledTimes(1);
      expect(mockDepotService.updateDepot).toHaveBeenCalledWith(
        'depot-1',
        {
          name: 'Updated Depot Name',
          address: '456 Updated Street',
        }
      );
    });

    // Verify that refreshDepots was called
    await waitFor(() => {
      expect(mockRefreshDepots).toHaveBeenCalledTimes(1);
    });
  });

  it('should not call API when form validation fails (empty name)', async () => {
    const user = userEvent.setup();

    renderDepots();

    // Open dialog
    const addButton = screen.getByText('+ Add Depot');
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add Depot')).toBeInTheDocument();
    });

    // Fill only address, leave name empty
    const addressInput = screen.getByPlaceholderText('e.g. Vauxhall, London');
    const createButton = screen.getByText('Create');

    await user.type(addressInput, '123 Test Street');

    // Create button should be disabled when name is empty
    expect(createButton).toBeDisabled();

    // Verify that createDepot was NOT called
    expect(mockDepotService.createDepot).not.toHaveBeenCalled();
  });

  it('should not call API when form validation fails (empty address)', async () => {
    const user = userEvent.setup();

    renderDepots();

    // Open dialog
    const addButton = screen.getByText('+ Add Depot');
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add Depot')).toBeInTheDocument();
    });

    // Fill only name, leave address empty
    const nameInput = screen.getByPlaceholderText('e.g. London Central');
    const createButton = screen.getByText('Create');

    await user.type(nameInput, 'New Test Depot');

    // Create button should be disabled when address is empty
    expect(createButton).toBeDisabled();

    // Verify that createDepot was NOT called
    expect(mockDepotService.createDepot).not.toHaveBeenCalled();
  });

  it('should exclude empty coordinate fields from API call', async () => {
    const user = userEvent.setup();
    
    const mockCreatedDepot = {
      id: 'depot-new',
      name: 'New Test Depot',
      location: '123 Test Street',
      routesCount: 0,
      vehiclesCount: 0,
      driversCount: 0,
      status: 'ACTIVE',
    };

    mockDepotService.createDepot.mockResolvedValue(mockCreatedDepot);
    mockRefreshDepots.mockResolvedValue(undefined);

    renderDepots();

    // Open dialog
    const addButton = screen.getByText('+ Add Depot');
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add Depot')).toBeInTheDocument();
    });

    // Fill in only name and address, leave coordinates empty
    const nameInput = screen.getByPlaceholderText('e.g. London Central');
    const addressInput = screen.getByPlaceholderText('e.g. Vauxhall, London');
    const createButton = screen.getByText('Create');

    await user.type(nameInput, 'New Test Depot');
    await user.type(addressInput, '123 Test Street');

    // Submit the form
    await user.click(createButton);

    // Verify that createDepot was called WITHOUT coordinate fields
    await waitFor(() => {
      expect(mockDepotService.createDepot).toHaveBeenCalledTimes(1);
      expect(mockDepotService.createDepot).toHaveBeenCalledWith({
        name: 'New Test Depot',
        address: '123 Test Street',
        // latitude and longitude should NOT be in the request
      });
      
      const callArgs = mockDepotService.createDepot.mock.calls[0][0];
      expect(callArgs).not.toHaveProperty('latitude');
      expect(callArgs).not.toHaveProperty('longitude');
    });
  });

  it('should handle API errors and display error message', async () => {
    const user = userEvent.setup();
    
    const errorMessage = 'Depot with name "New Test Depot" already exists';
    mockDepotService.createDepot.mockRejectedValue(new Error(errorMessage));

    renderDepots();

    // Open dialog
    const addButton = screen.getByText('+ Add Depot');
    await user.click(addButton);

    await waitFor(() => {
      expect(screen.getByText('Add Depot')).toBeInTheDocument();
    });

    // Fill in the form
    const nameInput = screen.getByPlaceholderText('e.g. London Central');
    const addressInput = screen.getByPlaceholderText('e.g. Vauxhall, London');
    const createButton = screen.getByText('Create');

    await user.type(nameInput, 'New Test Depot');
    await user.type(addressInput, '123 Test Street');

    // Submit the form
    await user.click(createButton);

    // Verify that createDepot was called
    await waitFor(() => {
      expect(mockDepotService.createDepot).toHaveBeenCalledTimes(1);
    });

    // Verify error message is displayed (check for the one in the dialog)
    await waitFor(() => {
      const errorElements = screen.getAllByText(errorMessage);
      expect(errorElements.length).toBeGreaterThan(0);
      // The error should be visible in the dialog
      expect(errorElements.some(el => el.closest('.bg-red-50'))).toBe(true);
    });

    // Verify that refreshDepots was NOT called on error
    expect(mockRefreshDepots).not.toHaveBeenCalled();
  });
});
