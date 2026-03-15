import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';

export default function Login() {
  const [email, setEmail] = useState('j.smith@depot-london.co.uk');
  const [password, setPassword] = useState('••••••••');
  const [rememberMe, setRememberMe] = useState(true);
  const navigate = useNavigate();
  const { setCurrentUser, data } = useApp();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Mock login - set user from data
    if (data) {
      setCurrentUser(data.currentUser);
      navigate('/dashboard');
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
            <Label htmlFor="email">Email Address</Label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="text-[12.5px]"
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
          <Button
            type="submit"
            className="w-full justify-center py-2.5 text-[14px] mt-0.5 bg-blue-600 hover:bg-blue-700"
          >
            Sign In →
          </Button>
          <div className="text-center text-[11px] text-gray-400">
            Contact your system administrator if you need access
          </div>
        </form>
      </Card>
    </div>
  );
}
