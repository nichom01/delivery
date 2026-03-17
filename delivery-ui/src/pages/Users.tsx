import { useState } from 'react';
import { useApp } from '@/contexts/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

export default function Users() {
  const { data } = useApp();
  const [activeTab, setActiveTab] = useState('users');
  
  if (!data) {
    return <div>Loading...</div>;
  }
  
  return (
    <>
      <div className="h-[52px] bg-white border-b border-gray-200 flex items-center px-6 gap-3 shrink-0">
        <div className="flex-1">
          <div className="text-[16px] font-bold text-gray-900">User Management</div>
          <div className="text-[11.5px] text-gray-500">Central Admin · All users across all depots</div>
        </div>
        <Button size="sm" className="bg-blue-600 hover:bg-blue-700">+ Invite User</Button>
      </div>
      
      <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
        <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
          <TabsList className="mb-4">
            <TabsTrigger value="users">System Users</TabsTrigger>
            <TabsTrigger value="api-keys">API Keys</TabsTrigger>
            <TabsTrigger value="pending">Pending Invites</TabsTrigger>
          </TabsList>
          
          <TabsContent value="users">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="text-[13.5px]">All Users ({data.users.length})</CardTitle>
                <div className="flex items-center gap-2">
                  <Input placeholder="Search…" className="text-[12px] px-2.5 py-1 w-[160px]" />
                  <select className="text-[12px] px-2 py-1 border border-gray-300 rounded">
                    <option>All roles</option>
                    <option>Central Admin</option>
                    <option>Depot Manager</option>
                    <option>Driver</option>
                  </select>
                  <select className="text-[12px] px-2 py-1 border border-gray-300 rounded">
                    <option>All depots</option>
                    {data.depots.map(d => (
                      <option key={d.id}>{d.name}</option>
                    ))}
                  </select>
                </div>
              </CardHeader>
              <CardContent className="p-0">
                <table className="w-full border-collapse">
                  <thead>
                    <tr>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Name
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Email
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Role
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Depot
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Last Login
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50">
                        Status
                      </th>
                      <th className="text-left text-[10.5px] font-bold text-gray-500 uppercase tracking-wide px-3.5 py-2 border-b-2 border-gray-100 bg-gray-50"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.users.map((user) => {
                      const depot = user.depotId ? data.depots.find(d => d.id === user.depotId) : null;
                      const roleBadgeVariant: 'purple' | 'blue' | 'grey' =
                        user.role === 'CENTRAL_ADMIN' ? 'purple' :
                        user.role === 'DEPOT_MANAGER' ? 'blue' : 'grey';
                      return (
                        <tr key={user.id} className="hover:bg-gray-50">
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700 font-semibold">
                            {user.name}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                            {user.email}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100">
                            <Badge variant={roleBadgeVariant}>
                              {user.role === 'CENTRAL_ADMIN' ? 'Central Admin' :
                               user.role === 'DEPOT_MANAGER' ? 'Depot Manager' : 'Driver'}
                            </Badge>
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                            {depot?.name || 'All depots'}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100 text-[12.5px] text-gray-700">
                            {user.lastLogin}
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100">
                            <Badge variant="green">{user.status}</Badge>
                          </td>
                          <td className="px-3.5 py-2 border-b border-gray-100">
                            <span className="text-blue-600 hover:underline text-[12px] cursor-pointer">Edit</span>
                          </td>
                        </tr>
                      );
                    })}
                    {data.users.length > 4 && (
                      <tr>
                        <td colSpan={7} className="px-3.5 py-1.5 text-center text-[11px] text-gray-500 bg-gray-50">
                          … {data.users.length - 4} more users …
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="api-keys">
            <Card>
              <CardContent className="p-5">
                <p className="text-gray-600">API Keys management coming soon...</p>
              </CardContent>
            </Card>
          </TabsContent>
          
          <TabsContent value="pending">
            <Card>
              <CardContent className="p-5">
                <p className="text-gray-600">Pending invites coming soon...</p>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </>
  );
}
