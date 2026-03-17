import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { depotService } from '@/api/services/depotService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { DepotDto, CreateDepotRequest, UpdateDepotRequest } from '@/api/types';

export default function Depots() {
  const { data, setSelectedDepotId, refreshDepots } = useApp();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editingDepot, setEditingDepot] = useState<DepotDto | null>(null);
  const [formName, setFormName] = useState('');
  const [formAddress, setFormAddress] = useState('');
  const [formLatitude, setFormLatitude] = useState('');
  const [formLongitude, setFormLongitude] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  
  if (!data) {
    return <div>Loading...</div>;
  }
  
  const handleSwitchDepot = (depotId: string) => {
    setSelectedDepotId(depotId);
    navigate('/dashboard');
  };
  
  const handleAddDepot = () => {
    setFormName('');
    setFormAddress('');
    setFormLatitude('');
    setFormLongitude('');
    setEditingDepot(null);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleEditDepot = (depot: DepotDto) => {
    setFormName(depot.name);
    setFormAddress(depot.location);
    setFormLatitude('');
    setFormLongitude('');
    setEditingDepot(depot);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleCancel = () => {
    setShowAddDialog(false);
    setEditingDepot(null);
    setFormName('');
    setFormAddress('');
    setFormLatitude('');
    setFormLongitude('');
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleSaveDepot = async () => {
    if (!formName.trim() || !formAddress.trim()) {
      setError('Name and address are required');
      return;
    }
    
    try {
      setSaving(true);
      setError(null);
      setSuccessMessage(null);
      
      if (editingDepot) {
        // Update existing depot
        const updateRequest: UpdateDepotRequest = {
          name: formName.trim(),
          address: formAddress.trim(),
          ...(formLatitude.trim() && { latitude: formLatitude.trim() }),
          ...(formLongitude.trim() && { longitude: formLongitude.trim() })
        };
        await depotService.updateDepot(editingDepot.id, updateRequest);
        setSuccessMessage('Depot updated successfully');
      } else {
        // Create new depot
        const createRequest: CreateDepotRequest = {
          name: formName.trim(),
          address: formAddress.trim(),
          ...(formLatitude.trim() && { latitude: formLatitude.trim() }),
          ...(formLongitude.trim() && { longitude: formLongitude.trim() })
        };
        await depotService.createDepot(createRequest);
        setSuccessMessage('Depot created successfully');
      }
      
      // Reload depots
      await refreshDepots();
      handleCancel();
    } catch (err) {
      console.error('Error saving depot:', err);
      const errorMessage = err instanceof Error ? err.message : 'Failed to save depot';
      setError(errorMessage);
    } finally {
      setSaving(false);
    }
  };
  
  // Filter depots based on search query
  const filteredDepots = data.depots.filter(depot =>
    depot.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    depot.location.toLowerCase().includes(searchQuery.toLowerCase())
  );
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">All Depots</div>
          <div className="text-[11.5px] text-gray-500">Central Admin · {data.depots.length} depots across the network</div>
        </div>
        {error && (
          <div className="text-[12px] text-red-600">{error}</div>
        )}
        {successMessage && (
          <div className="text-[12px] text-green-600">{successMessage}</div>
        )}
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700" onClick={handleAddDepot}>+ Add Depot</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-blue-50 border border-blue-200 text-[12px] text-blue-800">
          You are viewing all depots. To work within a specific depot (add orders, manage routes, vehicles etc.), select it from the <strong>Working Depot</strong> picker — visible on all depot-scoped screens.
        </div>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-[13.5px]">All Depots ({filteredDepots.length})</CardTitle>
            <Input 
              placeholder="Search…" 
              className="text-[12px] px-2.5 py-1 w-[180px]"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </CardHeader>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Depot Name
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Location
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Routes
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Vehicles
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Drivers
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Status
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                </tr>
              </thead>
              <tbody>
                {filteredDepots.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-3.5 py-4 text-center text-[12px] text-gray-500">
                      {searchQuery ? 'No depots found matching your search' : 'No depots available'}
                    </td>
                  </tr>
                ) : (
                  filteredDepots.map((depot) => (
                    <tr key={depot.id} className="hover:bg-gray-50">
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-semibold">
                        {depot.name}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {depot.location}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {depot.routesCount}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {depot.vehiclesCount}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                        {depot.driversCount}
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <Badge variant={depot.status === 'Active' ? 'green' : 'amber'}>
                          {depot.status}
                        </Badge>
                      </td>
                      <td className="px-3.5 py-2 border-b border-gray-100">
                        <div className="flex items-center gap-1.5">
                          <span
                            className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                            onClick={() => handleSwitchDepot(depot.id)}
                          >
                            Switch to →
                          </span>
                          <span 
                            className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                            onClick={() => handleEditDepot(depot)}
                          >
                            Edit
                          </span>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
                {filteredDepots.length > 5 && (
                  <tr>
                    <td colSpan={7} className="px-3.5 py-2 text-center text-[11px] text-gray-500 bg-gray-50">
                      … {filteredDepots.length - 5} more depots …
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </CardContent>
        </Card>
        
        {/* Add/Edit Depot Dialog */}
        {showAddDialog && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <Card className="w-full max-w-md">
              <CardHeader>
                <CardTitle className="text-[13.5px]">
                  {editingDepot ? 'Edit Depot' : 'Add Depot'}
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
                    <Label className="text-[11px]">Depot Name *</Label>
                    <Input
                      value={formName}
                      onChange={(e) => setFormName(e.target.value)}
                      placeholder="e.g. London Central"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Address *</Label>
                    <Input
                      value={formAddress}
                      onChange={(e) => setFormAddress(e.target.value)}
                      placeholder="e.g. Vauxhall, London"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Latitude (optional)</Label>
                    <Input
                      value={formLatitude}
                      onChange={(e) => setFormLatitude(e.target.value)}
                      placeholder="e.g. 51.4865"
                      type="number"
                      step="any"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Longitude (optional)</Label>
                    <Input
                      value={formLongitude}
                      onChange={(e) => setFormLongitude(e.target.value)}
                      placeholder="e.g. -0.1234"
                      type="number"
                      step="any"
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
                      onClick={handleSaveDepot}
                      disabled={saving || !formName.trim() || !formAddress.trim()}
                    >
                      {saving ? 'Saving...' : editingDepot ? 'Update' : 'Create'}
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
