import { useState, useEffect } from 'react';
import { useApp } from '@/contexts/AppContext';
import { auditService } from '@/api/services/auditService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { AuditEventDto } from '@/api/types';

export default function AuditLog() {
  const { selectedDepotId, getDepotById } = useApp();
  const [events, setEvents] = useState<AuditEventDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [startDate, setStartDate] = useState('2026-03-01');
  const [endDate, setEndDate] = useState('2026-03-12');
  
  // Filter state
  const [filterEntityType, setFilterEntityType] = useState<string>('');
  const [filterUserName, setFilterUserName] = useState<string>('');
  
  useEffect(() => {
    if (!selectedDepotId) {
      setLoading(false);
      return;
    }

    const loadAuditEvents = async () => {
      try {
        setLoading(true);
        setError(null);
        const auditEvents = await auditService.getAuditEvents(selectedDepotId);
        setEvents(auditEvents);
      } catch (err) {
        console.error('Failed to load audit events:', err);
        setError(err instanceof Error ? err.message : 'Failed to load audit events');
      } finally {
        setLoading(false);
      }
    };

    loadAuditEvents();
  }, [selectedDepotId]);
  
  if (loading) {
    return <div className="p-5">Loading...</div>;
  }

  if (error) {
    return <div className="p-5 text-red-600">Error: {error}</div>;
  }

  if (!selectedDepotId) {
    return <div className="p-5">Please select a depot</div>;
  }
  
  const depot = getDepotById(selectedDepotId);
  
  // Filter events based on selected filters
  const filteredEvents = events.filter(event => {
    // Date range filter
    if (startDate || endDate) {
      const eventDate = new Date(event.timestamp);
      const start = startDate ? new Date(startDate) : null;
      const end = endDate ? new Date(endDate) : null;
      
      if (start && eventDate < start) {
        return false;
      }
      if (end) {
        // Include the entire end date (set to end of day)
        const endOfDay = new Date(end);
        endOfDay.setHours(23, 59, 59, 999);
        if (eventDate > endOfDay) {
          return false;
        }
      }
    }
    
    // Entity type filter
    if (filterEntityType && event.entityType !== filterEntityType) {
      return false;
    }
    
    // User filter
    if (filterUserName && event.userName !== filterUserName) {
      return false;
    }
    
    return true;
  });
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Audit Log</div>
          <div className="text-[11.5px] text-gray-500">{depot?.name || 'Depot'} · Read-only record of all changes</div>
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
                <select 
                  className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]"
                  value={filterEntityType}
                  onChange={(e) => setFilterEntityType(e.target.value)}
                >
                  <option value="">All types</option>
                  {Array.from(new Set(events.map(e => e.entityType))).sort().map(type => (
                    <option key={type} value={type}>{type}</option>
                  ))}
                </select>
              </div>
              <div className="flex flex-col gap-1">
                <Label className="text-[11px]">User</Label>
                <select 
                  className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]"
                  value={filterUserName}
                  onChange={(e) => setFilterUserName(e.target.value)}
                >
                  <option value="">All users</option>
                  {Array.from(new Set(events.map(e => e.userName))).sort().map(name => (
                    <option key={name} value={name}>{name}</option>
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
            <span className="text-[11px] text-gray-500">{filteredEvents.length} of {events.length} events</span>
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
                {filteredEvents.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-3.5 py-4 text-center text-[12px] text-gray-500">
                      {events.length === 0 ? 'No audit events found' : 'No events match the selected filters'}
                    </td>
                  </tr>
                ) : (
                  filteredEvents.map((event, idx) => {
                    const actionBadgeVariant =
                      event.action === 'CREATE' ? 'green' :
                      event.action === 'UPDATE' ? 'blue' : 'red';
                    const roleBadgeVariant =
                      event.role === 'CENTRAL_ADMIN' ? 'purple' :
                      event.role === 'DEPOT_MANAGER' ? 'blue' : 'grey';
                    const roleDisplayName = 
                      event.role === 'CENTRAL_ADMIN' ? 'Central Admin' :
                      event.role === 'DEPOT_MANAGER' ? 'Depot Manager' : event.role;
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
                            {roleDisplayName}
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
                  })
                )}
              </tbody>
            </table>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
