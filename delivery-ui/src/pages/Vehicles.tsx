import { useState } from 'react';
import { useApp } from '@/contexts/AppContext';
import { vehicleService, CreateVehicleRequest, UpdateVehicleRequest } from '@/api/services/vehicleService';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { VehicleDto } from '@/api/types';

export default function Vehicles() {
  const { data, selectedDepotId, refreshVehicles } = useApp();
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState<VehicleDto | null>(null);
  const [formRegistration, setFormRegistration] = useState('');
  const [formMake, setFormMake] = useState('');
  const [formModel, setFormModel] = useState('');
  const [formCapacity, setFormCapacity] = useState('');
  const [formMotDate, setFormMotDate] = useState('');
  const [formNextServiceDue, setFormNextServiceDue] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const vehicles = data.vehicles.filter(v => v.depotId === selectedDepotId);
  const depot = data.depots.find(d => d.id === selectedDepotId);
  
  const handleAddVehicle = () => {
    setFormRegistration('');
    setFormMake('');
    setFormModel('');
    setFormCapacity('');
    setFormMotDate('');
    setFormNextServiceDue('');
    setEditingVehicle(null);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleEditVehicle = (vehicle: VehicleDto) => {
    setFormRegistration(vehicle.registration);
    // Parse makeModel back into make and model (assuming format "Make Model")
    const makeModelParts = vehicle.makeModel.split(' ');
    setFormMake(makeModelParts[0] || '');
    setFormModel(makeModelParts.slice(1).join(' ') || '');
    setFormCapacity(vehicle.capacity || '');
    setFormMotDate(vehicle.motDue || '');
    setFormNextServiceDue(vehicle.nextService || '');
    setEditingVehicle(vehicle);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleCancel = () => {
    setShowAddDialog(false);
    setEditingVehicle(null);
    setFormRegistration('');
    setFormMake('');
    setFormModel('');
    setFormCapacity('');
    setFormMotDate('');
    setFormNextServiceDue('');
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleSaveVehicle = async () => {
    if (!formRegistration.trim()) {
      setError('Registration is required');
      return;
    }
    
    try {
      setSaving(true);
      setError(null);
      setSuccessMessage(null);
      
      if (editingVehicle) {
        // Update existing vehicle
        const updateRequest: UpdateVehicleRequest = {
          registration: formRegistration.trim(),
          ...(formMake.trim() && { make: formMake.trim() }),
          ...(formModel.trim() && { model: formModel.trim() }),
          ...(formCapacity.trim() && { capacity: formCapacity.trim() }),
          ...(formMotDate.trim() && { motDate: formMotDate.trim() }),
          ...(formNextServiceDue.trim() && { nextServiceDue: formNextServiceDue.trim() })
        };
        await vehicleService.updateVehicle(editingVehicle.id, updateRequest);
        setSuccessMessage('Vehicle updated successfully');
      } else {
        // Create new vehicle
        const createRequest: CreateVehicleRequest = {
          registration: formRegistration.trim(),
          depotId: selectedDepotId,
          ...(formMake.trim() && { make: formMake.trim() }),
          ...(formModel.trim() && { model: formModel.trim() }),
          ...(formCapacity.trim() && { capacity: formCapacity.trim() }),
          ...(formMotDate.trim() && { motDate: formMotDate.trim() }),
          ...(formNextServiceDue.trim() && { nextServiceDue: formNextServiceDue.trim() })
        };
        await vehicleService.createVehicle(createRequest);
        setSuccessMessage('Vehicle created successfully');
      }
      
      await refreshVehicles();
      handleCancel();
    } catch (err) {
      console.error('Error saving vehicle:', err);
      const errorMessage = err instanceof Error ? err.message : 'Failed to save vehicle';
      setError(errorMessage);
    } finally {
      setSaving(false);
    }
  };
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Vehicle Fleet</div>
          <div className="text-[11.5px] text-gray-500">{depot?.name || 'Depot'} · {vehicles.length} vehicles</div>
        </div>
        {error && (
          <div className="text-[12px] text-red-600">{error}</div>
        )}
        {successMessage && (
          <div className="text-[12px] text-green-600">{successMessage}</div>
        )}
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700" onClick={handleAddVehicle}>+ Add Vehicle</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-amber-50 border border-amber-200 text-[12px] text-amber-800">
          ⚠ 1 vehicle has MOT due within 30 days — <span className="text-blue-600 hover:underline cursor-pointer">VW21 MNO</span>
        </div>
        
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Registration
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Make / Model
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Capacity
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    MOT Due
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Next Service
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Status
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                </tr>
              </thead>
              <tbody>
                {vehicles.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-3.5 py-4 text-center text-[12px] text-gray-500">
                      No vehicles available
                    </td>
                  </tr>
                ) : (
                  vehicles.map((vehicle) => {
                    const motDueSoon = vehicle.status === 'MOT Due Soon';
                    return (
                      <tr key={vehicle.id} className={`hover:bg-gray-50 ${motDueSoon ? 'bg-amber-50' : ''}`}>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono font-semibold">
                          {vehicle.registration}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {vehicle.makeModel}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {vehicle.capacity}
                        </td>
                        <td className={`px-3.5 py-2 border-b border-gray-100 text-[12.5px] ${motDueSoon ? 'text-amber-600 font-semibold' : 'text-gray-700'}`}>
                          {vehicle.motDue} {motDueSoon && '⚠'}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {vehicle.nextService}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <Badge variant={motDueSoon ? 'amber' : 'green'}>
                            {vehicle.status}
                          </Badge>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <span 
                            className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                            onClick={() => handleEditVehicle(vehicle)}
                          >
                            Edit
                          </span>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </CardContent>
        </Card>
        
        {/* Add Vehicle Dialog */}
        {showAddDialog && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <Card className="w-full max-w-md">
              <div className="p-6">
                <h2 className="text-[13.5px] font-semibold mb-4">
                  {editingVehicle ? 'Edit Vehicle' : 'Add Vehicle'}
                </h2>
                {error && (
                  <div className="mb-3 p-2 rounded-md bg-red-50 border border-red-200 text-[12px] text-red-800">
                    {error}
                  </div>
                )}
                <div className="space-y-3">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Registration *</Label>
                    <Input
                      value={formRegistration}
                      onChange={(e) => setFormRegistration(e.target.value)}
                      placeholder="e.g. AB12 CDE"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Make (optional)</Label>
                    <Input
                      value={formMake}
                      onChange={(e) => setFormMake(e.target.value)}
                      placeholder="e.g. Ford"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Model (optional)</Label>
                    <Input
                      value={formModel}
                      onChange={(e) => setFormModel(e.target.value)}
                      placeholder="e.g. Transit"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Capacity (optional)</Label>
                    <Input
                      value={formCapacity}
                      onChange={(e) => setFormCapacity(e.target.value)}
                      placeholder="e.g. 3.5t / 12m³"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">MOT Due Date (optional)</Label>
                    <Input
                      value={formMotDate}
                      onChange={(e) => setFormMotDate(e.target.value)}
                      placeholder="YYYY-MM-DD"
                      type="date"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Next Service Due (optional)</Label>
                    <Input
                      value={formNextServiceDue}
                      onChange={(e) => setFormNextServiceDue(e.target.value)}
                      placeholder="YYYY-MM-DD"
                      type="date"
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
                      onClick={handleSaveVehicle}
                      disabled={saving || !formRegistration.trim()}
                    >
                      {saving ? 'Saving...' : editingVehicle ? 'Update' : 'Create'}
                    </Button>
                  </div>
                </div>
              </div>
            </Card>
          </div>
        )}
      </div>
    </>
  );
}
