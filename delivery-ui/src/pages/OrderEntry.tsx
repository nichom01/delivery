import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '@/contexts/AppContext';
import { postcodeService } from '@/api/services/postcodeService';
import { orderService } from '@/api/services/orderService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import type { PostcodeLookupDto } from '@/api/types';

export default function OrderEntry() {
  const { selectedDepotId, depots, getDepotById } = useApp();
  const navigate = useNavigate();
  const [orderId, setOrderId] = useState('ORD-4521');
  const [despatchId, setDespatchId] = useState('DSP-8834');
  const [orderDate, setOrderDate] = useState('2026-03-12');
  const [deliveryDate, setDeliveryDate] = useState('2026-03-14');
  const [customerName, setCustomerName] = useState('Acme Supplies Ltd');
  const [address1, setAddress1] = useState('Unit 4, Station Road');
  const [address2, setAddress2] = useState('Vauxhall');
  const [town, setTown] = useState('London');
  const [postcode, setPostcode] = useState('SW8 1RT');
  const [boxCount, setBoxCount] = useState(3);
  const [boxPrefix, setBoxPrefix] = useState('BOX-DSP8834-');
  const [lookup, setLookup] = useState<PostcodeLookupDto | null>(null);
  const [lookupLoading, setLookupLoading] = useState(false);
  
  const depot = getDepotById(selectedDepotId);
  
  useEffect(() => {
    if (postcode && postcode.length >= 3) {
      const timeoutId = setTimeout(async () => {
        try {
          setLookupLoading(true);
          const result = await postcodeService.lookupPostcode(postcode);
          setLookup(result);
        } catch (err) {
          console.error('Postcode lookup failed:', err);
          setLookup(null);
        } finally {
          setLookupLoading(false);
        }
      }, 500);
      return () => clearTimeout(timeoutId);
    } else {
      setLookup(null);
    }
  }, [postcode]);
  
  const matchedRoute = lookup?.hierarchy?.[0]?.routeName || 'Route A';
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Manual Order Entry</div>
          <div className="text-[11.5px] text-gray-500">
            Adding order to: <strong>{depot?.name}</strong>
          </div>
        </div>
        <Button variant="outline" size="sm">⬆ Import CSV</Button>
        <Button variant="outline" size="sm">View API Spec</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="mb-4 p-2.5 rounded-md bg-blue-50 border border-blue-200 text-[12px] text-blue-800">
          📍 This order will be allocated to a route at <strong>{depot?.name}</strong>. To add an order to a different depot, change the Working Depot in the top bar.
        </div>
        
        <div className="grid grid-cols-2 gap-4 items-start">
          <div className="space-y-4">
            {/* Order Identification */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">Order Identification</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-3.5">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Order ID *</Label>
                    <Input value={orderId} onChange={(e) => setOrderId(e.target.value)} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Despatch ID *</Label>
                    <Input value={despatchId} onChange={(e) => setDespatchId(e.target.value)} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Order Date *</Label>
                    <Input type="date" value={orderDate} onChange={(e) => setOrderDate(e.target.value)} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Requested Delivery Date</Label>
                    <Input type="date" value={deliveryDate} onChange={(e) => setDeliveryDate(e.target.value)} />
                  </div>
                </div>
              </CardContent>
            </Card>
            
            {/* Delivery Address */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">Delivery Address</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-3.5">
                  <div className="flex flex-col gap-1 col-span-2">
                    <Label className="text-[11px]">Customer / Company Name *</Label>
                    <Input value={customerName} onChange={(e) => setCustomerName(e.target.value)} />
                  </div>
                  <div className="flex flex-col gap-1 col-span-2">
                    <Label className="text-[11px]">Address Line 1 *</Label>
                    <Input value={address1} onChange={(e) => setAddress1(e.target.value)} />
                  </div>
                  <div className="flex flex-col gap-1 col-span-2">
                    <Label className="text-[11px]">Address Line 2</Label>
                    <Input value={address2} onChange={(e) => setAddress2(e.target.value)} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Town / City *</Label>
                    <Input value={town} onChange={(e) => setTown(e.target.value)} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Postcode *</Label>
                    <Input
                      value={postcode}
                      onChange={(e) => setPostcode(e.target.value)}
                      className="font-mono font-semibold"
                    />
                  </div>
                </div>
              </CardContent>
            </Card>
            
            {/* Box Details */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">Box Details</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 gap-3.5">
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Number of Boxes *</Label>
                    <Input type="number" value={boxCount} onChange={(e) => setBoxCount(Number(e.target.value))} />
                  </div>
                  <div className="flex flex-col gap-1">
                    <Label className="text-[11px]">Box ID Prefix</Label>
                    <Input value={boxPrefix} onChange={(e) => setBoxPrefix(e.target.value)} />
                  </div>
                </div>
                <div className="mt-2.5 p-2 bg-gray-50 rounded text-[12px] text-gray-600">
                  IDs: <span className="font-mono">{boxPrefix}001</span>, <span className="font-mono">-002</span>, <span className="font-mono">-003</span>
                </div>
              </CardContent>
            </Card>
          </div>
          
          {/* Route Allocation Preview */}
          <div>
            <Card className="border-2 border-blue-600">
              <CardHeader className="bg-blue-50">
                <CardTitle className="text-[13.5px] text-blue-600">🗺 Route Allocation Preview</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="mb-2.5">
                  <div className="text-[11px] text-gray-500 mb-1">Postcode:</div>
                  <div className="font-mono text-[18px] font-bold">{postcode}</div>
                </div>
                <div className="mb-2.5">
                  <div className="text-[11px] text-gray-500 mb-1">Hierarchy match:</div>
                  {lookupLoading ? (
                    <div className="text-[12px] text-gray-500">Loading...</div>
                  ) : lookup ? (
                  <div className="border border-gray-200 rounded-md overflow-hidden">
                    {lookup.hierarchy.map((level, idx) => (
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
                            <span className="text-blue-600">→ {matchedRoute}</span>
                          ) : (
                            <span className="text-gray-500">{level.level} — {level.isMatch ? 'MATCH' : 'skipped'}</span>
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
                  ) : (
                    <div className="text-[12px] text-gray-500">Enter a postcode to see route allocation</div>
                  )}
                </div>
                <div className="p-3 bg-white rounded-md border border-green-600 mb-2.5">
                  <div className="text-[11px] text-gray-500 mb-1">Allocated to:</div>
                  <div className="text-[16px] font-bold">{matchedRoute}</div>
                  <div className="text-[11px] text-gray-500">{depot?.name} · SW zones</div>
                </div>
                <div className="p-2.5 rounded-md bg-green-50 border border-green-200 text-[11.5px] text-green-800">
                  ✓ Ready to submit. Order will be assigned to {matchedRoute} at {depot?.name}.
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
        
        <div className="flex justify-end gap-2.5 mt-4">
          <Button variant="outline" size="sm" onClick={() => {
            setOrderId('');
            setDespatchId('');
            setCustomerName('');
            setAddress1('');
            setAddress2('');
            setTown('');
            setPostcode('');
            setBoxCount(0);
            setBoxPrefix('');
            setLookup(null);
          }}>Clear</Button>
          <Button 
            size="sm" 
            className="bg-blue-600 hover:bg-blue-700"
            onClick={async () => {
              try {
                const customerAddress = [address1, address2, town].filter(Boolean).join(', ');
                const boxIdentifiers = Array.from({ length: boxCount }, (_, i) => `${boxPrefix}${String(i + 1).padStart(3, '0')}`);
                
                await orderService.createOrder({
                  orderId,
                  despatchId,
                  customerAddress,
                  deliveryPostcode: postcode,
                  orderDate,
                  requestedDeliveryDate: deliveryDate,
                  boxIdentifiers,
                  expectedBoxCount: boxCount,
                });
                
                navigate('/dashboard');
              } catch (err) {
                alert(err instanceof Error ? err.message : 'Failed to create order');
              }
            }}
          >
            Submit Order →
          </Button>
        </div>
      </div>
    </>
  );
}
