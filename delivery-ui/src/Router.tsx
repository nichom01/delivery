import { Routes, Route, Navigate } from 'react-router-dom';
import { DeliverOpsLayout } from './components/deliverops-layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import DayPlan from './pages/DayPlan';
import RouteDrilldown from './pages/RouteDrilldown';
import OrderEntry from './pages/OrderEntry';
import GoodsReceiving from './pages/GoodsReceiving';
import ManifestBuilder from './pages/ManifestBuilder';
import RoutesPage from './pages/RoutesPage';
import PostcodeRules from './pages/PostcodeRules';
import Vehicles from './pages/Vehicles';
import Drivers from './pages/Drivers';
import Depots from './pages/Depots';
import Users from './pages/Users';
import AuditLog from './pages/AuditLog';
import NotMatch from './pages/NotMatch';
import { useAuth } from './hooks/useAuth';

function ProtectedLayout() {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <DeliverOpsLayout />;
}

export default function Router() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<ProtectedLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="dashboard/routes/:routeId" element={<RouteDrilldown />} />
        <Route path="day-plan" element={<DayPlan />} />
        <Route path="orders/new" element={<OrderEntry />} />
        <Route path="receiving" element={<GoodsReceiving />} />
        <Route path="manifests" element={<ManifestBuilder />} />
        <Route path="manifests/:manifestId" element={<ManifestBuilder />} />
        <Route path="depot/routes" element={<RoutesPage />} />
        <Route path="depot/postcodes" element={<PostcodeRules />} />
        <Route path="depot/vehicles" element={<Vehicles />} />
        <Route path="depot/drivers" element={<Drivers />} />
        <Route path="depots" element={<Depots />} />
        <Route path="users" element={<Users />} />
        <Route path="depot/audit" element={<AuditLog />} />
        <Route path="*" element={<NotMatch />} />
      </Route>
    </Routes>
  );
}
