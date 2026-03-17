import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/api/services/authService';
import { storage } from '@/utils/storage';
import type { CurrentUserDto } from '@/api/types';

export function useAuth() {
  const [user, setUser] = useState<CurrentUserDto | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const token = storage.getToken();
    if (!token) {
      setLoading(false);
      return;
    }

    // Verify token by getting current user
    authService.getCurrentUser()
      .then(setUser)
      .catch((error) => {
        // Token invalid or API error, clear it
        console.error('Auth check failed:', error);
        storage.removeToken();
        setUser(null);
        // Don't navigate here - let the Router handle it based on isAuthenticated
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  const login = async (username: string, password: string): Promise<void> => {
    const response = await authService.login(username, password);
    storage.setToken(response.token);
    setUser(response.user);
    navigate('/dashboard');
  };

  const logout = () => {
    storage.removeToken();
    setUser(null);
    navigate('/login');
  };

  return {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user,
  };
}
