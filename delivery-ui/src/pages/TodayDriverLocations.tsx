import { useCallback, useEffect, useMemo, useState } from 'react';
import { useApp } from '@/contexts/AppContext';
import { driverLocationService } from '@/api/services/driverLocationService';
import { Card, CardContent } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import type { DriverLocationSampleDto } from '@/api/types';
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Scatter,
  ScatterChart,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

function utcDateString(d: Date): string {
  return d.toISOString().split('T')[0];
}

function padDomain(values: number[]): [number, number] {
  if (values.length === 0) {
    return [0, 1];
  }
  const min = Math.min(...values);
  const max = Math.max(...values);
  if (min === max) {
    const d = Math.abs(min) > 1 ? 0.01 : 0.001;
    return [min - d, max + d];
  }
  const pad = (max - min) * 0.12;
  return [min - pad, max + pad];
}

export default function TodayDriverLocations() {
  const { data, selectedDepotId, getDepotById } = useApp();
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedDate, setSelectedDate] = useState(utcDateString(new Date()));
  const [samples, setSamples] = useState<DriverLocationSampleDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [view, setView] = useState<'graphic' | 'data'>('graphic');

  const driverAppUsers = useMemo(() => {
    if (!data?.users || !selectedDepotId) {
      return [];
    }
    return data.users.filter(
      (u) => u.role === 'DRIVER' && u.depotId === selectedDepotId
    );
  }, [data?.users, selectedDepotId]);

  useEffect(() => {
    if (driverAppUsers.length === 0) {
      setSelectedUserId('');
      return;
    }
    setSelectedUserId((prev) =>
      driverAppUsers.some((u) => u.id === prev) ? prev : driverAppUsers[0].id
    );
  }, [driverAppUsers]);

  const loadSamples = useCallback(async () => {
    if (!selectedUserId) {
      setSamples([]);
      return;
    }
    try {
      setLoading(true);
      setError(null);
      const rows = await driverLocationService.listForUser(selectedUserId, selectedDate);
      setSamples(rows);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load locations');
      setSamples([]);
    } finally {
      setLoading(false);
    }
  }, [selectedUserId, selectedDate]);

  useEffect(() => {
    void loadSamples();
  }, [loadSamples]);

  const depot = selectedDepotId ? getDepotById(selectedDepotId) : undefined;

  const scatterData = useMemo(
    () =>
      samples.map((s) => ({
        lon: s.longitude,
        lat: s.latitude,
        recordedAt: s.recordedAt,
      })),
    [samples]
  );

  const seriesData = useMemo(
    () =>
      samples.map((s, i) => ({
        seq: i + 1,
        t: new Date(s.recordedAt).getTime(),
        lat: s.latitude,
        lon: s.longitude,
      })),
    [samples]
  );

  const lonDomain = padDomain(samples.map((s) => s.longitude));
  const latDomain = padDomain(samples.map((s) => s.latitude));

  if (!data || !selectedDepotId) {
    return <div className="flex items-center justify-center min-h-[200px] text-gray-500">Loading…</div>;
  }

  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex flex-wrap items-center gap-3 px-6 shrink-0">
        <div className="flex-1 min-w-[200px]">
          <div className="text-[16px] font-bold text-gray-900">Driver locations (day)</div>
          <div className="text-[11.5px] text-gray-500">
            {depot?.name ?? 'Depot'} · Day range is UTC (same as dashboard date)
          </div>
        </div>
        <label className="flex items-center gap-2 text-[12px] text-gray-600">
          <span className="whitespace-nowrap">Driver app user</span>
          <select
            className="text-[12px] px-2 py-1 border border-gray-300 rounded min-w-[160px]"
            value={selectedUserId}
            onChange={(e) => setSelectedUserId(e.target.value)}
            disabled={driverAppUsers.length === 0}
          >
            {driverAppUsers.length === 0 ? (
              <option value="">No driver users in depot</option>
            ) : (
              driverAppUsers.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name}
                </option>
              ))
            )}
          </select>
        </label>
        <label className="flex items-center gap-2 text-[12px] text-gray-600">
          <span>Date (UTC)</span>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="text-[12px] px-2 py-1 border border-gray-300 rounded"
          />
        </label>
      </div>

      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        {error && (
          <div className="mb-3 text-[12px] text-red-600 bg-red-50 border border-red-100 rounded px-3 py-2">
            {error}
          </div>
        )}

        <Tabs value={view} onValueChange={(v) => setView(v as 'graphic' | 'data')} className="w-full">
          <TabsList className="bg-white border border-gray-200">
            <TabsTrigger value="graphic" className="text-[12px]">
              Graphic
            </TabsTrigger>
            <TabsTrigger value="data" className="text-[12px]">
              Raw data
            </TabsTrigger>
          </TabsList>

          <TabsContent value="graphic" className="mt-4">
            <Card>
              <CardContent className="p-4 space-y-6">
                {loading ? (
                  <div className="h-[320px] flex items-center justify-center text-gray-500 text-[13px]">
                    Loading…
                  </div>
                ) : samples.length === 0 ? (
                  <div className="h-[320px] flex items-center justify-center text-gray-500 text-[13px] text-center px-4">
                    No location points for this driver on the selected UTC day.
                  </div>
                ) : (
                  <>
                    <div>
                      <div className="text-[12px] font-semibold text-gray-800 mb-2">
                        Position trace (longitude × latitude)
                      </div>
                      <div className="h-[280px] w-full min-h-[200px]">
                        <ResponsiveContainer width="100%" height="100%">
                          <ScatterChart margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                            <XAxis
                              type="number"
                              dataKey="lon"
                              name="Longitude"
                              domain={lonDomain}
                              tick={{ fontSize: 11 }}
                              label={{ value: 'Longitude', position: 'insideBottom', offset: -4, fontSize: 11 }}
                            />
                            <YAxis
                              type="number"
                              dataKey="lat"
                              name="Latitude"
                              domain={latDomain}
                              tick={{ fontSize: 11 }}
                              label={{
                                value: 'Latitude',
                                angle: -90,
                                position: 'insideLeft',
                                fontSize: 11,
                              }}
                            />
                            <Tooltip
                              cursor={{ strokeDasharray: '3 3' }}
                              formatter={(value: number, name: string) => [
                                typeof value === 'number' ? value.toFixed(5) : value,
                                name,
                              ]}
                              labelFormatter={() => 'Point'}
                            />
                            <Scatter name="Samples" data={scatterData} fill="#2563eb" />
                          </ScatterChart>
                        </ResponsiveContainer>
                      </div>
                    </div>
                    <div>
                      <div className="text-[12px] font-semibold text-gray-800 mb-2">
                        Latitude / longitude vs sample order
                      </div>
                      <div className="h-[240px] w-full min-h-[200px]">
                        <ResponsiveContainer width="100%" height="100%">
                          <LineChart data={seriesData} margin={{ top: 8, right: 16, left: 0, bottom: 8 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                            <XAxis dataKey="seq" tick={{ fontSize: 11 }} label={{ value: 'Point #', fontSize: 11 }} />
                            <YAxis yAxisId="lat" tick={{ fontSize: 11 }} width={44} />
                            <YAxis yAxisId="lon" orientation="right" tick={{ fontSize: 11 }} width={44} />
                            <Tooltip
                              labelFormatter={(seq) => `Point ${seq}`}
                              formatter={(v: number) => [v.toFixed(5), '']}
                            />
                            <Line
                              yAxisId="lat"
                              type="monotone"
                              dataKey="lat"
                              name="Latitude"
                              stroke="#16a34a"
                              dot={false}
                              strokeWidth={2}
                            />
                            <Line
                              yAxisId="lon"
                              type="monotone"
                              dataKey="lon"
                              name="Longitude"
                              stroke="#9333ea"
                              dot={false}
                              strokeWidth={2}
                            />
                          </LineChart>
                        </ResponsiveContainer>
                      </div>
                    </div>
                  </>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="data" className="mt-4">
            <Card>
              <CardContent className="p-0">
                {loading ? (
                  <div className="p-8 text-center text-gray-500 text-[13px]">Loading…</div>
                ) : samples.length === 0 ? (
                  <div className="p-8 text-center text-gray-500 text-[13px]">
                    No rows for this driver on the selected UTC day.
                  </div>
                ) : (
                  <div className="overflow-x-auto">
                    <table className="w-full border-collapse min-w-[640px]">
                      <thead>
                        <tr>
                          <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                            Recorded (UTC)
                          </th>
                          <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                            Received (UTC)
                          </th>
                          <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                            Latitude
                          </th>
                          <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                            Longitude
                          </th>
                          <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                            Id
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {samples.map((s) => (
                          <tr key={s.id} className="hover:bg-gray-50">
                            <td className="px-3.5 py-2 border-b border-gray-100 text-[12px] text-gray-800 font-mono">
                              {s.recordedAt}
                            </td>
                            <td className="px-3.5 py-2 border-b border-gray-100 text-[12px] text-gray-700 font-mono">
                              {s.receivedAt}
                            </td>
                            <td className="px-3.5 py-2 border-b border-gray-100 text-[12px] text-gray-800 font-mono">
                              {Number(s.latitude).toFixed(6)}
                            </td>
                            <td className="px-3.5 py-2 border-b border-gray-100 text-[12px] text-gray-800 font-mono">
                              {Number(s.longitude).toFixed(6)}
                            </td>
                            <td className="px-3.5 py-2 border-b border-gray-100 text-[11px] text-gray-500 font-mono">
                              {s.id}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </>
  );
}
