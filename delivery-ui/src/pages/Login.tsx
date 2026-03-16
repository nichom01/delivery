import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';

export default function Login() {
  const [username, setUsername] = useState('depot1');
  const [password, setPassword] = useState('password');
  const [rememberMe, setRememberMe] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    
    try {
      await login(username, password);
      // Navigation is handled by useAuth hook
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-[var(--navy)] min-h-screen flex items-center justify-center w-full">
      <Card className="bg-white rounded-xl p-10 w-[380px] shadow-2xl">
        <div className="text-center mb-6">
          <div className="w-[46px] h-[46px] bg-[var(--navy)] rounded-[10px] inline-flex items-center justify-center text-[20px] font-bold text-white mb-2.5">
            D
          </div>
          <div className="text-[20px] font-bold text-gray-900">DeliverOps</div>
          <div className="text-[12px] text-gray-500 mt-0.5">Delivery Van Management System</div>
        </div>
        <form onSubmit={handleSubmit} className="flex flex-col gap-3.5">
          <div className="flex flex-col gap-1">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="text-[12.5px]"
              placeholder="admin, depot1, depot2, etc."
            />
          </div>
          <div className="flex flex-col gap-1">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="text-[12.5px]"
            />
          </div>
          <div className="flex items-center justify-between">
            <label className="flex items-center gap-1.5 font-normal text-[12px] cursor-pointer">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4"
              />
              Remember me
            </label>
            <span className="text-blue-600 cursor-pointer text-[12px] hover:underline">
              Forgot password?
            </span>
          </div>
          {error && (
            <div className="text-red-600 text-sm text-center">{error}</div>
          )}
          <Button
            type="submit"
            disabled={loading}
            className="w-full justify-center py-2.5 text-[14px] mt-0.5 bg-blue-600 hover:bg-blue-700"
          >
            {loading ? 'Signing in...' : 'Sign In →'}
          </Button>
          <div className="text-center text-[11px] text-gray-400">
            Contact your system administrator if you need access
          </div>
        </form>
      </Card>
    </div>
  );
}
