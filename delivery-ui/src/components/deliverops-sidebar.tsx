import { NavLink, useLocation } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { Avatar, AvatarFallback } from './ui/avatar';
import {
  LayoutDashboard,
  CalendarDays,
  Package,
  Search,
  FileText,
  Map,
  MapPin,
  Mail,
  Truck,
  User,
  Building2,
  Users,
  FileSearch,
  LogOut,
} from 'lucide-react';
import { cn } from '@/lib/utils';

const depotMenuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard', scope: 'depot' },
  { icon: CalendarDays, label: 'Day Plan', path: '/day-plan', scope: 'depot' },
  { icon: Package, label: 'Order Entry', path: '/orders/new', scope: 'depot' },
  { icon: Search, label: 'Goods Receiving', path: '/receiving', scope: 'depot' },
  { icon: FileText, label: 'Manifests', path: '/manifests', scope: 'depot' },
  { icon: Map, label: 'Routes', path: '/depot/routes', scope: 'depot' },
  { icon: Mail, label: 'Postcode Rules', path: '/depot/postcodes', scope: 'depot' },
  { icon: Truck, label: 'Vehicles', path: '/depot/vehicles', scope: 'depot' },
  { icon: User, label: 'Drivers', path: '/depot/drivers', scope: 'depot' },
  { icon: MapPin, label: 'Driver locations', path: '/depot/driver-locations', scope: 'depot' },
];

const systemMenuItems = [
  { icon: Building2, label: 'All Depots', path: '/depots', scope: 'global' },
  { icon: Users, label: 'Users', path: '/users', scope: 'global' },
  { icon: FileSearch, label: 'Audit Log', path: '/depot/audit', scope: 'depot' },
];

export function DeliverOpsSidebar() {
  const { currentUser, logout } = useApp();
  const location = useLocation();
  const isAdmin = currentUser?.role === 'CENTRAL_ADMIN';
  
  if (!currentUser) return null;
  
  const avatarClass = currentUser.role === 'CENTRAL_ADMIN' ? 'bg-purple-600' : 'bg-blue-600';
  
  return (
    <div className="w-[224px] bg-[var(--navy)] text-white flex flex-col shrink-0 sticky top-[48px] h-[calc(100vh-48px)] overflow-y-auto">
      {/* My Depot Section */}
      <div className="px-3.5 pt-3.5 pb-1.5">
        <div className="text-[9.5px] text-cyan-400 font-bold uppercase tracking-wide flex items-center gap-1.5">
          <span>My Depot</span>
          <div className="flex-1 h-px bg-white/7"></div>
        </div>
      </div>
      
      {depotMenuItems.map((item) => {
        const Icon = item.icon;
        const isActive = location.pathname === item.path || 
          (item.path === '/dashboard' && location.pathname === '/');
        return (
          <NavLink
            key={item.path}
            to={item.path}
            className={cn(
              'flex items-center gap-2 px-3.5 py-1.5 mx-2 rounded-md cursor-pointer transition-all text-[12.5px] text-gray-400',
              isActive
                ? 'bg-blue-600 text-white'
                : 'hover:bg-white/7 hover:text-gray-300'
            )}
          >
            <Icon className="w-[17px] text-center shrink-0" />
            <span>{item.label}</span>
            <span className="ml-auto text-[9px] px-1.5 py-0.5 rounded-full font-bold tracking-wide bg-cyan-500/18 text-cyan-400 shrink-0">
              depot
            </span>
          </NavLink>
        );
      })}
      
      {/* System Section */}
      {isAdmin && (
        <>
          <div className="px-3.5 pt-3.5 pb-1.5 mt-2">
            <div className="text-[9.5px] text-purple-400 font-bold uppercase tracking-wide flex items-center gap-1.5">
              <span>System</span>
              <div className="flex-1 h-px bg-white/7"></div>
            </div>
          </div>
          
          {systemMenuItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={cn(
                  'flex items-center gap-2 px-3.5 py-1.5 mx-2 rounded-md cursor-pointer transition-all text-[12.5px] text-gray-400',
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'hover:bg-white/7 hover:text-gray-300'
                )}
              >
                <Icon className="w-[17px] text-center shrink-0" />
                <span>{item.label}</span>
                {item.scope === 'global' && (
                  <span className="ml-auto text-[9px] px-1.5 py-0.5 rounded-full font-bold tracking-wide bg-purple-500/18 text-purple-400 shrink-0">
                    global
                  </span>
                )}
              </NavLink>
            );
          })}
        </>
      )}
      
      {/* Sidebar User */}
      <div className="mt-auto border-t border-white/8">
        <div className="px-3.5 py-3 flex items-center gap-2">
          <Avatar className={`w-7 h-7 ${avatarClass}`}>
            <AvatarFallback className={`text-[11px] font-bold text-white ${avatarClass}`}>
              {currentUser.initials}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1">
            <div className="text-[12px] text-white font-medium">{currentUser.name}</div>
            <div className="text-[10px] text-gray-400">
              {currentUser.role === 'CENTRAL_ADMIN' ? 'Central Admin' : 'Depot Manager'}
            </div>
          </div>
        </div>
        <button
          onClick={logout}
          className="w-full px-3.5 py-2 mx-2 mb-2 rounded-md flex items-center gap-2 text-[12.5px] text-gray-400 hover:bg-white/7 hover:text-gray-300 transition-all"
        >
          <LogOut className="w-[17px] text-center shrink-0" />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}
