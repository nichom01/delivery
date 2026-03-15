import { useNavigate } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';

export default function Depots() {
  const { data, setSelectedDepotId } = useApp();
  const navigate = useNavigate();
  
  if (!data) {
    return <div>Loading...</div>;
  }
  
  const handleSwitchDepot = (depotId: string) => {
    setSelectedDepotId(depotId);
    navigate('/dashboard');
  };
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">All Depots</div>
          <div className="text-[11.5px] text-gray-500">Central Admin · {data.depots.length} depots across the network</div>
        </div>
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700">+ Add Depot</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-blue-50 border border-blue-200 text-[12px] text-blue-800">
          You are viewing all depots. To work within a specific depot (add orders, manage routes, vehicles etc.), select it from the <strong>Working Depot</strong> picker — visible on all depot-scoped screens.
        </div>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-[13.5px]">All Depots ({data.depots.length})</CardTitle>
            <Input placeholder="Search…" className="text-[12px] px-2.5 py-1 w-[180px]" />
          </CardHeader>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Depot Name
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Location
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Routes
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Vehicles
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Drivers
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Status
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                </tr>
              </thead>
              <tbody>
                {data.depots.map((depot) => (
                  <tr key={depot.id} className="hover:bg-gray-50">
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-semibold">
                      {depot.name}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                      {depot.location}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                      {depot.routesCount}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                      {depot.vehiclesCount}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                      {depot.driversCount}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100">
                      <Badge variant={depot.status === 'Active' ? 'green' : 'amber'}>
                        {depot.status}
                      </Badge>
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100">
                      <div className="flex items-center gap-1.5">
                        <span
                          className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                          onClick={() => handleSwitchDepot(depot.id)}
                        >
                          Switch to →
                        </span>
                        <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">Edit</span>
                      </div>
                    </td>
                  </tr>
                ))}
                {data.depots.length > 5 && (
                  <tr>
                    <td colSpan={7} className="px-3.5 py-2 text-center text-[11px] text-gray-500 bg-gray-50">
                      … {data.depots.length - 5} more depots …
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
