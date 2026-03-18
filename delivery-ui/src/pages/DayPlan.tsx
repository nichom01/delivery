import { useState, useEffect } from 'react';
import { useApp } from '@/contexts/AppContext';
import { depotService } from '@/api/services/depotService';
import { orderService } from '@/api/services/orderService';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { DayPlanDto, DayPlanRouteDto, DayPlanOrderDto, RouteDto } from '@/api/types';

// ─── Manifest status helpers ──────────────────────────────────────────────────

function manifestBadgeColor(status: string) {
  switch (status) {
    case 'CONFIRMED':
    case 'IN_PROGRESS': return 'blue';
    case 'COMPLETE': return 'green';
    case 'DRAFT': return 'amber';
    default: return 'grey';
  }
}

function manifestLabel(status: string) {
  switch (status) {
    case 'NONE': return 'No manifest';
    case 'DRAFT': return 'Draft';
    case 'CONFIRMED': return 'Confirmed';
    case 'IN_PROGRESS': return 'In Progress';
    case 'COMPLETE': return 'Complete';
    default: return status;
  }
}

// ─── Goods-in progress bar ────────────────────────────────────────────────────

function GoodsInBar({ received, total }: { received: number; total: number }) {
  const pct = total > 0 ? Math.round((received / total) * 100) : 0;
  const color = pct === 100 ? 'bg-green-500' : pct === 0 ? 'bg-red-400' : 'bg-amber-400';
  return (
    <div className="flex items-center gap-2 min-w-[100px]">
      <div className="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden">
        <div className={`h-full rounded-full ${color}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-[11px] text-gray-500 whitespace-nowrap">{received}/{total}</span>
    </div>
  );
}

// ─── Reroute modal ────────────────────────────────────────────────────────────

interface RerouteModalProps {
  order: DayPlanOrderDto;
  currentRouteId: string;
  depotRoutes: RouteDto[];
  onClose: () => void;
  onSuccess: () => void;
}

function RerouteModal({ order, currentRouteId, depotRoutes, onClose, onSuccess }: RerouteModalProps) {
  const [targetRouteId, setTargetRouteId] = useState('');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const otherRoutes = depotRoutes.filter(r => r.id !== currentRouteId);

  const handleSubmit = async () => {
    if (!targetRouteId) { setError('Please select a destination route'); return; }
    if (!reason.trim()) { setError('Please provide a reason'); return; }

    try {
      setSubmitting(true);
      setError(null);
      await orderService.rerouteOrder(order.id, { routeId: targetRouteId, reason: reason.trim() });
      onSuccess();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to re-route order');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-5">
        <div className="text-[15px] font-bold text-gray-900 mb-1">Move Order to Different Route</div>
        <div className="text-[12px] text-gray-500 mb-4">
          Order <span className="font-mono font-semibold">{order.orderId}</span> · {order.customerAddress || order.deliveryPostcode}
        </div>

        <div className="space-y-3">
          <div>
            <Label className="text-[11px] mb-1 block">Destination Route</Label>
            <select
              value={targetRouteId}
              onChange={e => { setTargetRouteId(e.target.value); setError(null); }}
              className="w-full border border-gray-300 rounded-md px-3 py-2 text-[13px] focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select a route…</option>
              {otherRoutes.map(r => (
                <option key={r.id} value={r.id}>{r.code} – {r.name}</option>
              ))}
            </select>
          </div>

          <div>
            <Label className="text-[11px] mb-1 block">Reason for Move</Label>
            <Input
              value={reason}
              onChange={e => { setReason(e.target.value); setError(null); }}
              placeholder="e.g. Route A overloaded, balancing workload"
              className="text-[13px]"
            />
          </div>

          {error && (
            <div className="text-[12px] text-red-700 bg-red-50 border border-red-200 rounded px-3 py-2">
              {error}
            </div>
          )}
        </div>

        <div className="flex gap-2 mt-5">
          <Button variant="outline" size="sm" className="flex-1" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button
            size="sm"
            className="flex-1 bg-blue-600 hover:bg-blue-700"
            onClick={handleSubmit}
            disabled={submitting || !targetRouteId || !reason.trim()}
          >
            {submitting ? 'Moving…' : 'Confirm Move'}
          </Button>
        </div>
      </div>
    </div>
  );
}

// ─── Route section ────────────────────────────────────────────────────────────

interface RouteSectionProps {
  route: DayPlanRouteDto;
  depotRoutes: RouteDto[];
  onRerouteSuccess: () => void;
}

function RouteSection({ route, depotRoutes, onRerouteSuccess }: RouteSectionProps) {
  const [expanded, setExpanded] = useState(true);
  const [rerouteOrder, setRerouteOrder] = useState<DayPlanOrderDto | null>(null);

  const receivedBoxes = route.orders.reduce((sum, o) => sum + o.boxesReceived + o.boxesReady, 0);
  const goodsInPct = route.totalBoxes > 0 ? Math.round((receivedBoxes / route.totalBoxes) * 100) : 0;

  return (
    <>
      {rerouteOrder && (
        <RerouteModal
          order={rerouteOrder}
          currentRouteId={route.routeId}
          depotRoutes={depotRoutes}
          onClose={() => setRerouteOrder(null)}
          onSuccess={() => { setRerouteOrder(null); onRerouteSuccess(); }}
        />
      )}

      <Card className="mb-3">
        {/* Route header */}
        <div
          className="flex items-center gap-3 px-4 py-3 cursor-pointer select-none hover:bg-gray-50 rounded-t-lg"
          onClick={() => setExpanded(e => !e)}
        >
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-[13.5px] font-bold text-gray-900">{route.routeName}</span>
              <span className="text-[11px] font-mono text-gray-400">{route.routeCode}</span>
              <Badge className={`badge-${manifestBadgeColor(route.manifestStatus)} text-[10px]`}>
                {manifestLabel(route.manifestStatus)}
              </Badge>
            </div>
            <div className="text-[11.5px] text-gray-500 mt-0.5">
              {route.driverName ? (
                <span>{route.driverName} · {route.vehicleRegistration}</span>
              ) : (
                <span className="text-amber-600">No driver / vehicle assigned</span>
              )}
            </div>
          </div>

          {/* Totals */}
          <div className="flex items-center gap-5 shrink-0">
            <div className="text-center">
              <div className="text-[18px] font-bold text-gray-900 leading-none">{route.totalOrders}</div>
              <div className="text-[10px] text-gray-500 mt-0.5">orders</div>
            </div>
            <div className="text-center">
              <div className="text-[18px] font-bold text-gray-900 leading-none">{route.totalBoxes}</div>
              <div className="text-[10px] text-gray-500 mt-0.5">boxes</div>
            </div>
            {/* Goods-in breakdown pills */}
            <div className="flex flex-col gap-1 text-[11px]">
              {route.ordersFullyReceived > 0 && (
                <span className="px-2 py-0.5 rounded-full bg-green-100 text-green-800 whitespace-nowrap">
                  ✓ {route.ordersFullyReceived} received
                </span>
              )}
              {route.ordersPartiallyReceived > 0 && (
                <span className="px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 whitespace-nowrap">
                  ~ {route.ordersPartiallyReceived} partial
                </span>
              )}
              {route.ordersNotYetReceived > 0 && (
                <span className="px-2 py-0.5 rounded-full bg-red-100 text-red-800 whitespace-nowrap">
                  ✗ {route.ordersNotYetReceived} not in
                </span>
              )}
            </div>
            {/* Goods-in bar */}
            <div className="w-[120px]">
              <div className="text-[10px] text-gray-500 mb-1">Goods in {goodsInPct}%</div>
              <GoodsInBar received={receivedBoxes} total={route.totalBoxes} />
            </div>
            <span className="text-gray-400 text-[14px]">{expanded ? '▲' : '▼'}</span>
          </div>
        </div>

        {/* Order table */}
        {expanded && (
          <CardContent className="p-0 border-t border-gray-100">
            {route.orders.length === 0 ? (
              <div className="px-4 py-5 text-center text-[12.5px] text-gray-400">
                No orders scheduled for this route on this date.
              </div>
            ) : (
              <table className="w-full border-collapse">
                <thead>
                  <tr>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Order ID
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Customer / Address
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Postcode
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Boxes
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 min-w-[120px]">
                      Goods In
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Status
                    </th>
                    <th className="px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50" />
                  </tr>
                </thead>
                <tbody>
                  {route.orders.map(order => {
                    const receivedCount = order.boxesReceived + order.boxesReady;
                    const allIn = order.boxesExpected === 0;
                    const noneIn = receivedCount === 0 && order.boxesExpected > 0;
                    const rowBg = allIn ? '' : noneIn ? 'bg-red-50/40' : 'bg-amber-50/40';

                    const statusColor =
                      order.orderStatus === 'READY_FOR_MANIFEST' ? 'green' :
                      order.orderStatus === 'EXCEPTION' ? 'red' :
                      'grey';
                    const statusLabel =
                      order.orderStatus === 'READY_FOR_MANIFEST' ? 'Ready' :
                      order.orderStatus === 'EXCEPTION' ? 'Exception' :
                      'Pending';

                    return (
                      <tr key={order.id} className={`hover:bg-gray-50 ${rowBg}`}>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] font-mono text-gray-700 align-middle">
                          {order.orderId}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 align-middle max-w-[220px] truncate">
                          {order.customerAddress || '—'}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] font-mono text-gray-700 align-middle">
                          {order.deliveryPostcode}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 align-middle">
                          {order.totalBoxes}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 align-middle">
                          <GoodsInBar received={receivedCount} total={order.totalBoxes} />
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 align-middle">
                          <Badge className={`badge-${statusColor} text-[10.5px]`}>
                            {statusLabel}
                          </Badge>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 align-middle text-right">
                          <button
                            onClick={() => setRerouteOrder(order)}
                            className="text-[11.5px] text-blue-600 hover:underline whitespace-nowrap"
                          >
                            Move route →
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </CardContent>
        )}
      </Card>
    </>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function DayPlan() {
  const { selectedDepotId, getDepotById, data } = useApp();
  const [dayPlan, setDayPlan] = useState<DayPlanDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);

  const depotRoutes: RouteDto[] = data?.routes.filter(r => r.depotId === selectedDepotId) ?? [];

  const loadDayPlan = async () => {
    if (!selectedDepotId) { setLoading(false); return; }
    try {
      setLoading(true);
      setError(null);
      const data = await depotService.getDayPlan(selectedDepotId, selectedDate);
      setDayPlan(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load day plan');
    } finally {
      setLoading(false);
    }
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { loadDayPlan(); }, [selectedDepotId, selectedDate]);

  const depot = getDepotById(selectedDepotId ?? '');

  if (loading) {
    return <div className="flex items-center justify-center min-h-[200px] text-gray-500 text-[13px]">Loading day plan…</div>;
  }

  if (!selectedDepotId || error) {
    return (
      <div className="flex items-center justify-center min-h-[200px] text-red-600 text-[13px]">
        {error || 'No depot selected'}
      </div>
    );
  }

  const dateLabel = new Date(selectedDate + 'T00:00:00').toLocaleDateString('en-GB', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
  });

  const routesWithOrders = dayPlan?.routes.filter(r => r.totalOrders > 0) ?? [];
  const routesEmpty = dayPlan?.routes.filter(r => r.totalOrders === 0) ?? [];

  const totalAwaitingBoxes = dayPlan?.routes.reduce((s, r) => {
    return s + r.orders.reduce((os, o) => os + o.boxesExpected, 0);
  }, 0) ?? 0;

  return (
    <>
      {/* Page header */}
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Day Plan</div>
          <div className="text-[11.5px] text-gray-500">
            {depot?.name} · {dateLabel}
          </div>
        </div>
        <input
          type="date"
          value={selectedDate}
          onChange={e => setSelectedDate(e.target.value)}
          className="text-[12px] px-2 py-1 border border-gray-300 rounded"
        />
      </div>

      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        {/* Summary strip */}
        {dayPlan && (
          <div className="grid grid-cols-4 gap-3.5 mb-5">
            <div className="stat-card blue">
              <div className="text-[10.5px] text-gray-500 uppercase tracking-wide font-semibold mb-1">Total Orders</div>
              <div className="text-[26px] font-bold text-gray-900 leading-none">{dayPlan.totalOrdersOnDay}</div>
              <div className="text-[11px] text-gray-500 mt-1">scheduled for {dateLabel.split(',')[0]}</div>
            </div>
            <div className="stat-card blue">
              <div className="text-[10.5px] text-gray-500 uppercase tracking-wide font-semibold mb-1">Total Boxes</div>
              <div className="text-[26px] font-bold text-gray-900 leading-none">{dayPlan.totalBoxesOnDay}</div>
              <div className="text-[11px] text-gray-500 mt-1">across all routes</div>
            </div>
            <div className={`stat-card ${totalAwaitingBoxes > 0 ? 'amber' : 'green'}`}>
              <div className="text-[10.5px] text-gray-500 uppercase tracking-wide font-semibold mb-1">Awaiting Goods</div>
              <div className="text-[26px] font-bold text-gray-900 leading-none">{totalAwaitingBoxes}</div>
              <div className="text-[11px] text-gray-500 mt-1">boxes not yet received</div>
            </div>
            <div className="stat-card blue">
              <div className="text-[10.5px] text-gray-500 uppercase tracking-wide font-semibold mb-1">Active Routes</div>
              <div className="text-[26px] font-bold text-gray-900 leading-none">{routesWithOrders.length}</div>
              <div className="text-[11px] text-gray-500 mt-1">
                {routesEmpty.length > 0 ? `${routesEmpty.length} idle` : 'all active'}
              </div>
            </div>
          </div>
        )}

        {/* Routes with orders */}
        {routesWithOrders.length === 0 ? (
          <Card>
            <CardContent className="py-10 text-center text-[13px] text-gray-400">
              No orders are scheduled for {dateLabel}. Orders are matched to a date via their Requested Delivery Date.
            </CardContent>
          </Card>
        ) : (
          <>
            {routesWithOrders.map(route => (
              <RouteSection
                key={route.routeId}
                route={route}
                depotRoutes={depotRoutes}
                onRerouteSuccess={loadDayPlan}
              />
            ))}

            {/* Idle routes (collapsed) */}
            {routesEmpty.length > 0 && (
              <div className="mt-2">
                <div className="text-[11px] text-gray-400 uppercase tracking-wide font-semibold mb-2 px-1">
                  Idle routes — no orders for this date
                </div>
                <div className="flex flex-wrap gap-2">
                  {routesEmpty.map(r => (
                    <span key={r.routeId} className="text-[11.5px] px-2.5 py-1 bg-white border border-gray-200 rounded text-gray-500">
                      {r.routeCode} {r.routeName}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </>
  );
}
