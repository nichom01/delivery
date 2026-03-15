import { useLocation, Link } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { DepotPicker } from './depot-picker';
import { Avatar, AvatarFallback } from './ui/avatar';

function getBreadcrumbs(pathname: string): Array<{ label: string; path?: string }> {
  const parts = pathname.split('/').filter(Boolean);
  const crumbs: Array<{ label: string; path?: string }> = [];
  
  if (parts.length === 0) {
    return [{ label: 'Dashboard' }];
  }
  
  const routeMap: Record<string, string> = {
    dashboard: 'Dashboard',
    'orders': 'Order Entry',
    'receiving': 'Goods Receiving',
    'manifests': 'Manifests',
    'routes': 'Routes',
    'postcodes': 'Postcode Rules',
    'vehicles': 'Vehicles',
    'drivers': 'Drivers',
    'depots': 'All Depots',
    'users': 'User Management',
    'audit': 'Audit Log',
  };
  
  let currentPath = '';
  parts.forEach((part, index) => {
    currentPath += `/${part}`;
    const label = routeMap[part] || part;
    if (index === parts.length - 1) {
      crumbs.push({ label });
    } else {
      crumbs.push({ label, path: currentPath });
    }
  });
  
  return crumbs;
}

export function ContextBar() {
  const location = useLocation();
  const { currentUser, data } = useApp();
  const isGlobalRoute = location.pathname === '/depots' || location.pathname === '/users';
  const breadcrumbs = getBreadcrumbs(location.pathname);
  
  if (!currentUser || !data) return null;
  
  const roleLabel = currentUser.role === 'central_admin' ? 'Central Admin' : 'Depot Manager';
  const avatarClass = currentUser.role === 'central_admin' ? 'bg-purple-600' : 'bg-blue-600';
  
  return (
    <div className="h-[48px] bg-white border-b-2 border-gray-200 flex items-center px-5 gap-0 sticky top-0 z-100 shadow-sm">
      {/* Logo */}
      <div className="flex items-center gap-2 pr-5 border-r border-gray-200 mr-5 shrink-0">
        <div className="w-[26px] h-[26px] bg-[var(--navy)] rounded-md flex items-center justify-center font-bold text-[13px] text-white">
          D
        </div>
        <div className="font-bold text-[14px] text-gray-900 tracking-tight">DeliverOps</div>
      </div>
      
      {/* Depot Picker or Global Label */}
      {isGlobalRoute ? (
        <div className="flex items-center gap-2 px-3 py-1 bg-purple-50 rounded-md border-1.5 border-purple-300">
          <span className="text-[11px] font-bold text-purple-700 uppercase tracking-wide">
            🌐 Global — All Depots
          </span>
        </div>
      ) : (
        <DepotPicker />
      )}
      
      {/* Breadcrumb */}
      <div className="flex items-center gap-1.5 ml-4 text-[12px] text-gray-500 flex-1">
        {breadcrumbs.map((crumb, index) => (
          <div key={index} className="flex items-center gap-1.5">
            {index > 0 && <span className="text-gray-300">›</span>}
            {crumb.path ? (
              <Link to={crumb.path} className="text-blue-600 hover:underline">
                {crumb.label}
              </Link>
            ) : (
              <span className="text-gray-800 font-semibold">{crumb.label}</span>
            )}
          </div>
        ))}
      </div>
      
      {/* User */}
      <div className="ml-auto flex items-center gap-2.5 shrink-0">
        <div className="text-right">
          <div className="text-[12.5px] font-semibold text-gray-800">{currentUser.name}</div>
          <div className="text-[10px] text-gray-500">{roleLabel}</div>
        </div>
        <Avatar className={`w-7 h-7 ${avatarClass}`}>
          <AvatarFallback className={`text-[11px] font-bold text-white ${avatarClass}`}>
            {currentUser.initials}
          </AvatarFallback>
        </Avatar>
      </div>
    </div>
  );
}
