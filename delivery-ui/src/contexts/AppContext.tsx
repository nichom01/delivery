import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { getMockData } from '@/api/client';
import type { MockApiPayload, CurrentUser, Depot } from '@/api/types';

interface AppContextValue {
  data: MockApiPayload | null;
  currentUser: CurrentUser | null;
  selectedDepotId: string | null;
  setSelectedDepotId: (depotId: string | null) => void;
  setCurrentUser: (user: CurrentUser | null) => void;
  loading: boolean;
  getDepotById: (id: string) => Depot | undefined;
}

const AppContext = createContext<AppContextValue | undefined>(undefined);

export function AppProvider({ children }: { children: ReactNode }) {
  const [data, setData] = useState<MockApiPayload | null>(null);
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [selectedDepotId, setSelectedDepotId] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const payload = await getMockData();
        setData(payload);
        // Initialize with first depot if user has depotId, otherwise use first depot
        if (payload.currentUser.depotId) {
          setSelectedDepotId(payload.currentUser.depotId);
        } else if (payload.depots.length > 0) {
          setSelectedDepotId(payload.depots[0].id);
        }
        setCurrentUser(payload.currentUser);
      } catch (error) {
        console.error('Failed to load mock data:', error);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  const getDepotById = (id: string): Depot | undefined => {
    return data?.depots.find(d => d.id === id);
  };

  return (
    <AppContext.Provider
      value={{
        data,
        currentUser,
        selectedDepotId,
        setSelectedDepotId,
        setCurrentUser,
        loading,
        getDepotById,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const context = useContext(AppContext);
  if (context === undefined) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
}
