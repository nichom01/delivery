import { useApp } from '@/contexts/AppContext';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from './ui/dropdown-menu';

export function DepotPicker() {
  const { currentUser, selectedDepotId, setSelectedDepotId, data, getDepotById } = useApp();
  
  if (!currentUser || !data) return null;
  
  const isLocked = currentUser.role === 'DEPOT_MANAGER';
  const selectedDepot = selectedDepotId ? getDepotById(selectedDepotId) : null;
  
  if (isLocked && selectedDepot) {
    return (
      <div className="flex items-center gap-2.5 shrink-0">
        <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wide">
          Working Depot
        </span>
        <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-md border-1.5 border-gray-300 bg-gray-50 cursor-default min-w-[200px]">
          <div className="w-1.5 h-1.5 rounded-full bg-green-600 shrink-0"></div>
          <span className="text-[13px] font-semibold text-gray-700">{selectedDepot.name}</span>
          <span className="ml-auto text-gray-400 text-[11px]">🔒</span>
        </div>
      </div>
    );
  }
  
  return (
    <div className="flex items-center gap-2.5 shrink-0">
      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wide">
        Working Depot
      </span>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-md border-1.5 border-blue-600 bg-blue-50 cursor-pointer min-w-[200px] hover:bg-blue-100 transition-colors">
            <div className="w-1.5 h-1.5 rounded-full bg-green-600 shrink-0"></div>
            <span className="text-[13px] font-semibold text-[var(--navy)]">
              {selectedDepot?.name || 'Select depot'}
            </span>
            <span className="ml-auto text-blue-600 text-[11px]">▾</span>
          </div>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start" className="min-w-[200px]">
          {data.depots.map((depot) => (
            <DropdownMenuItem
              key={depot.id}
              onClick={() => setSelectedDepotId(depot.id)}
              className={selectedDepotId === depot.id ? 'bg-blue-50' : ''}
            >
              <div className="flex items-center gap-2 w-full">
                <div className="w-1.5 h-1.5 rounded-full bg-green-600"></div>
                <span className="font-semibold">{depot.name}</span>
              </div>
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}
