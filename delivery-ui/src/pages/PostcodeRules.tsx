import { useState } from 'react';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { lookupPostcode } from '@/api/client';

export default function PostcodeRules() {
  const { data, selectedDepotId } = useApp();
  const [testPostcode, setTestPostcode] = useState('SW8 1RT');
  const [lookupResult, setLookupResult] = useState<ReturnType<typeof lookupPostcode> | null>(null);
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const routes = data.routes.filter(r => r.depotId === selectedDepotId);
  const rules = data.postcodeRules.filter(r => routes.some(rt => rt.id === r.routeId));
  
  const handleTest = () => {
    const result = lookupPostcode(testPostcode, data.postcodeRules);
    setLookupResult(result);
  };
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Postcode Rules</div>
          <div className="text-[11.5px] text-gray-500">London Central · Routes and their postcode coverage</div>
        </div>
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700">+ Add Rule</Button>
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
                              <span className="text-gray-500">{level.level} — skipped</span>
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
                <CardTitle className="text-[13.5px]">Add / Edit Rule</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-3.5">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Postcode Pattern *</Label>
                    <Input placeholder="e.g. SW8 or SW8 1AA" className="font-mono" />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Route *</Label>
                    <select className="px-2.5 py-1.5 border border-gray-300 rounded text-[12.5px]">
                      {routes.map(r => (
                        <option key={r.id}>{r.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Effective From *</Label>
                    <Input type="date" defaultValue="2026-03-13" />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Effective To (blank = open)</Label>
                    <Input type="date" />
                  </div>
                  <div className="flex flex-col gap-1 col-span-2">
                    <Label className="text-[11px]">Reason (audit note)</Label>
                    <Input placeholder="e.g. Boundary adjustment — Vauxhall transfer" />
                  </div>
                </div>
                <div className="flex gap-2 justify-end mt-2.5">
                  <Button variant="outline" size="sm">Cancel</Button>
                  <Button size="sm" className="bg-blue-600 hover:bg-blue-700">Save Rule</Button>
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
                  <select className="text-[12px] px-2 py-1 border border-gray-300 rounded">
                    <option>All routes</option>
                    {routes.map(r => (
                      <option key={r.id}>{r.name}</option>
                    ))}
                  </select>
                  <select className="text-[12px] px-2 py-1 border border-gray-300 rounded">
                    <option>All levels</option>
                    <option>Full</option>
                    <option>Sector</option>
                    <option>District</option>
                    <option>Area</option>
                    <option>Letter</option>
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
                    {rules.map((rule, idx) => (
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
                          <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">Edit</span>
                        </td>
                      </tr>
                    ))}
                    {rules.length > 6 && (
                      <tr>
                        <td colSpan={6} className="px-3.5 py-1.5 text-center text-[11px] text-gray-500 bg-gray-50">
                          … {rules.length - 6} more active rules …
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
