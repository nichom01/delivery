import { useState } from 'react';
import { useApp } from '@/contexts/AppContext';
import { driverService, CreateDriverRequest, UpdateDriverRequest } from '@/api/services/driverService';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { DriverDto } from '@/api/types';

export default function Drivers() {
  const { data, selectedDepotId, refreshDrivers } = useApp();
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [editingDriver, setEditingDriver] = useState<DriverDto | null>(null);
  const [formName, setFormName] = useState('');
  const [formContact, setFormContact] = useState('');
  const [formLicenceNumber, setFormLicenceNumber] = useState('');
  const [formLicenceExpiry, setFormLicenceExpiry] = useState('');
  const [formShiftInfo, setFormShiftInfo] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const drivers = data.drivers.filter(d => d.depotId === selectedDepotId);
  const depot = data.depots.find(d => d.id === selectedDepotId);
  
  const handleAddDriver = () => {
    setFormName('');
    setFormContact('');
    setFormLicenceNumber('');
    setFormLicenceExpiry('');
    setFormShiftInfo('');
    setEditingDriver(null);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleEditDriver = (driver: DriverDto) => {
    setFormName(driver.name);
    setFormContact(driver.contact || '');
    setFormLicenceNumber(driver.licenceNo || '');
    setFormLicenceExpiry(driver.expiry || '');
    setFormShiftInfo(''); // Shift info not in DTO, leave empty
    setEditingDriver(driver);
    setShowAddDialog(true);
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleCancel = () => {
    setShowAddDialog(false);
    setEditingDriver(null);
    setFormName('');
    setFormContact('');
    setFormLicenceNumber('');
    setFormLicenceExpiry('');
    setFormShiftInfo('');
    setError(null);
    setSuccessMessage(null);
  };
  
  const handleSaveDriver = async () => {
    if (!formName.trim()) {
      setError('Name is required');
      return;
    }
    
    try {
      setSaving(true);
      setError(null);
      setSuccessMessage(null);
      
      if (editingDriver) {
        // Update existing driver
        const updateRequest: UpdateDriverRequest = {
          name: formName.trim(),
          ...(formContact.trim() && { contact: formContact.trim() }),
          ...(formLicenceNumber.trim() && { licenceNumber: formLicenceNumber.trim() }),
          ...(formLicenceExpiry.trim() && { licenceExpiry: formLicenceExpiry.trim() }),
          ...(formShiftInfo.trim() && { shiftInfo: formShiftInfo.trim() })
        };
        await driverService.updateDriver(editingDriver.id, updateRequest);
        setSuccessMessage('Driver updated successfully');
      } else {
        // Create new driver
        const createRequest: CreateDriverRequest = {
          name: formName.trim(),
          depotId: selectedDepotId,
          ...(formContact.trim() && { contact: formContact.trim() }),
          ...(formLicenceNumber.trim() && { licenceNumber: formLicenceNumber.trim() }),
          ...(formLicenceExpiry.trim() && { licenceExpiry: formLicenceExpiry.trim() }),
          ...(formShiftInfo.trim() && { shiftInfo: formShiftInfo.trim() })
        };
        await driverService.createDriver(createRequest);
        setSuccessMessage('Driver created successfully');
      }
      
      await refreshDrivers();
      handleCancel();
    } catch (err) {
      console.error('Error saving driver:', err);
      const errorMessage = err instanceof Error ? err.message : 'Failed to save driver';
      setError(errorMessage);
    } finally {
      setSaving(false);
    }
  };
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Driver Management</div>
          <div className="text-[11.5px] text-gray-500">{depot?.name || 'Depot'} · {drivers.length} drivers</div>
        </div>
        {error && (
          <div className="text-[12px] text-red-600">{error}</div>
        )}
        {successMessage && (
          <div className="text-[12px] text-green-600">{successMessage}</div>
        )}
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700" onClick={handleAddDriver}>+ Add Driver</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <Card>
          <CardContent className="p-0">
            <table className="w-full border-collapse">
              <thead>
                <tr>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Name
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Licence No.
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Expiry
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Contact
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Today's Route
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                    Status
                  </th>
                  <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                </tr>
              </thead>
              <tbody>
                {drivers.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-3.5 py-4 text-center text-[12px] text-gray-500">
                      No drivers available
                    </td>
                  </tr>
                ) : (
                  drivers.map((driver) => {
                    const route = driver.todaysRouteId
                      ? data.routes.find(r => r.id === driver.todaysRouteId)
                      : null;
                    return (
                      <tr key={driver.id} className="hover:bg-gray-50">
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-semibold">
                          {driver.name}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono">
                          {driver.licenceNo}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {driver.expiry}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {driver.contact}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {route?.name || '—'}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <Badge variant={driver.status === 'Active' ? 'green' : 'grey'}>
                            {driver.status}
                          </Badge>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <span 
                            className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                            onClick={() => handleEditDriver(driver)}
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
        
        {/* Add Driver Dialog */}
        {showAddDialog && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <Card className="w-full max-w-md">
              <div className="p-6">
                <h2 className="text-[13.5px] font-semibold mb-4">
                  {editingDriver ? 'Edit Driver' : 'Add Driver'}
                </h2>
                {error && (
                  <div className="mb-3 p-2 rounded-md bg-red-50 border border-red-200 text-[12px] text-red-800">
                    {error}
                  </div>
                )}
                <div className="space-y-3">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Name *</Label>
                    <Input
                      value={formName}
                      onChange={(e) => setFormName(e.target.value)}
                      placeholder="e.g. John Smith"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Contact (optional)</Label>
                    <Input
                      value={formContact}
                      onChange={(e) => setFormContact(e.target.value)}
                      placeholder="e.g. phone or email"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Licence Number (optional)</Label>
                    <Input
                      value={formLicenceNumber}
                      onChange={(e) => setFormLicenceNumber(e.target.value)}
                      placeholder="e.g. DRIV123456789"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Licence Expiry (optional)</Label>
                    <Input
                      value={formLicenceExpiry}
                      onChange={(e) => setFormLicenceExpiry(e.target.value)}
                      placeholder="YYYY-MM-DD"
                      type="date"
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Shift Info (optional)</Label>
                    <Input
                      value={formShiftInfo}
                      onChange={(e) => setFormShiftInfo(e.target.value)}
                      placeholder="e.g. Mon-Fri, 8am-5pm"
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
                      onClick={handleSaveDriver}
                      disabled={saving || !formName.trim()}
                    >
                      {saving ? 'Saving...' : editingDriver ? 'Update' : 'Create'}
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
