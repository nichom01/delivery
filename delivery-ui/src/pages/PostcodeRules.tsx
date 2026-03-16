import { useState, useEffect } from 'react';
import { useApp } from '@/contexts/AppContext';
import { depotService } from '@/api/services/depotService';
import { postcodeService } from '@/api/services/postcodeService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { RouteDto, PostcodeRuleDto, PostcodeLookupDto } from '@/api/types';

export default function PostcodeRules() {
  const { selectedDepotId, getDepotById } = useApp();
  const [routes, setRoutes] = useState<RouteDto[]>([]);
  const [rules, setRules] = useState<PostcodeRuleDto[]>([]);
  const [testPostcode, setTestPostcode] = useState('SW8 1RT');
  const [lookupResult, setLookupResult] = useState<PostcodeLookupDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Filter state
  const [filterRouteId, setFilterRouteId] = useState<string>('');
  const [filterLevel, setFilterLevel] = useState<string>('');
  
  // Form state
  const [editingRule, setEditingRule] = useState<PostcodeRuleDto | null>(null);
  const [formPattern, setFormPattern] = useState('');
  const [formLevel, setFormLevel] = useState<'full' | 'sector' | 'district' | 'area' | 'letter'>('full');
  const [formRouteId, setFormRouteId] = useState('');
  const [formEffectiveFrom, setFormEffectiveFrom] = useState(new Date().toISOString().split('T')[0]);
  const [formEffectiveTo, setFormEffectiveTo] = useState('');
  const [formReason, setFormReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  useEffect(() => {
    if (!selectedDepotId) {
      setLoading(false);
      return;
    }

    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);
        const [routesData, rulesData] = await Promise.all([
          depotService.getRoutesByDepot(selectedDepotId),
          postcodeService.getPostcodeRules(selectedDepotId)
        ]);
        setRoutes(routesData);
        setRules(rulesData);
        // Initialize form route if routes are available
        if (routesData.length > 0 && !formRouteId) {
          setFormRouteId(routesData[0].id);
        }
      } catch (err) {
        console.error('Failed to load data:', err);
        setError(err instanceof Error ? err.message : 'Failed to load data');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [selectedDepotId]);

  const handleTest = async () => {
    try {
      const result = await postcodeService.lookupPostcode(testPostcode);
      setLookupResult(result);
    } catch (err) {
      console.error('Failed to lookup postcode:', err);
      setLookupResult(null);
    }
  };

  const handleAddRule = () => {
    // Reset form and show it
    setEditingRule(null);
    setFormPattern('');
    setFormLevel('full');
    setFormRouteId(routes.length > 0 ? routes[0].id : '');
    setFormEffectiveFrom(new Date().toISOString().split('T')[0]);
    setFormEffectiveTo('');
    setFormReason('');
    setSaveError(null);
  };
  
  const handleEditRule = (rule: PostcodeRuleDto) => {
    setEditingRule(rule);
    setFormPattern(rule.pattern);
    setFormLevel(rule.level);
    setFormRouteId(rule.routeId);
    setFormEffectiveFrom(rule.effectiveFrom);
    setFormEffectiveTo(rule.effectiveTo || '');
    setFormReason('');
    setSaveError(null);
  };

  const handleCancel = () => {
    setEditingRule(null);
    setFormPattern('');
    setFormLevel('full');
    setFormRouteId('');
    setFormEffectiveFrom(new Date().toISOString().split('T')[0]);
    setFormEffectiveTo('');
    setFormReason('');
    setSaveError(null);
  };

  const handleSaveRule = async () => {
    if (!formPattern.trim()) {
      setSaveError('Postcode pattern is required');
      return;
    }
    if (!formRouteId) {
      setSaveError('Route is required');
      return;
    }
    if (!formEffectiveFrom) {
      setSaveError('Effective from date is required');
      return;
    }

    // Determine level from pattern if not editing
    const normalizedPattern = formPattern.trim().toUpperCase();
    let level = formLevel;
    
    if (!editingRule) {
      const parts = normalizedPattern.split(' ');
      if (parts.length === 2 && parts[1].length >= 2) {
        level = 'full';
      } else if (parts.length === 2 && parts[1].length === 1) {
        level = 'sector';
      } else if (parts.length === 1 && parts[0].length >= 3) {
        level = 'district';
      } else if (parts.length === 1 && parts[0].length === 2) {
        level = 'area';
      } else if (parts.length === 1 && parts[0].length === 1) {
        level = 'letter';
      }
    }

    try {
      setSaving(true);
      setSaveError(null);
      
      if (editingRule && editingRule.id) {
        // Update existing rule
        await postcodeService.updatePostcodeRule(editingRule.id, {
          pattern: normalizedPattern,
          level,
          routeId: formRouteId,
          effectiveFrom: formEffectiveFrom,
          effectiveTo: formEffectiveTo || null
        });
      } else {
        // Create new rule
        await postcodeService.createPostcodeRule({
          pattern: normalizedPattern,
          level,
          routeId: formRouteId,
          effectiveFrom: formEffectiveFrom,
          effectiveTo: formEffectiveTo || undefined
        });
      }

      // Reload rules
      const rulesData = await postcodeService.getPostcodeRules(selectedDepotId!);
      setRules(rulesData);
      
      // Reset form
      handleCancel();
    } catch (err) {
      console.error('Failed to save postcode rule:', err);
      setSaveError(err instanceof Error ? err.message : 'Failed to save postcode rule');
    } finally {
      setSaving(false);
    }
  };

  const depot = selectedDepotId ? getDepotById(selectedDepotId) : null;
  
  // Filter rules based on selected filters
  const filteredRules = rules.filter(rule => {
    if (filterRouteId && rule.routeId !== filterRouteId) {
      return false;
    }
    if (filterLevel && rule.level.toLowerCase() !== filterLevel.toLowerCase()) {
      return false;
    }
    return true;
  });
  
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
          <div className="text-[16px] font-bold text-gray-900">Postcode Rules</div>
          <div className="text-[11.5px] text-gray-500">{depot?.name || 'Depot'} · Routes and their postcode coverage</div>
        </div>
        <Button size="sm" onClick={handleAddRule} className="bg-blue-600 hover:bg-blue-700">+ Add Rule</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="grid grid-cols-2 gap-4 items-start">
          <div className="space-y-4">
            {/* Postcode Lookup Test */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">Postcode Lookup Test</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex gap-2 mb-2.5">
                  <Input
                    value={testPostcode}
                    onChange={(e) => setTestPostcode(e.target.value)}
                    className="flex-1 font-mono"
                  />
                  <Button size="sm" onClick={handleTest} className="bg-blue-600 hover:bg-blue-700">
                    Test
                  </Button>
                </div>
                {lookupResult && (
                  <>
                    <div className="font-semibold text-[12px] text-gray-600 mb-1">
                      Hierarchy for {testPostcode}:
                    </div>
                    <div className="border border-gray-200 rounded-md overflow-hidden">
                      {lookupResult.hierarchy.map((level, idx) => (
                        <div
                          key={idx}
                          className={`flex items-center gap-2.5 px-3 py-2 border-b border-gray-200 last:border-b-0 ${
                            level.isMatch ? 'bg-blue-50' : 'opacity-45'
                          }`}
                        >
                          <div className={`w-2 h-2 rounded-full ${level.isMatch ? 'bg-green-600' : 'bg-gray-300'}`}></div>
                          <div className="font-mono font-semibold text-[12.5px] min-w-[100px]">{level.pattern}</div>
                          <div className="flex-1 text-[11px]">
                            {level.isMatch ? (
                              <span className="text-blue-600">→ {level.routeName}</span>
                            ) : (
                              <span className="text-gray-500">{level.level} — {level.routeName || 'no match'}</span>
                            )}
                          </div>
                          {level.isMatch && (
                            <span className="text-[9px] bg-blue-600 text-white px-1.5 py-0.5 rounded-full font-bold">
                              MATCH
                            </span>
                          )}
                        </div>
                      ))}
                    </div>
                  </>
                )}
              </CardContent>
            </Card>
            
            {/* Add / Edit Rule */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">
                  {editingRule ? 'Edit Rule' : 'Add Rule'}
                </CardTitle>
              </CardHeader>
              <CardContent>
                {saveError && (
                  <div className="mb-2 p-2 rounded-md bg-red-50 border border-red-200 text-[12px] text-red-800">
                    {saveError}
                  </div>
                )}
                <div className="grid grid-cols-2 gap-3.5">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Postcode Pattern *</Label>
                    <Input 
                      placeholder="e.g. SW8 or SW8 1AA" 
                      className="font-mono"
                      value={formPattern}
                      onChange={(e) => setFormPattern(e.target.value)}
                      disabled={!!editingRule}
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Level *</Label>
                    <select 
                      className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]"
                      value={formLevel}
                      onChange={(e) => setFormLevel(e.target.value as typeof formLevel)}
                      disabled={!!editingRule}
                    >
                      <option value="full">Full</option>
                      <option value="sector">Sector</option>
                      <option value="district">District</option>
                      <option value="area">Area</option>
                      <option value="letter">Letter</option>
                    </select>
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Route *</Label>
                    <select 
                      className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]"
                      value={formRouteId}
                      onChange={(e) => setFormRouteId(e.target.value)}
                    >
                      <option value="">Select a route</option>
                      {routes.map(r => (
                        <option key={r.id} value={r.id}>{r.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Effective From *</Label>
                    <Input 
                      type="date" 
                      value={formEffectiveFrom}
                      onChange={(e) => setFormEffectiveFrom(e.target.value)}
                    />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Effective To (blank = open)</Label>
                    <Input 
                      type="date" 
                      value={formEffectiveTo}
                      onChange={(e) => setFormEffectiveTo(e.target.value)}
                    />
                  </div>
                  <div className="flex flex-col gap-1 col-span-2">
                    <Label className="text-[11px]">Reason (audit note)</Label>
                    <Input 
                      placeholder="e.g. Boundary adjustment — Vauxhall transfer" 
                      value={formReason}
                      onChange={(e) => setFormReason(e.target.value)}
                    />
                  </div>
                </div>
                <div className="flex gap-2 justify-end mt-2.5">
                  <Button variant="outline" size="sm" onClick={handleCancel} disabled={saving}>
                    Cancel
                  </Button>
                  <Button 
                    size="sm" 
                    className="bg-blue-600 hover:bg-blue-700" 
                    onClick={handleSaveRule}
                    disabled={saving || !formPattern.trim() || !formRouteId || !formEffectiveFrom}
                  >
                    {saving ? 'Saving...' : editingRule ? 'Update Rule' : 'Save Rule'}
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
          
          {/* Active Rules */}
          <div>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="text-[13.5px]">Active Rules</CardTitle>
                <div className="flex items-center gap-1.5">
                  <select 
                    className="text-[12px] px-2 py-1 border border-gray-300 rounded"
                    value={filterRouteId}
                    onChange={(e) => setFilterRouteId(e.target.value)}
                  >
                    <option value="">All routes</option>
                    {routes.map(r => (
                      <option key={r.id} value={r.id}>{r.name}</option>
                    ))}
                  </select>
                  <select 
                    className="text-[12px] px-2 py-1 border border-gray-300 rounded"
                    value={filterLevel}
                    onChange={(e) => setFilterLevel(e.target.value)}
                  >
                    <option value="">All levels</option>
                    <option value="full">Full</option>
                    <option value="sector">Sector</option>
                    <option value="district">District</option>
                    <option value="area">Area</option>
                    <option value="letter">Letter</option>
                  </select>
                </div>
              </CardHeader>
              <CardContent className="p-0">
                <table className="w-full border-collapse">
                  <thead>
                    <tr>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Pattern
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Level
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Route
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        From
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        To
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredRules.length === 0 ? (
                      <tr>
                        <td colSpan={6} className="px-3.5 py-4 text-center text-[12px] text-gray-500">
                          No rules match the selected filters
                        </td>
                      </tr>
                    ) : (
                      filteredRules.map((rule, idx) => (
                      <tr key={idx} className="hover:bg-gray-50">
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono font-semibold">
                          {rule.pattern}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <span className="inline-block px-1.5 py-0.5 rounded text-[10.5px] font-mono bg-gray-100 text-gray-600 capitalize">
                            {rule.level}
                          </span>
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {rule.routeName}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {new Date(rule.effectiveFrom).toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: '2-digit' })}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                          {rule.effectiveTo ? new Date(rule.effectiveTo).toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: '2-digit' }) : '—'}
                        </td>
                        <td className="px-3.5 py-2 border-b border-gray-100">
                          <span 
                            className="text-blue-600 hover:underline text-[12px] cursor-pointer"
                            onClick={() => handleEditRule(rule)}
                          >
                            Edit
                          </span>
                        </td>
                      </tr>
                      ))
                    )}
                    {filteredRules.length > 6 && (
                      <tr>
                        <td colSpan={6} className="px-3.5 py-1.5 text-center text-[11px] text-gray-500 bg-gray-50">
                          … {filteredRules.length - 6} more active rules …
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
