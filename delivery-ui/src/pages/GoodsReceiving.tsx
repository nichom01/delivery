import { useState } from 'react';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';

export default function GoodsReceiving() {
  const { data, selectedDepotId } = useApp();
  const [boxId, setBoxId] = useState('BOX-DSP8820-002');
  const [selectedOrder, setSelectedOrder] = useState<string | null>(null);
  
  if (!data || !selectedDepotId) {
    return <div>Loading...</div>;
  }
  
  const orders = data.ordersAwaitingGoods;
  const order = selectedOrder ? orders.find(o => o.orderId === selectedOrder) : orders[0];
  const awaitingCount = orders.filter(o => o.boxesReceived < o.boxesExpected).length;
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Goods Receiving</div>
          <div className="text-[11.5px] text-gray-500">London Central · Check in arriving boxes</div>
        </div>
        {awaitingCount > 0 && (
          <div className="flex items-center gap-1.5 bg-amber-50 px-2.5 py-1 rounded text-[12px] text-amber-800">
            🟡 {awaitingCount} orders awaiting goods
          </div>
        )}
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <div className="grid grid-cols-2 gap-4 items-start">
          <div className="space-y-4">
            {/* Check In Box */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">Check In a Box</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="mb-2.5 p-2.5 rounded-md bg-blue-50 border border-blue-200 text-[12px] text-blue-800">
                  Scan or enter the box ID to check it in against an order.
                </div>
                <div className="flex flex-col gap-2.5 mb-2.5">
                  <Label className="text-[11px]">Box ID / Barcode</Label>
                  <div className="flex gap-2">
                    <Input
                      value={boxId}
                      onChange={(e) => setBoxId(e.target.value)}
                      className="flex-1 font-mono"
                    />
                    <Button className="bg-blue-600 hover:bg-blue-700">Check In</Button>
                  </div>
                </div>
                <div className="p-3 rounded-md bg-green-50 border border-green-200">
                  <div className="font-semibold text-green-800 mb-1">✓ Box found — ORD-4498</div>
                  <div className="text-[11px] text-green-800">
                    <strong>Acme Supplies Ltd</strong> · SW8 1RT · Route A<br />
                    Box 2 of 2 — all boxes now received
                  </div>
                </div>
              </CardContent>
            </Card>
            
            {/* Orders Awaiting Goods */}
            <Card>
              <CardHeader>
                <CardTitle className="text-[13.5px]">Orders Awaiting Goods</CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                <table className="w-full border-collapse">
                  <thead>
                    <tr>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Order ID
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Customer
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Route
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Boxes
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {orders.map((o) => {
                      const badgeColor = o.boxesReceived === o.boxesExpected ? 'green' :
                        o.boxesReceived === 0 ? 'red' : 'amber';
                      const bgColor = o.boxesReceived === o.boxesExpected ? 'bg-green-50' :
                        o.boxesReceived === 0 ? 'bg-red-50' : 'bg-amber-50';
                      return (
                        <tr
                          key={o.orderId}
                          className={`hover:bg-gray-50 cursor-pointer ${bgColor} ${selectedOrder === o.orderId ? 'ring-2 ring-blue-500' : ''}`}
                          onClick={() => setSelectedOrder(o.orderId)}
                        >
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-mono">
                            {o.orderId}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                            {o.customer}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                            {o.routeName}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100">
                            <Badge variant={badgeColor as any}>
                              {o.boxesReceived === o.boxesExpected ? `${o.boxesReceived} / ${o.boxesExpected} ✓` : `${o.boxesReceived} / ${o.boxesExpected}`}
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
          
          {/* Order Detail */}
          {order && (
            <div>
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle className="text-[13.5px]">Order Detail — {order.orderId}</CardTitle>
                  {order.boxesReceived < order.boxesExpected && (
                    <Badge variant="amber">Partial</Badge>
                  )}
                </CardHeader>
                <CardContent>
                  <div className="mb-3 text-[12.5px]">
                    <div><strong>Customer:</strong> {order.customer}</div>
                    <div><strong>Route:</strong> {order.routeName} · London Central</div>
                  </div>
                  <div className="mb-2.5">
                    <div className="font-semibold text-[12px] mb-2">Box Status ({order.boxesExpected} expected)</div>
                    <div className="grid grid-cols-4 gap-2">
                      {order.boxes.map((box) => {
                        const statusColor = box.status === 'received' ? 'green' : 'amber';
                        return (
                          <div
                            key={box.id}
                            className={`border rounded-md p-2 text-center text-[11px] ${
                              box.status === 'received'
                                ? 'border-green-600 bg-green-50'
                                : 'border-amber-600 bg-amber-50'
                            }`}
                          >
                            <div className="text-[18px]">📦</div>
                            <div className="font-mono text-[10px]">{box.id}</div>
                            <div className={`text-[10px] mt-0.5 ${
                              box.status === 'received' ? 'text-green-800' : 'text-amber-800'
                            }`}>
                              {box.status === 'received' ? `✓ ${box.receivedAt}` : 'Awaiting'}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                  <div className="p-2.5 bg-gray-50 rounded text-[12px] text-gray-600 mb-2.5">
                    Boxes {order.boxes.filter(b => b.status === 'received').map(b => b.id.split('-').pop()).join(', ')} will be manifested. Missing boxes will be added to the next {order.routeName} run when received.
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" className="flex-1">Flag Exception</Button>
                    <Button size="sm" className="flex-1 bg-blue-600 hover:bg-blue-700">Ready for Manifest</Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
