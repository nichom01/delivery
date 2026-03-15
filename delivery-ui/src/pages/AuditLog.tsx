import { useState } from 'react';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function AuditLog() {
  const { data, selectedDepotId } = useApp();
  const [startDate, setStartDate] = useState('2026-03-01');
  const [endDate, setEndDate] = useState('2026-03-12');
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const events = data.auditEvents;
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Audit Log</div>
          <div className="text-[11.5px] text-gray-500">London Central · Read-only record of all changes</div>
        </div>
        <Button variant="outline" size="sm">⬇ Export CSV</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        {/* Filters */}
        <Card className="mb-4">
          <CardHeader>
            <CardTitle className="text-[13.5px]">Filters</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 gap-3.5">
              <div className="flex flex-col gap-1">
                <Label className="text-[11px]">Date Range</Label>
                <div className="flex items-center gap-1">
                  <Input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} className="flex-1" />
                  <span className="text-gray-500">–</span>
                  <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} className="flex-1" />
                </div>
              </div>
              <div className="flex flex-col gap-1">
                <Label className="text-[11px]">Entity Type</Label>
                <select className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]">
                  <option>All types</option>
                  <option>Postcode Rule</option>
                  <option>Route</option>
                  <option>Vehicle</option>
                  <option>Driver</option>
                  <option>Manifest</option>
                  <option>Order</option>
                </select>
              </div>
              <div className="flex flex-col gap-1">
                <Label className="text-[11px]">User</Label>
                <select className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]">
                  <option>All users</option>
                  {Array.from(new Set(events.map(e => e.userName))).map(name => (
                    <option key={name}>{name}</option>
                  ))}
                </select>
              </div>
            </div>
          </CardContent>
        </Card>
        
        {/* Audit Events */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-[13.5px]">Audit Events</CardTitle>
            <span className="text-[11px] text-gray-500">{events.length} events in range</span>
          </CardHeader>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Timestamp
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    User
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Role
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Action
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Entity
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Detail
                  </th>
                </tr>
              </thead>
              <tbody>
                {events.map((event, idx) => {
                  const actionBadgeVariant =
                    event.action === 'CREATE' ? 'green' :
                    event.action === 'UPDATE' ? 'blue' : 'red';
                  const roleBadgeVariant =
                    event.role === 'Central Admin' ? 'purple' :
                    event.role === 'Depot Mgr' ? 'blue' : 'grey';
                  return (
                    <tr key={idx} className="hover:bg-gray-50">
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono text-[11px]">
                        {event.timestamp}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {event.userName}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <Badge variant={roleBadgeVariant as any}>
                          {event.role}
                        </Badge>
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <Badge variant={actionBadgeVariant as any}>
                          {event.action}
                        </Badge>
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {event.entityType}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {event.detail}
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
