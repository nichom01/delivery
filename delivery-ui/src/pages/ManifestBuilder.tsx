import { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { manifestService } from '@/api/services/manifestService';
import { depotService } from '@/api/services/depotService';
import { vehicleService } from '@/api/services/vehicleService';
import { driverService } from '@/api/services/driverService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import type { ManifestDto, RouteDto, VehicleDto, DriverDto, ManifestStopDto } from '@/api/types';

export default function ManifestBuilder() {
  const { manifestId } = useParams();
  const [searchParams] = useSearchParams();
  const routeIdFromUrl = searchParams.get('routeId');
  const { selectedDepotId, getDepotById } = useApp();
  const navigate = useNavigate();
  const [manifests, setManifests] = useState<ManifestDto[]>([]);
  const [routes, setRoutes] = useState<RouteDto[]>([]);
  const [vehicles, setVehicles] = useState<VehicleDto[]>([]);
  const [drivers, setDrivers] = useState<DriverDto[]>([]);
  const [routeStops, setRouteStops] = useState<ManifestStopDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [routeId, setRouteId] = useState('');
  const [driverId, setDriverId] = useState('');
  const [vehicleId, setVehicleId] = useState('');
  const [date, setDate] = useState('');

  useEffect(() => {
    if (!selectedDepotId) {
      setLoading(false);
      return;
    }

    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);
        const [manifestsData, routesData, vehiclesData, driversData] = await Promise.all([
          manifestService.getManifests(selectedDepotId),
          depotService.getRoutesByDepot(selectedDepotId),
          vehicleService.getVehicles(selectedDepotId),
          driverService.getDrivers(selectedDepotId)
        ]);
        setManifests(manifestsData);
        setRoutes(routesData);
        setVehicles(vehiclesData);
        setDrivers(driversData);
      } catch (err) {
        console.error('Failed to load data:', err);
        setError(err instanceof Error ? err.message : 'Failed to load data');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [selectedDepotId]);
  
  // Compute manifest based on manifests array and manifestId
  // Only set manifest if we're editing a specific manifest (manifestId is provided)
  // If no manifestId, allow creating a new manifest for any route
  const manifest = manifestId
    ? manifests.find(m => m.id === manifestId)
    : null;
  
  // Initialize form values when manifest loads or when routes are available
  useEffect(() => {
    if (manifest) {
      setRouteId(manifest.routeId);
      setDriverId(manifest.driverId);
      setVehicleId(manifest.vehicleId);
      setDate(manifest.date);
    } else if (routes.length > 0 && !manifest) {
      // Initialize route and date if no manifest exists
      // Always prioritize routeId from URL query parameter if provided and valid
      if (routeIdFromUrl) {
        const routeExists = routes.find(r => r.id === routeIdFromUrl);
        if (routeExists) {
          setRouteId(routeIdFromUrl);
        } else if (!routeId) {
          // If URL route doesn't exist, fall back to first route
          setRouteId(routes[0].id);
        }
      } else if (!routeId && routes.length > 0) {
        // No URL parameter, use first route
        setRouteId(routes[0].id);
      }
      if (!date) {
        setDate(new Date().toISOString().split('T')[0]);
      }
    }
  }, [manifest, routes, routeIdFromUrl]);
  
  // When arriving via ?routeId= (no manifestId in URL), redirect to the existing
  // draft manifest for that route so the page binds correctly
  useEffect(() => {
    if (!manifestId && routeIdFromUrl && manifests.length > 0) {
      const existingDraft = manifests.find(
        m => m.routeId === routeIdFromUrl && (m.status === 'DRAFT' || m.status === 'CONFIRMED')
      );
      if (existingDraft) {
        navigate(`/manifests/${existingDraft.id}`, { replace: true });
      }
    }
  }, [manifests, manifestId, routeIdFromUrl]);

  // Fetch route stops when routeId is selected but no manifest exists
  useEffect(() => {
    if (!manifest && routeId && routes.length > 0) {
      const loadRouteStops = async () => {
        try {
          const stops = await manifestService.getRouteStops(routeId);
          setRouteStops(stops);
        } catch (err) {
          console.error('Failed to load route stops:', err);
          setRouteStops([]);
        }
      };
      loadRouteStops();
    } else {
      setRouteStops([]);
    }
  }, [manifest, routeId, routes]);
  
  const handleSaveDraft = async () => {
    // If no manifest exists, create one
    if (!manifest) {
      if (!routeId || !driverId || !vehicleId || !date) {
        setError('Please select a route, driver, vehicle, and date');
        return;
      }
      
      try {
        setSaving(true);
        setError(null);
        setSuccessMessage(null);
        
        const newManifest = await manifestService.createManifest({
          routeId: routeId,
          date: date,
          driverId: driverId,
          vehicleId: vehicleId
        });

        setSuccessMessage('Manifest created successfully');
        // Reload manifests then navigate to the new manifest URL so the page
        // binds to it correctly and Confirm becomes active
        const updatedManifests = await manifestService.getManifests(selectedDepotId);
        setManifests(updatedManifests);
        navigate(`/manifests/${newManifest.id}`, { replace: true });
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to create manifest');
      } finally {
        setSaving(false);
      }
      return;
    }
    
    try {
      setSaving(true);
      setError(null);
      setSuccessMessage(null);
      
      const updateRequest: any = {};
      if (driverId !== manifest.driverId) updateRequest.driverId = driverId;
      if (vehicleId !== manifest.vehicleId) updateRequest.vehicleId = vehicleId;
      if (date !== manifest.date) updateRequest.date = date;
      
      if (Object.keys(updateRequest).length > 0) {
        await manifestService.updateManifest(manifest.id, updateRequest);
        setSuccessMessage('Manifest draft saved successfully');
        // Reload manifests
        const updatedManifests = await manifestService.getManifests(selectedDepotId);
        setManifests(updatedManifests);
      } else {
        setSuccessMessage('No changes to save');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save manifest');
    } finally {
      setSaving(false);
    }
  };
  
  const handleConfirmManifest = async () => {
    if (!manifest) return;
    
    try {
      setSaving(true);
      setError(null);
      setSuccessMessage(null);
      
      // Save any pending changes first
      const updateRequest: any = {};
      if (driverId !== manifest.driverId) updateRequest.driverId = driverId;
      if (vehicleId !== manifest.vehicleId) updateRequest.vehicleId = vehicleId;
      if (date !== manifest.date) updateRequest.date = date;
      
      if (Object.keys(updateRequest).length > 0) {
        await manifestService.updateManifest(manifest.id, updateRequest);
      }
      
      // Then confirm
      await manifestService.confirmManifest(manifest.id);
      setSuccessMessage('Manifest confirmed successfully');
      
      // Reload manifests
      const updatedManifests = await manifestService.getManifests(selectedDepotId);
      setManifests(updatedManifests);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to confirm manifest');
    } finally {
      setSaving(false);
    }
  };
  
  const handleRemoveStop = async (orderId: string) => {
    if (!manifest) return;
    
    if (!confirm(`Are you sure you want to remove order ${orderId} from this manifest?`)) {
      return;
    }
    
    try {
      setError(null);
      setSuccessMessage(null);
      await manifestService.removeStopFromManifest(manifest.id, orderId);
      setSuccessMessage(`Order ${orderId} removed from manifest`);
      // Reload manifests
      const updatedManifests = await manifestService.getManifests(selectedDepotId);
      setManifests(updatedManifests);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to remove stop');
    }
  };

  const route = routes.find(r => r.id === (manifest?.routeId || routeId));
  const driver = manifest ? drivers.find(d => d.id === manifest.driverId) : (driverId ? drivers.find(d => d.id === driverId) : null);
  const vehicle = manifest ? vehicles.find(v => v.id === manifest.vehicleId) : (vehicleId ? vehicles.find(v => v.id === vehicleId) : null);

  const depot = selectedDepotId ? getDepotById(selectedDepotId) : null;
  
  // Determine if form should be enabled (no manifest or manifest is DRAFT)
  const isFormEnabled = !manifest || manifest.status === 'DRAFT';
  
  // Get stops to display - use manifest stops if manifest exists, otherwise use route stops
  const stopsToDisplay = manifest?.stops || routeStops;
  
  if (loading) {
    return <div className="p-5">Loading...</div>;
  }

  if (!selectedDepotId) {
    return <div className="p-5">Please select a depot</div>;
  }
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">
            Manifest Builder — {route?.name || 'Route F'}
          </div>
          <div className="text-[11.5px] text-gray-500">
            {depot?.name || 'Depot'} · {manifest?.date || new Date().toISOString().split('T')[0]} · {manifest?.status || 'Draft'}
          </div>
        </div>
        {error && (
          <div className="text-[12px] text-red-600">{error}</div>
        )}
        {successMessage && (
          <div className="text-[12px] text-green-600">{successMessage}</div>
        )}
        <Button 
          variant="outline" 
          size="sm"
          onClick={handleSaveDraft}
          disabled={saving || !isFormEnabled}
        >
          {saving ? 'Saving...' : manifest ? 'Save Draft' : 'Create Manifest'}
        </Button>
        <Button 
          size="sm" 
          className="bg-green-600 hover:bg-green-700"
          onClick={handleConfirmManifest}
          disabled={saving || !manifest || manifest.status !== 'DRAFT'}
        >
          ✓ Confirm Manifest
        </Button>
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
                    <Label className="text-[11px]">Route *</Label>
                    {manifest ? (
                      <Input value={route?.name || ''} disabled />
                    ) : (
                      <select 
                        className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]"
                        value={routeId || ''}
                        onChange={(e) => setRouteId(e.target.value)}
                      >
                        <option value="">Select a route</option>
                        {routes.map(r => (
                          <option key={r.id} value={r.id}>
                            {r.name}
                          </option>
                        ))}
                      </select>
                    )}
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Delivery Date</Label>
                    <Input 
                      type="date" 
                      value={date || manifest?.date || new Date().toISOString().split('T')[0]} 
                      onChange={(e) => setDate(e.target.value)}
                      disabled={!isFormEnabled}
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Driver *</Label>
                    <select 
                      className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]"
                      value={driverId || manifest?.driverId || ''}
                      onChange={(e) => setDriverId(e.target.value)}
                      disabled={!isFormEnabled}
                    >
                      <option value="">Select a driver</option>
                      {drivers.map(d => (
                        <option key={d.id} value={d.id}>
                          {d.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Vehicle *</Label>
                    <select 
                      className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]"
                      value={vehicleId || manifest?.vehicleId || ''}
                      onChange={(e) => setVehicleId(e.target.value)}
                      disabled={!isFormEnabled}
                    >
                      <option value="">Select a vehicle</option>
                      {vehicles.map(v => (
                        <option key={v.id} value={v.id}>
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
                  Delivery Stops ({stopsToDisplay.length})
                </CardTitle>
                <div className="text-[11px] text-gray-500">
                  {stopsToDisplay.length > 0 
                    ? `${stopsToDisplay.reduce((sum, stop) => {
                        const boxes = typeof stop.boxes === 'number' ? stop.boxes : parseInt(String(stop.boxes).split(' ')[0]) || 0;
                        return sum + boxes;
                      }, 0)} boxes to load`
                    : 'No boxes to load'}
                </div>
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
                    {stopsToDisplay.length > 0 ? (
                      stopsToDisplay.map((stop) => {
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
                              {manifest?.status === 'DRAFT' && (
                                <span 
                                  className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                                  onClick={() => handleRemoveStop(stop.orderId)}
                                >
                                  Remove
                                </span>
                              )}
                            </td>
                          </tr>
                        );
                      })
                    ) : (
                      <tr>
                        <td colSpan={5} className="px-3.5 py-8 text-center text-[12.5px] text-gray-500">
                          {manifest ? 'No orders with received boxes found for this route' : routeId ? 'No orders with received boxes found for this route' : 'Select a route to see orders with received boxes'}
                        </td>
                      </tr>
                    )}
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
