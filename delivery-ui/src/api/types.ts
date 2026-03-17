// API Types matching backend DTOs

export type UserRole = 'CENTRAL_ADMIN' | 'DEPOT_MANAGER';

// Auth
export interface CurrentUserDto {
  name: string;
  role: string;
  initials: string;
  depotId?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: CurrentUserDto;
}

// Depots
export interface DepotDto {
  id: string;
  name: string;
  location: string;
  routesCount: number;
  vehiclesCount: number;
  driversCount: number;
  status: string;
}

export interface CreateDepotRequest {
  name: string;
  address: string;
  latitude?: string;
  longitude?: string;
}

export interface UpdateDepotRequest {
  name?: string;
  address?: string;
  latitude?: string;
  longitude?: string;
  status?: string;
}

// Routes
export interface RouteDto {
  id: string;
  depotId: string;
  code: string;
  name: string;
  coverage: string;
  postcodeRulesCount: number;
  status: string;
}

// Dashboard
export interface DashboardSummaryDto {
  totalRoutes: number;
  deliveriesComplete: number;
  deliveriesTotal: number;
  boxesDelivered: number;
  boxesTotal: number;
  exceptionsCount: number;
}

export interface RouteSummaryDto {
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
  plannedOrders?: number;
  plannedBoxes?: number;
  awaitingGoodsOrders?: number;
  awaitingGoodsBoxes?: number;
}

export interface ExceptionDto {
  orderId: string;
  boxesSummary: string;
  routeName: string;
}

export interface DashboardDto {
  date: string;
  summary: DashboardSummaryDto;
  routeSummary: RouteSummaryDto[];
  openExceptions: ExceptionDto[];
  awaitingGoods: OrderAwaitingGoodsDto[];
}

// Route Drilldown
export interface RouteStatsDto {
  deliveriesDone: number;
  deliveriesTotal: number;
  boxesDone: number;
  boxesTotal: number;
  exceptionsCount: number;
  lastActivity?: string;
  lastActivityPostcode?: string;
}

export interface DeliveryStopDto {
  seq: number;
  address: string;
  postcode: string;
  boxes: number | string;
  status: string;
  deliveryTime?: string;
  hasPod: boolean;
}

export interface RouteDrilldownDto {
  routeName: string;
  vehicle: string;
  driver: string;
  stats: RouteStatsDto;
  stops: DeliveryStopDto[];
}

// Orders
export interface CreateOrderRequest {
  orderId: string;
  despatchId: string;
  customerAddress?: string;
  deliveryPostcode: string;
  orderDate?: string;
  requestedDeliveryDate?: string;
  boxIdentifiers?: string[];
  expectedBoxCount?: number;
}

export interface BoxDto {
  id: string;
  status: 'received' | 'pending' | 'missing';
  receivedAt?: string;
}

export interface OrderAwaitingGoodsDto {
  orderId: string;
  customer: string;
  routeName: string;
  boxesReceived: number;
  boxesExpected: number;
  boxes: BoxDto[];
}

export interface OrderDto {
  id: string;
  orderId: string;
  despatchId: string;
  customerAddress?: string;
  deliveryPostcode: string;
  status: string;
  exceptionReason?: string;
  readyForManifest?: boolean;
}

// Postcode Rules
export interface PostcodeHierarchyLevelDto {
  level: string;
  pattern: string;
  routeName: string;
  isMatch: boolean;
}

export interface PostcodeLookupDto {
  hierarchy: PostcodeHierarchyLevelDto[];
}

export interface PostcodeRuleDto {
  id?: string;
  pattern: string;
  level: 'full' | 'sector' | 'district' | 'area' | 'letter';
  routeId: string;
  routeName: string;
  effectiveFrom: string;
  effectiveTo?: string;
}

export interface CreatePostcodeRuleRequest {
  pattern: string;
  level: 'full' | 'sector' | 'district' | 'area' | 'letter';
  routeId: string;
  effectiveFrom: string;
  effectiveTo?: string;
}

// Vehicles
export interface VehicleDto {
  id: string;
  depotId: string;
  registration: string;
  makeModel: string;
  capacity: string;
  motDue: string;
  nextService: string;
  status: string;
}

// Drivers
export interface DriverDto {
  id: string;
  depotId: string;
  name: string;
  licenceNo: string;
  expiry: string;
  contact: string;
  todaysRouteId?: string;
  status: string;
}

// Manifests
export interface ManifestStopDto {
  orderId: string;
  address: string;
  boxes: number | string;
  boxStatus: string;
}

export interface ManifestDto {
  id: string;
  routeId: string;
  date: string;
  driverId: string;
  vehicleId: string;
  status: string;
  stops: ManifestStopDto[];
}

export interface CreateManifestRequest {
  routeId: string;
  date: string;
  vehicleId: string;
  driverId: string;
  boxIds?: string[];
}

export interface UpdateManifestRequest {
  driverId?: string;
  vehicleId?: string;
  date?: string;
}

export interface CreateRouteRequest {
  code: string;
  name: string;
  description?: string;
  depotId: string;
}

export interface UpdateRouteRequest {
  name?: string;
  description?: string;
}

export interface UpdatePostcodeRuleRequest {
  pattern?: string;
  level?: 'full' | 'sector' | 'district' | 'area' | 'letter';
  routeId?: string;
  effectiveFrom?: string;
  effectiveTo?: string | null;
}

// Users
export interface UserDto {
  id: string;
  name: string;
  email: string;
  role: string;
  depotId?: string;
  lastLogin: string;
  status: string;
}

// Audit
export interface AuditEventDto {
  timestamp: string;
  userId: string;
  userName: string;
  role: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  entityType: string;
  detail: string;
}

// Day Plan
export interface DayPlanOrderDto {
  id: string;
  orderId: string;
  customerAddress: string;
  deliveryPostcode: string;
  orderStatus: string;
  totalBoxes: number;
  boxesExpected: number;
  boxesReceived: number;
  boxesReady: number;
}

export interface DayPlanRouteDto {
  routeId: string;
  routeCode: string;
  routeName: string;
  totalOrders: number;
  totalBoxes: number;
  ordersFullyReceived: number;
  ordersPartiallyReceived: number;
  ordersNotYetReceived: number;
  manifestStatus: string;
  vehicleRegistration: string | null;
  driverName: string | null;
  orders: DayPlanOrderDto[];
}

export interface DayPlanDto {
  date: string;
  depotId: string;
  depotName: string;
  totalOrdersOnDay: number;
  totalBoxesOnDay: number;
  routes: DayPlanRouteDto[];
}

// Reroute
export interface RerouteOrderRequest {
  routeId: string;
  reason: string;
}

export interface RerouteResultDto {
  id: string;
  code: string;
  name: string;
  depotId: string;
}
