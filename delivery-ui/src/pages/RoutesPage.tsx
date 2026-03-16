import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { depotService } from '@/api/services/depotService';
import { routeService } from '@/api/services/routeService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { RouteDto, CreateRouteRequest, UpdateRouteRequest } from '@/api/types';

export default function RoutesPage() {
  const { selectedDepotId, getDepotById } = useApp();
  const [routes, setRoutes] = useState<RouteDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editingRoute, setEditingRoute] = useState<RouteDto | null>(null);
  const [formCode, setFormCode] = useState('');
  const [formName, setFormName] = useState('');
  const [formDescription, setFormDescription] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!selectedDepotId) {
      setLoading(false);
      return;
    }

    const loadRoutes = async () => {
      try {
        setLoading(true);
        setError(null);
        const routesData = await depotService.getRoutesByDepot(selectedDepotId);
        setRoutes(routesData);
      } catch (err) {
        console.error('Failed to load routes:', err);
        setError(err instanceof Error ? err.message : 'Failed to load routes');
      } finally {
        setLoading(false);
      }
    };

    loadRoutes();
  }, [selectedDepotId]);
  
  const handleAddRoute = () => {
    setFormCode('');
    setFormName('');
    setFormDescription('');
    setEditingRoute(null);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleEditRoute = (route: RouteDto) => {
    setFormCode(route.code);
    setFormName(route.name);
    setFormDescription(route.coverage || '');
    setEditingRoute(route);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleCancel = () => {
    setShowAddDialog(false);
    setEditingRoute(null);
    setFormCode('');
    setFormName('');
    setFormDescription('');
  };
  
  const handleSaveRoute = async () => {
    if (!selectedDepotId) return;
    
    if (!formCode.trim() || !formName.trim()) {
      setError('Code and name are required');
      return;
    }
    
    try {
      setSaving(true);
      setError(null);
      setSuccessMessage(null);
      
      if (editingRoute) {
        // Update existing route
        const updateRequest: UpdateRouteRequest = {
          name: formName.trim(),
          description: formDescription.trim() || undefined
        };
        await routeService.updateRoute(editingRoute.id, updateRequest);
        setSuccessMessage('Route updated successfully');
      } else {
        // Create new route
        const createRequest: CreateRouteRequest = {
          code: formCode.trim().toUpperCase(),
          name: formName.trim(),
          description: formDescription.trim() || undefined,
          depotId: selectedDepotId
        };
        await routeService.createRoute(createRequest);
        setSuccessMessage('Route created successfully');
      }
      
      // Reload routes
      const updatedRoutes = await depotService.getRoutesByDepot(selectedDepotId);
      setRoutes(updatedRoutes);
      handleCancel();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save route');
    } finally {
      setSaving(false);
    }
  };

  const depot = selectedDepotId ? getDepotById(selectedDepotId) : null;
  
  if (loading) {
    return <div className="p-5">Loading...</div>;
  }

  if (error) {
    return <div className="p-5 text-red-600">Error: {error}</div>;
  }

  if (!selectedDepotId) {
    return <div className="p-5">Please select a depot</div>;
  }
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Routes</div>
          <div className="text-[11.5px] text-gray-500">{depot?.name || 'Depot'} · {routes.length} routes</div>
        </div>
        {error && (
          <div className="text-[12px] text-red-600">{error}</div>
        )}
        {successMessage && (
          <div className="text-[12px] text-green-600">{successMessage}</div>
        )}
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700" onClick={handleAddRoute}>+ Add Route</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-blue-50 border border-blue-200 text-[12px] text-blue-800">
          Routes are permanently assigned to this depot. To move postcode coverage to another depot, reassign postcode rules via <Link to="/depot/postcodes" className="text-blue-600 hover:underline">Postcode Rules</Link>.
        </div>
        
        <Card>
          <CardHeader>
            <CardTitle className="text-[13.5px]">Routes — {depot?.name || 'Depot'}</CardTitle>
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
                      {route.coverage || '—'}
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                      {route.postcodeRulesCount} rules
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100">
                      <Badge variant="green">ACTIVE</Badge>
                    </td>
                    <td className="px-3.5 py-2 border-b border-gray-100">
                      <div className="flex items-center gap-1.5">
                        <Link to="/depot/postcodes" className="text-blue-600 hover:underline text-[12px]">
                          Postcodes
                        </Link>
                        <span 
                          className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                          onClick={() => handleEditRoute(route)}
                        >
                          Edit
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
        
        {/* Add/Edit Route Dialog */}
        {showAddDialog && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <Card className="w-full max-w-md">
              <CardHeader>
                <CardTitle className="text-[13.5px]">
                  {editingRoute ? 'Edit Route' : 'Add Route'}
                </CardTitle>
              </CardHeader>
              <CardContent>
                {error && (
                  <div className="mb-3 p-2 rounded-md bg-red-50 border border-red-200 text-[12px] text-red-800">
                    {error}
                  </div>
                )}
                <div className="space-y-3">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Code *</Label>
                    <Input
                      value={formCode}
                      onChange={(e) => setFormCode(e.target.value)}
                      placeholder="e.g. RT-A"
                      disabled={!!editingRoute}
                      className="font-mono"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Name *</Label>
                    <Input
                      value={formName}
                      onChange={(e) => setFormName(e.target.value)}
                      placeholder="e.g. Route A"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Coverage / Description</Label>
                    <Input
                      value={formDescription}
                      onChange={(e) => setFormDescription(e.target.value)}
                      placeholder="Optional description"
                    />
                  </div>
                  <div className="flex gap-2 pt-2">
                    <Button
                      variant="outline"
                      size="sm"
                      className="flex-1"
                      onClick={handleCancel}
                      disabled={saving}
                    >
                      Cancel
                    </Button>
                    <Button
                      size="sm"
                      className="flex-1 bg-blue-600 hover:bg-blue-700"
                      onClick={handleSaveRoute}
                      disabled={saving || !formCode.trim() || !formName.trim()}
                    >
                      {saving ? 'Saving...' : editingRoute ? 'Update' : 'Create'}
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </>
  );
}
