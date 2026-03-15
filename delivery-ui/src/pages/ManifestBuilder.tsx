import { useParams } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';

export default function ManifestBuilder() {
  const { manifestId } = useParams();
  const { data, selectedDepotId } = useApp();
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const manifest = manifestId
    ? data.manifests.find(m => m.id === manifestId)
    : data.manifests[0];
  
  const route = manifest ? data.routes.find(r => r.id === manifest.routeId) : null;
  const driver = manifest ? data.drivers.find(d => d.id === manifest.driverId) : null;
  const vehicle = manifest ? data.vehicles.find(v => v.id === manifest.vehicleId) : null;
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">
            Manifest Builder — {route?.name || 'Route F'}
          </div>
          <div className="text-[11.5px] text-gray-500">
            London Central · {manifest?.date || '12 Mar 2026'} · Draft
          </div>
        </div>
        <Button variant="outline" size="sm">Save Draft</Button>
        <Button size="sm" className="bg-green-600 hover:bg-green-700">✓ Confirm Manifest</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-amber-50 border border-amber-200 text-[12px] text-amber-800">
          ⚠ 2 orders have outstanding boxes not yet arrived. Available boxes will be manifested; missing boxes scheduled to next run automatically.
        </div>
        
        <div className="grid grid-cols-2 gap-4 items-start">
          <div className="space-y-4">
            {/* Manifest Details */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">Manifest Details</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-3.5">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Route</Label>
                    <Input value={route?.name || ''} disabled />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Delivery Date</Label>
                    <Input type="date" defaultValue={manifest?.date || '2026-03-12'} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Driver *</Label>
                    <select className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]">
                      {data.drivers.map(d => (
                        <option key={d.id} value={d.id} selected={d.id === manifest?.driverId}>
                          {d.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Vehicle *</Label>
                    <select className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]">
                      {data.vehicles.map(v => (
                        <option key={v.id} value={v.id} selected={v.id === manifest?.vehicleId}>
                          {v.registration} — {v.makeModel}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
          
          {/* Delivery Stops */}
          <div>
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">
                  Delivery Stops ({manifest?.stops.length || 18})
                </CardTitle>
                <div className="text-[11px] text-gray-500">94 boxes to load</div>
              </CardHeader>
              <CardContent className="p-0">
                <table className="w-full border-collapse">
                  <thead>
                    <tr>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Order
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Address
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Boxes
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Status
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {manifest?.stops.map((stop) => {
                      const isPartial = typeof stop.boxes === 'string' && stop.boxes.includes('of');
                      return (
                        <tr
                          key={stop.orderId}
                          className={`hover:bg-gray-50 ${isPartial ? 'bg-amber-50' : ''}`}
                        >
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono">
                            {stop.orderId}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                            {stop.address}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                            {stop.boxes}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100">
                            <Badge variant={stop.boxStatus.includes('Part') ? 'amber' : 'green'}>
                              {stop.boxStatus}
                            </Badge>
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100">
                            <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">Remove</span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </>
  );
}
