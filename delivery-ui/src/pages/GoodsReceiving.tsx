import { useState, useEffect } from 'react';
import { useApp } from '@/contexts/AppContext';
import { orderService } from '@/api/services/orderService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import type { OrderAwaitingGoodsDto } from '@/api/types';

export default function GoodsReceiving() {
  const { selectedDepotId, getDepotById } = useApp();
  const [boxId, setBoxId] = useState('');
  const [selectedOrder, setSelectedOrder] = useState<string | null>(null);
  const [orders, setOrders] = useState<OrderAwaitingGoodsDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [checkingIn, setCheckingIn] = useState(false);
  const [lastCheckedBox, setLastCheckedBox] = useState<{ boxId: string; orderId: string } | null>(null);
  const [exceptionReason, setExceptionReason] = useState('');
  const [showExceptionDialog, setShowExceptionDialog] = useState(false);
  
  useEffect(() => {
    if (!selectedDepotId) {
      setLoading(false);
      return;
    }
    
    const loadOrders = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await orderService.getOrdersAwaitingGoods(selectedDepotId ?? undefined);
        setOrders(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load orders');
      } finally {
        setLoading(false);
      }
    };
    
    loadOrders();
  }, [selectedDepotId]);
  
  const handleCheckIn = async () => {
    if (!boxId.trim()) {
      setError('Please enter a box ID');
      return;
    }
    
    try {
      setCheckingIn(true);
      setError(null);
      setSuccessMessage(null);
      const box = await orderService.receiveBox(boxId.trim());
      
      // Find the order this box belongs to
      const order = orders.find(o => o.boxes.some(b => b.id === box.id));
      if (order) {
        setLastCheckedBox({ boxId: box.id, orderId: order.orderId });
        setSuccessMessage(`Box ${box.id} checked in successfully for order ${order.orderId}`);
        // Reload orders to get updated status
        const updatedOrders = await orderService.getOrdersAwaitingGoods(selectedDepotId ?? undefined);
        setOrders(updatedOrders);
        setBoxId('');
      } else {
        setSuccessMessage(`Box ${box.id} checked in successfully`);
        setBoxId('');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to check in box');
    } finally {
      setCheckingIn(false);
    }
  };
  
  const handleFlagException = async () => {
    if (!selectedOrder) return;
    if (!exceptionReason.trim()) {
      setError('Please provide a reason for the exception');
      return;
    }

    const orderObj = orders.find(o => o.orderId === selectedOrder);
    if (!orderObj) return;

    try {
      setError(null);
      await orderService.flagException(orderObj.id, exceptionReason.trim());
      setSuccessMessage(`Exception flagged for order ${selectedOrder}`);
      setShowExceptionDialog(false);
      setExceptionReason('');
      const updatedOrders = await orderService.getOrdersAwaitingGoods(selectedDepotId ?? undefined);
      setOrders(updatedOrders);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to flag exception');
    }
  };

  const handleReadyForManifest = async () => {
    if (!selectedOrder) return;

    const orderObj = orders.find(o => o.orderId === selectedOrder);
    if (!orderObj) return;

    try {
      setError(null);
      await orderService.markReadyForManifest(orderObj.id);
      setSuccessMessage(`Order ${selectedOrder} marked as ready for manifest`);
      const updatedOrders = await orderService.getOrdersAwaitingGoods(selectedDepotId ?? undefined);
      setOrders(updatedOrders);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to mark order as ready for manifest');
    }
  };
  
  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }
  
  if (error && !selectedDepotId) {
    return <div className="flex items-center justify-center min-h-screen text-red-600">{error}</div>;
  }
  
  const depot = selectedDepotId ? getDepotById(selectedDepotId) : undefined;
  const order = selectedOrder ? orders.find(o => o.orderId === selectedOrder) : orders[0];
  const awaitingCount = orders.filter(o => o.boxesReceived < o.boxesExpected).length;
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">Goods Receiving</div>
          <div className="text-[11.5px] text-gray-500">{depot?.name || 'Depot'} · Check in arriving boxes</div>
        </div>
        {error && (
          <div className="flex items-center gap-1.5 bg-red-50 px-2.5 py-1 rounded text-[12px] text-red-800">
            {error}
          </div>
        )}
        {successMessage && (
          <div className="flex items-center gap-1.5 bg-green-50 px-2.5 py-1 rounded text-[12px] text-green-800">
            {successMessage}
          </div>
        )}
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
                      onChange={(e) => {
                        setBoxId(e.target.value);
                        setError(null);
                        setSuccessMessage(null);
                      }}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' && !checkingIn) {
                          handleCheckIn();
                        }
                      }}
                      className="flex-1 font-mono"
                      placeholder="Scan or enter box ID"
                    />
                    <Button 
                      className="bg-blue-600 hover:bg-blue-700"
                      onClick={handleCheckIn}
                      disabled={checkingIn || !boxId.trim()}
                    >
                      {checkingIn ? 'Checking In...' : 'Check In'}
                    </Button>
                  </div>
                </div>
                {lastCheckedBox && (
                  <div className="p-3 rounded-md bg-green-50 border border-green-200">
                    <div className="font-semibold text-green-800 mb-1">✓ Box {lastCheckedBox.boxId} checked in</div>
                    <div className="text-[11px] text-green-800">
                      Order {lastCheckedBox.orderId}
                    </div>
                  </div>
                )}
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
                      const badgeColor: 'green' | 'red' | 'amber' = o.boxesReceived === o.boxesExpected ? 'green' :
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
                            <Badge variant={badgeColor}>
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
                    {order.boxes.filter(b => b.status === 'received').length > 0 && (
                      <>Boxes {order.boxes.filter(b => b.status === 'received').map(b => b.id.split('-').pop()).join(', ')} will be manifested. </>
                    )}
                    {order.boxesReceived < order.boxesExpected && (
                      <>Missing boxes will be added to the next {order.routeName} run when received.</>
                    )}
                  </div>
                  <div className="flex gap-2">
                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="flex-1"
                      onClick={() => setShowExceptionDialog(true)}
                    >
                      Flag Exception
                    </Button>
                    <Button 
                      size="sm" 
                      className="flex-1 bg-blue-600 hover:bg-blue-700"
                      onClick={handleReadyForManifest}
                    >
                      Ready for Manifest
                    </Button>
                  </div>
                  {showExceptionDialog && (
                    <div className="mt-3 p-3 border border-gray-300 rounded bg-white">
                      <Label className="text-[11px] mb-2 block">Exception Reason</Label>
                      <Input
                        value={exceptionReason}
                        onChange={(e) => setExceptionReason(e.target.value)}
                        placeholder="Enter reason for exception"
                        className="mb-2"
                      />
                      <div className="flex gap-2">
                        <Button 
                          size="sm" 
                          variant="outline"
                          onClick={() => {
                            setShowExceptionDialog(false);
                            setExceptionReason('');
                          }}
                        >
                          Cancel
                        </Button>
                        <Button 
                          size="sm" 
                          className="bg-red-600 hover:bg-red-700"
                          onClick={handleFlagException}
                          disabled={!exceptionReason.trim()}
                        >
                          Flag Exception
                        </Button>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
