import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { depotService } from '@/api/services/depotService';
import { userService } from '@/api/services/userService';
import { vehicleService } from '@/api/services/vehicleService';
import { driverService } from '@/api/services/driverService';
import type { DepotDto, CurrentUserDto, UserDto, VehicleDto, DriverDto, RouteDto } from '@/api/types';

interface AppData {
  depots: DepotDto[];
  users: UserDto[];
  vehicles: VehicleDto[];
  drivers: DriverDto[];
  routes: RouteDto[];
}

interface AppContextValue {
  currentUser: CurrentUserDto | null;
  selectedDepotId: string | null;
  setSelectedDepotId: (depotId: string | null) => void;
  depots: DepotDto[];
  data: AppData | null;
  loading: boolean;
  getDepotById: (id: string) => DepotDto | undefined;
  refreshDepots: () => Promise<void>;
  refreshVehicles: () => Promise<void>;
  refreshDrivers: () => Promise<void>;
  logout: () => void;
}

const AppContext = createContext<AppContextValue | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const { user, loading: authLoading, logout } = useAuth();
  const [depots, setDepots] = useState<DepotDto[]>([]);
  const [users, setUsers] = useState<UserDto[]>([]);
  const [vehicles, setVehicles] = useState<VehicleDto[]>([]);
  const [drivers, setDrivers] = useState<DriverDto[]>([]);
  const [routes, setRoutes] = useState<RouteDto[]>([]);
  const [selectedDepotId, setSelectedDepotId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const loadDepots = async () => {
    try {
      const depotList = await depotService.getAllDepots();
      setDepots(depotList);
      
      // Initialize selected depot if not set
      if (!selectedDepotId) {
        if (user?.depotId) {
          setSelectedDepotId(user.depotId);
        } else if (depotList.length > 0) {
          setSelectedDepotId(depotList[0].id);
        }
      }
    } catch (error) {
      console.error('Failed to load depots:', error);
      // Don't throw - just log the error and continue
      setDepots([]);
    }
  };

  const loadUsers = async () => {
    try {
      const userList = await userService.getUsers();
      setUsers(userList);
    } catch (error) {
      console.error('Failed to load users:', error);
      // Don't throw - just log the error and continue
      setUsers([]);
    }
  };

  const loadVehicles = async () => {
    try {
      const vehicleList = await vehicleService.getVehicles();
      setVehicles(vehicleList);
    } catch (error) {
      console.error('Failed to load vehicles:', error);
      // Don't throw - just log the error and continue
      setVehicles([]);
    }
  };

  const loadDrivers = async () => {
    try {
      const driverList = await driverService.getDrivers();
      setDrivers(driverList);
    } catch (error) {
      console.error('Failed to load drivers:', error);
      // Don't throw - just log the error and continue
      setDrivers([]);
    }
  };

  const loadRoutes = async () => {
    try {
      // Load routes for all depots - we'll filter by depotId in components
      const allRoutes: RouteDto[] = [];
      const depotList = await depotService.getAllDepots();
      
      for (const depot of depotList) {
        try {
          const depotRoutes = await depotService.getRoutesByDepot(depot.id);
          allRoutes.push(...depotRoutes);
        } catch (error) {
          console.error(`Failed to load routes for depot ${depot.id}:`, error);
        }
      }
      
      setRoutes(allRoutes);
    } catch (error) {
      console.error('Failed to load routes:', error);
      // Don't throw - just log the error and continue
      setRoutes([]);
    }
  };

  useEffect(() => {
    if (authLoading) {
      // Still loading auth, wait
      return;
    }
    
    if (user) {
      // User is authenticated, load all data
      Promise.all([
        loadDepots(),
        loadUsers(),
        loadVehicles(),
        loadDrivers(),
        loadRoutes()
      ]).finally(() => setLoading(false));
    } else {
      // No user, just set loading to false
      setLoading(false);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [authLoading, user]);

  const getDepotById = (id: string): DepotDto | undefined => {
    return depots.find(d => d.id === id);
  };

  const refreshDepots = async () => {
    await loadDepots();
    // Also refresh users in case they were affected
    await loadUsers();
  };

  const refreshVehicles = async () => {
    await loadVehicles();
  };

  const refreshDrivers = async () => {
    await loadDrivers();
  };

  // Provide data object once loading is complete (even if arrays are empty)
  const data: AppData | null = !loading && !authLoading
    ? { depots, users, vehicles, drivers, routes }
    : null;

  return (
    <AppContext.Provider
      value={{
        currentUser: user,
        selectedDepotId,
        setSelectedDepotId,
        depots,
        data,
        loading: loading || authLoading,
        getDepotById,
        refreshDepots,
        refreshVehicles,
        refreshDrivers,
        logout,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useApp() {
  const context = useContext(AppContext);
  if (context === undefined) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
}
