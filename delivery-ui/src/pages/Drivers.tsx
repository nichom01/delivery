import { useApp } from '@/contexts/AppContext';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

export default function Drivers() {
  const { data, selectedDepotId } = useApp();
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const drivers = data.drivers.filter(d => d.depotId === selectedDepotId);
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Driver Management</div>
          <div className="text-[11.5px] text-gray-500">London Central · {drivers.length} drivers</div>
        </div>
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700">+ Add Driver</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Name
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Licence No.
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Expiry
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Contact
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Today's Route
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Status
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                </tr>
              </thead>
              <tbody>
                {drivers.map((driver) => {
                  const route = driver.todaysRouteId
                    ? data.routes.find(r => r.id === driver.todaysRouteId)
                    : null;
                  return (
                    <tr key={driver.id} className="hover:bg-gray-50">
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-semibold">
                        {driver.name}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono">
                        {driver.licenceNo}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {driver.expiry}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {driver.contact}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {route?.name || '—'}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <Badge variant={driver.status === 'Active' ? 'green' : 'grey'}>
                          {driver.status}
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
