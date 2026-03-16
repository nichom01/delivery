import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { dashboardService } from '@/api/services/dashboardService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import type { DashboardDto } from '@/api/types';

function StatCard({ label, value, sub, color }: { label: string; value: string | number; sub?: string; color: 'blue' | 'green' | 'amber' | 'red' }) {
  return (
    <div className={`stat-card ${color}`}>
      <div className="text-[10.5px] text-gray-500 uppercase tracking-wide font-semibold mb-1">
        {label}
      </div>
      <div className="text-[26px] font-bold text-gray-900 leading-none">{value}</div>
      {sub && <div className="text-[11px] text-gray-500 mt-1">{sub}</div>}
    </div>
  );
}

function ProgressBar({ percentage, color }: { percentage: number; color: 'green' | 'blue' | 'amber' }) {
  return (
    <div className="progress-bar">
      <div className={`progress-fill ${color}`} style={{ width: `${percentage}%` }}></div>
    </div>
  );
}

export default function Dashboard() {
  const { selectedDepotId, depots, getDepotById } = useApp();
  const [dashboard, setDashboard] = useState<DashboardDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  
  useEffect(() => {
    if (!selectedDepotId) {
      setLoading(false);
      return;
    }
    
    const loadDashboard = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await dashboardService.getDashboard(selectedDepotId, selectedDate);
        setDashboard(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load dashboard');
      } finally {
        setLoading(false);
      }
    };
    
    loadDashboard();
  }, [selectedDepotId, selectedDate]);
  
  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }
  
  if (error || !dashboard || !selectedDepotId) {
    return <div className="flex items-center justify-center min-h-screen text-red-600">{error || 'No depot selected'}</div>;
  }
  
  const depot = getDepotById(selectedDepotId);
  const today = new Date(dashboard.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
  
  const deliveryPercentage = dashboard.summary.deliveriesTotal > 0
    ? Math.round((dashboard.summary.deliveriesComplete / dashboard.summary.deliveriesTotal) * 100)
    : 0;
  const boxPercentage = dashboard.summary.boxesTotal > 0
    ? Math.round((dashboard.summary.boxesDelivered / dashboard.summary.boxesTotal) * 100)
    : 0;
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Depot Dashboard</div>
          <div className="text-[11.5px] text-gray-500">
            {depot?.name} · Live · {today}
          </div>
        </div>
        <input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          className="text-[12px] px-2 py-1 border border-gray-300 rounded"
        />
        <Button variant="outline" size="sm" className="text-[12px]">
          ⬇ Export
        </Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        {/* Stats Row */}
        <div className="grid grid-cols-4 gap-3.5 mb-5">
          <StatCard
            label="Total Routes Today"
            value={dashboard.summary.totalRoutes}
            sub="All vehicles dispatched"
            color="blue"
          />
          <StatCard
            label="Deliveries Complete"
            value={dashboard.summary.deliveriesComplete}
            sub={`of ${dashboard.summary.deliveriesTotal} total · ${deliveryPercentage}%`}
            color="green"
          />
          <StatCard
            label="Boxes Delivered"
            value={dashboard.summary.boxesDelivered}
            sub={`of ${dashboard.summary.boxesTotal.toLocaleString()} total · ${boxPercentage}%`}
            color="green"
          />
          <StatCard
            label="Exceptions"
            value={dashboard.summary.exceptionsCount}
            sub="Missing boxes · 3 orders"
            color="amber"
          />
        </div>
        
        {/* Route Summary Table */}
        <Card className="mb-4">
          <CardHeader className="flex flex-row items-center justify-between gap-3">
            <CardTitle className="text-[13.5px] font-semibold text-gray-800 whitespace-nowrap">
              Route Summary — Today
            </CardTitle>
            <span className="text-gray-500 text-[11px]">Last updated 11:47 · Auto-refreshing</span>
          </CardHeader>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap">
                      Route
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap">
                      Vehicle
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap">
                      Driver
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap">
                      Deliveries
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap">
                      Boxes
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap min-w-[140px]">
                      Delivery %
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap">
                      Box %
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap">
                      Status
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50 whitespace-nowrap"></th>
                  </tr>
                </thead>
                <tbody>
                  {dashboard.routeSummary.map((route) => {
                    const delPct = route.deliveriesTotal > 0
                      ? Math.round((route.deliveriesDone / route.deliveriesTotal) * 100)
                      : 0;
                    const boxPct = route.boxesTotal > 0
                      ? Math.round((route.boxesDone / route.boxesTotal) * 100)
                      : 0;
                    const statusColor =
                      route.status === 'Complete' ? 'green' :
                      route.status === 'In Progress' || route.status === 'Departed' ? 'blue' :
                      route.status === 'Exception' ? 'amber' : 'grey';
                    
                    return (
                      <tr key={route.routeId} className="hover:bg-gray-50">
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 align-middle">
                          <div className="font-semibold">{route.routeName}</div>
                          <div className="text-[11.5px] text-gray-500">{route.description}</div>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono align-middle">
                          {route.vehicle}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 align-middle">
                          {route.driver}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 align-middle">
                          {route.deliveriesDone} / {route.deliveriesTotal}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 align-middle">
                          {route.boxesDone} / {route.boxesTotal}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 align-middle">
                          <ProgressBar percentage={delPct} color={statusColor === 'green' ? 'green' : statusColor === 'amber' ? 'amber' : 'blue'} />
                          <div className="text-[11px] text-gray-500 mt-0.5">
                            {route.progressNote || `${delPct}%`}
                          </div>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 align-middle">
                          <ProgressBar percentage={boxPct} color={statusColor === 'green' ? 'green' : statusColor === 'amber' ? 'amber' : 'blue'} />
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 align-middle">
                          <Badge className={`badge-${statusColor}`}>
                            {route.status}
                          </Badge>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 align-middle">
                          {route.status === 'Pending' ? (
                            <Link to="/manifests" className="text-blue-600 hover:underline text-[12px]">
                              Manifest →
                            </Link>
                          ) : (
                            <Link
                              to={`/dashboard/routes/${route.routeId}`}
                              className="text-blue-600 hover:underline text-[12px]"
                            >
                              View →
                            </Link>
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
        
        {/* Exceptions and Awaiting Goods */}
        <div className="grid grid-cols-2 gap-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-[13.5px]">⚠ Open Exceptions</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <table className="w-full border-collapse">
                <thead>
                  <tr>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Order
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Box
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Route
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                  </tr>
                </thead>
                <tbody>
                  {dashboard.openExceptions.map((exc) => (
                    <tr key={exc.orderId} className="hover:bg-gray-50">
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono">
                        {exc.orderId}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {exc.boxesSummary}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {exc.routeName}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">View</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </CardContent>
          </Card>
          
          <Card>
            <CardHeader>
              <CardTitle className="text-[13.5px]">📦 Awaiting Goods</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              <table className="w-full border-collapse">
                <thead>
                  <tr>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Order
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Boxes Expected
                    </th>
                    <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                      Received
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {dashboard.awaitingGoods.map((order) => {
                    const badgeColor = order.boxesReceived === order.boxesExpected ? 'green' :
                      order.boxesReceived === 0 ? 'red' : 'amber';
                    return (
                      <tr key={order.orderId} className="hover:bg-gray-50">
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono">
                          {order.orderId}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {order.boxesExpected}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <Badge className={`badge-${badgeColor}`}>
                            {order.boxesReceived} / {order.boxesExpected}
                          </Badge>
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
    </>
  );
}
