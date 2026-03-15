export type UserRole = 'central_admin' | 'depot_manager';

export interface CurrentUser {
  name: string;
  role: UserRole;
  initials: string;
  depotId?: string;
}

export interface Depot {
  id: string;
  name: string;
  location: string;
  routesCount: number;
  vehiclesCount: number;
  driversCount: number;
  status: string;
}

export interface Route {
  id: string;
  depotId: string;
  code: string;
  name: string;
  coverage: string;
  postcodeRulesCount: number;
  status: string;
}

export interface DashboardSummary {
  totalRoutes: number;
  deliveriesComplete: number;
  deliveriesTotal: number;
  boxesDelivered: number;
  boxesTotal: number;
  exceptionsCount: number;
}

export interface RouteSummary {
  routeId: string;
  routeName: string;
  description: string;
  vehicle: string;
  driver: string;
  deliveriesDone: number;
  deliveriesTotal: number;
  boxesDone: number;
  boxesTotal: number;
  status: string;
  progressNote?: string;
}

export interface Dashboard {
  date: string;
  summary: DashboardSummary;
  routeSummary: RouteSummary[];
  openExceptions: Array<{ orderId: string; boxesSummary: string; routeName: string }>;
  awaitingGoods: Array<{ orderId: string; expectedBoxes: number; receivedBoxes: number }>;
}

export interface DeliveryStop {
  seq: number;
  address: string;
  postcode: string;
  boxes: number | string;
  status: string;
  deliveryTime?: string;
  hasPod: boolean;
}

export interface RouteDrilldown {
  routeName: string;
  vehicle: string;
  driver: string;
  stats: {
    deliveriesDone: number;
    deliveriesTotal: number;
    boxesDone: number;
    boxesTotal: number;
    exceptionsCount: number;
    lastActivity?: string;
    lastActivityPostcode?: string;
  };
  stops: DeliveryStop[];
}

export interface Box {
  id: string;
  status: 'received' | 'pending' | 'missing';
  receivedAt?: string;
}

export interface OrderAwaitingGoods {
  orderId: string;
  customer: string;
  routeName: string;
  boxesReceived: number;
  boxesExpected: number;
  boxes: Box[];
}

export interface PostcodeHierarchyLevel {
  level: string;
  pattern: string;
  routeName: string;
  isMatch: boolean;
}

export interface PostcodeLookup {
  hierarchy: PostcodeHierarchyLevel[];
}

export interface PostcodeRule {
  pattern: string;
  level: 'full' | 'sector' | 'district' | 'area' | 'letter';
  routeId: string;
  routeName: string;
  effectiveFrom: string;
  effectiveTo?: string;
}

export interface Vehicle {
  id: string;
  depotId: string;
  registration: string;
  makeModel: string;
  capacity: string;
  motDue: string;
  nextService: string;
  status: string;
}

export interface Driver {
  id: string;
  depotId: string;
  name: string;
  licenceNo: string;
  expiry: string;
  contact: string;
  todaysRouteId?: string;
  status: string;
}

export interface ManifestStop {
  orderId: string;
  address: string;
  boxes: number | string;
  boxStatus: string;
}

export interface Manifest {
  id: string;
  routeId: string;
  date: string;
  driverId: string;
  vehicleId: string;
  status: string;
  stops: ManifestStop[];
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole | 'driver';
  depotId?: string;
  lastLogin: string;
  status: string;
}

export interface AuditEvent {
  timestamp: string;
  userId: string;
  userName: string;
  role: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  entityType: string;
  detail: string;
}

export interface MockApiPayload {
  currentUser: CurrentUser;
  depots: Depot[];
  routes: Route[];
  dashboard: Dashboard;
  routeDrilldown: Record<string, RouteDrilldown>;
  ordersAwaitingGoods: OrderAwaitingGoods[];
  postcodeRules: PostcodeRule[];
  vehicles: Vehicle[];
  drivers: Driver[];
  manifests: Manifest[];
  users: User[];
  auditEvents: AuditEvent[];
}
