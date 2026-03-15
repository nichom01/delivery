import { useParams, Link } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';

function StatCard({ label, value, sub, color }: { label: string; value: string | number; sub?: string; color: 'blue' | 'green' | 'amber' | 'red' }) {
  return (
    <div className={`stat-card ${color}`}>
      <div className="text-[10.5px] text-gray-500 uppercase tracking-wide font-semibold mb-1">
        {label}
      </div>
      <div className="text-[22px] font-bold text-gray-900 leading-none">{value}</div>
      {sub && <div className="text-[11px] text-gray-500 mt-1">{sub}</div>}
    </div>
  );
}

export default function RouteDrilldown() {
  const { routeId } = useParams();
  const { data, selectedDepotId, getDepotById } = useApp();
  
  if (!data || !routeId || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const depot = getDepotById(selectedDepotId);
  const routeData = data.routeDrilldown[routeId];
  
  if (!routeData) {
    return <div>Route not found</div>;
  }
  
  const route = data.routes.find(r => r.id === routeId);
  const today = new Date(data.dashboard.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
  
  const delPct = routeData.stats.deliveriesTotal > 0
    ? Math.round((routeData.stats.deliveriesDone / routeData.stats.deliveriesTotal) * 100)
    : 0;
  const boxPct = routeData.stats.boxesTotal > 0
    ? Math.round((routeData.stats.boxesDone / routeData.stats.boxesTotal) * 100)
    : 0;
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">{routeData.routeName} — Delivery Detail</div>
          <div className="text-[11.5px] text-gray-500">
            {depot?.name} · {today} · {routeData.driver} · {routeData.vehicle}
          </div>
        </div>
        <Button variant="outline" size="sm" asChild>
          <Link to="/dashboard">← Back to Dashboard</Link>
        </Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        {/* Stats */}
        <div className="grid grid-cols-4 gap-3.5 mb-5">
          <StatCard
            label="Deliveries"
            value={`${routeData.stats.deliveriesDone}/${routeData.stats.deliveriesTotal}`}
            sub={`${delPct}% complete`}
            color="blue"
          />
          <StatCard
            label="Boxes Delivered"
            value={`${routeData.stats.boxesDone}/${routeData.stats.boxesTotal}`}
            sub={`${boxPct}% complete`}
            color="green"
          />
          <StatCard
            label="Exceptions"
            value={routeData.stats.exceptionsCount}
            sub="Missing box"
            color="amber"
          />
          <StatCard
            label="Last Activity"
            value={routeData.stats.lastActivity || '—'}
            sub={routeData.stats.lastActivityPostcode || ''}
            color="blue"
          />
        </div>
        
        {/* Delivery Stops */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between gap-3">
            <CardTitle className="text-[13.5px] font-semibold text-gray-800">Delivery Stops</CardTitle>
            <div className="flex items-center gap-2">
              <Input placeholder="Search…" className="text-[12px] px-2.5 py-1 w-[180px]" />
              <select className="text-[12px] px-2 py-1 border border-gray-300 rounded">
                <option>All statuses</option>
                <option>Delivered</option>
                <option>Pending</option>
              </select>
            </div>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      #
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Address
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Postcode
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Boxes
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Status
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Delivery Time
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      POD
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {routeData.stops.map((stop) => {
                    const statusColor =
                      stop.status === 'Delivered' ? 'green' :
                      stop.status === 'Part Delivered' ? 'amber' : 'grey';
                    return (
                      <tr key={stop.seq} className={`hover:bg-gray-50 ${stop.status === 'Part Delivered' ? 'bg-amber-50' : ''}`}>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-500">
                          {stop.seq}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {stop.address}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono">
                          {stop.postcode}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {stop.boxes}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <Badge variant={statusColor as any}>
                            {stop.status}
                          </Badge>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {stop.deliveryTime || '—'}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          {stop.hasPod ? (
                            <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">📷 View</span>
                          ) : (
                            '—'
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
