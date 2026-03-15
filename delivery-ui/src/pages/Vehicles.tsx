import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

export default function Vehicles() {
  const { data, selectedDepotId } = useApp();
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const vehicles = data.vehicles.filter(v => v.depotId === selectedDepotId);
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Vehicle Fleet</div>
          <div className="text-[11.5px] text-gray-500">London Central · {vehicles.length} vehicles</div>
        </div>
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700">+ Add Vehicle</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-amber-50 border border-amber-200 text-[12px] text-amber-800">
          ⚠ 1 vehicle has MOT due within 30 days — <span className="text-blue-600 hover:underline cursor-pointer">VW21 MNO</span>
        </div>
        
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Registration
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Make / Model
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Capacity
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    MOT Due
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Next Service
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Status
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                </tr>
              </thead>
              <tbody>
                {vehicles.map((vehicle) => {
                  const motDueSoon = vehicle.status === 'MOT Due Soon';
                  return (
                    <tr key={vehicle.id} className={`hover:bg-gray-50 ${motDueSoon ? 'bg-amber-50' : ''}`}>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono font-semibold">
                        {vehicle.registration}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {vehicle.makeModel}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {vehicle.capacity}
                      </td>
                      <td className={`px-3.5 py-2 border-b border-gray-100 text-[12.5px] ${motDueSoon ? 'text-amber-600 font-semibold' : 'text-gray-700'}`}>
                        {vehicle.motDue} {motDueSoon && '⚠'}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {vehicle.nextService}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <Badge variant={motDueSoon ? 'amber' : 'green'}>
                          {vehicle.status}
                        </Badge>
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">Edit</span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
