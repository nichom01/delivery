import { Link } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

export default function RoutesPage() {
  const { data, selectedDepotId } = useApp();
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const routes = data.routes.filter(r => r.depotId === selectedDepotId);
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Routes</div>
          <div className="text-[11.5px] text-gray-500">London Central · {routes.length} routes</div>
        </div>
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700">+ Add Route</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-blue-50 border border-blue-200 text-[12px] text-blue-800">
          Routes are permanently assigned to this depot. To move postcode coverage to another depot, reassign postcode rules via <Link to="/depot/postcodes" className="text-blue-600 hover:underline">Postcode Rules</Link>.
        </div>
        
        <Card>
          <CardHeader>
            <CardTitle className="text-[13.5px]">Routes — London Central</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Code
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Name
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Coverage
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Postcode Rules
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Status
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                </tr>
              </thead>
              <tbody>
                {routes.map((route) => (
                  <tr key={route.id} className="hover:bg-gray-50">
                    <td className="px-3.5 py-2 border-b border-gray-100">
                      <span className="inline-block px-1.5 py-0.5 rounded text-[10.5px] font-mono bg-gray-100 text-gray-600">
                        {route.code}
                      </span>
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-semibold">
                      {route.name}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                      {route.coverage}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                      {route.postcodeRulesCount} rules
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100">
                      <Badge variant="green">{route.status}</Badge>
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100">
                      <div className="flex items-center gap-1.5">
                        <Link to="/depot/postcodes" className="text-blue-600 hover:underline text-[12px]">
                          Postcodes
                        </Link>
                        <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">Edit</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
