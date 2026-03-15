# DELIVERY VAN MANAGEMENT SYSTEM
## Product Requirements Document
**Version 1.0 | Draft**  
March 2026

---

## 1. System Overview

This document describes the requirements for a Delivery Van Management System designed to support the end-to-end operation of goods delivery via delivery vans across a multi-depot network. The system manages the full delivery lifecycle from order receipt through to proof of delivery, supporting depot managers, central administrators, and (in a future phase) drivers via a mobile application.

### 1.1 Objectives

- Provide a centralised platform to manage vehicles, drivers, routes and depots
- Automate the allocation of incoming orders to routes using postcode-based routing rules
- Support the manifestation of confirmed goods onto delivery runs
- Track delivery progress in real time and capture proof of delivery
- Maintain a full audit trail of all configuration changes and operational activity

### 1.2 Scope — Phase 1

- Core data maintenance: depots, routes, vehicles, drivers
- Postcode-to-route routing configuration with hierarchical fallback
- Order ingestion via manual entry and API endpoint
- Box-level goods receiving and exception handling
- Manifest creation and confirmation
- Operational reporting and live depot dashboard
- User management with role-based access control

### 1.3 Out of Scope — Future Phases

- Mobile driver application (manifest download, POD capture, delivery confirmation)
- Delivery stop ordering and ETA calculations
- Customer notifications and tracking
- Returns management
- Financial processing and invoicing

---

## 2. Core Data Entities

### 2.1 Entity Overview

The system is built around the following core entities and their relationships:

| Entity        | Key Relationships     | Description |
|---------------|-----------------------|-------------|
| Depot         | Has many Routes       | A physical dispatch location. All routes and operational data are scoped to a depot. |
| Route         | Belongs to one Depot  | A named delivery run operated from a depot. Routes are permanently fixed to their depot. |
| Postcode Rule | Maps to a Route       | Defines which route a postcode (or postcode prefix) is allocated to, with effective date range. |
| Vehicle       | Belongs to a Depot    | A delivery van with registration, capacity and maintenance details. |
| Driver        | Belongs to a Depot    | A driver with personal, licence and contact details. |
| Order         | Allocated to a Route  | An incoming delivery order identified by Order ID and Despatch ID, containing one or more boxes. |
| Box           | Belongs to an Order   | The atomic unit of delivery. Each box is tracked individually from receipt through to delivery. |
| Manifest      | Route / Vehicle / Driver | A confirmed delivery run for a specific date, vehicle and driver. Contains manifested boxes. |
| POD           | Belongs to a delivery stop | Proof of delivery image with timestamp, captured per delivery address. |

### 2.2 Depot

- Unique identifier, name, address and geographic location
- 25+ depots in the network
- All routes, vehicles, drivers and operational data are scoped to a depot
- Depot boundaries are defined implicitly by the postcode-to-route mappings

### 2.3 Route

- Name / code, description, associated depot
- Routes are permanently fixed to a depot — a route is never reassigned to a different depot
- To move delivery coverage between depots, postcode rules are reassigned to routes in the target depot

### 2.4 Vehicle

- Registration number, make, model
- Capacity details (weight and/or volume)
- MOT date, last service date, next service due
- Assigned depot

### 2.5 Driver

- Name, contact details
- Driving licence details and expiry
- Assigned depot
- Shift / availability information

---

## 3. Postcode Routing

### 3.1 Hierarchical Matching

The system allocates an incoming order to a route by matching its delivery postcode against a hierarchical set of rules. The system uses a **longest-match-wins** approach — the most specific matching rule takes precedence. This means the operator only needs to maintain exceptions at a granular level, relying on broader rules for the majority of deliveries.

The matching hierarchy, from most specific to least specific, is:

| Priority | Level       | Example   | Notes |
|----------|-------------|-----------|-------|
| 1        | Full Postcode | SW1A 1AA | Highest priority. Overrides all broader rules. |
| 2        | Sector      | SW1A 1    | Matches all postcodes in the sector unless overridden. |
| 3        | District    | SW1A      | Matches all postcodes in the district unless overridden. |
| 4        | Area        | SW        | Matches all postcodes in the area unless overridden. |
| 5        | First Letter | S        | Guaranteed fallback. System is seeded with A–Z entries at setup. |

Since every UK postcode begins with a letter, the single-letter fallback guarantees that every postcode will always resolve to a route. The system is seeded with default route assignments for each letter A–Z at initial setup.

### 3.2 Effective Date Management

Every postcode rule has an effective date range (effective from / effective to). This enables:

- **Full historical audit** — the system can reconstruct which route any postcode would have resolved to on any given date
- **Planned boundary changes** — future-dated rules can be created in advance
- The current active rule for any postcode is the one with the most specific match and an effective date range covering today

When a postcode is reassigned to a different route, the existing rule is closed with an effective-to date and a new rule is created with the new route assignment. **Rules are never deleted.**

### 3.3 Depot Boundary Management

Route-depot assignments are permanent. To move delivery coverage from one depot to another, the relevant postcode rules are reassigned to routes belonging to the target depot. Depot managers may make postcode rule changes within their own depot's routes, and all such changes are fully audited.

---

## 4. Order Ingestion

### 4.1 Order Identification

Each order is uniquely identified by a **composite key** of Order ID and Despatch ID. This combination must be unique across the system. Duplicate submissions (identical Order ID + Despatch ID) are rejected with a clear error response, providing an idempotency guarantee for external systems.

### 4.2 Ingestion Methods

**Manual Entry**

- Authorised users (depot managers and central admin) can enter orders directly via the system UI
- The manual entry form validates the same rules as the API
- The UI resolves and displays the allocated route at point of entry

**API Endpoint**

- A REST API endpoint accepts orders from external systems (e.g. order management, warehouse systems)
- External systems authenticate using API keys
- The endpoint returns a clear success or failure response including the allocated route on success, or a validation error on failure
- If the delivery postcode cannot be resolved to a route (should not occur given the A–Z fallback), the order is flagged for manual review

### 4.3 Order Data

- Order ID and Despatch ID (composite unique key)
- Customer / delivery address details
- Delivery postcode (used for route allocation)
- Expected box count and box identifiers (if available from source system)
- Order date and requested delivery date

---

## 5. Delivery Lifecycle

### 5.1 End-to-End Flow

The delivery lifecycle progresses through the following stages:

| Step | Stage              | Description |
|------|--------------------|-------------|
| 1    | Order Received     | Order arrives via manual entry or API. Postcode is resolved to a route. Order status is Pending. |
| 2    | Goods Arriving     | Physical goods arrive at the depot, potentially from multiple source locations over time. Each arrival is checked at box level. |
| 3    | Goods Reconciliation | Arriving boxes are matched against expected boxes for each order. Order status updates as boxes are received (e.g. 2 of 3 boxes received). |
| 4    | Final Goods Arrival | When the final source delivery arrives, all boxes are reconciled. The manifest can now be confirmed. |
| 5    | Manifest Creation  | Available boxes are assigned to a vehicle and driver for a specific route and date. The manifest is confirmed and locked. |
| 6    | Vehicle Departure  | The vehicle departs with the confirmed manifest. |
| 7    | Delivery           | Driver delivers boxes to each address. POD is captured per delivery stop (future mobile phase). |
| 8    | Completion        | All deliveries confirmed. Route marked complete. |

### 5.2 Box-Level Tracking

The box is the atomic unit of the delivery system. Each box is tracked individually through its lifecycle:

- **Expected** — box is anticipated based on order data
- **Received** — box has physically arrived at the depot and been checked in
- **Manifested** — box has been assigned to a manifest for delivery
- **Delivered** — box has been delivered to the customer (confirmed via mobile POD — future phase)
- **Exception** — box was expected but has not arrived

### 5.3 Exception Handling — Missing Boxes

- When a manifest is confirmed, any boxes that have not yet arrived are flagged as exceptions
- The business rule is to **always deliver available boxes** — a missing box does not hold back other boxes in the same order
- When a missing box subsequently arrives at the depot, it is automatically allocated to the original route for the next available delivery run
- The missing box retains its link to the original order throughout

### 5.4 Manifest

- One manifest per route per day
- Attributes: route, vehicle, driver, date, status (draft / confirmed / in progress / complete)
- Manifest is confirmed when all expected goods have been received and reconciled
- Once confirmed, the manifest is **locked** — changes require an audited override process
- Manifest records the exact boxes loaded, enabling reconciliation against deliveries

### 5.5 Proof of Delivery (Future Mobile Phase)

- POD is captured once per delivery stop (covering all boxes at that address)
- POD consists of a photographic image captured by the driver's mobile device
- Timestamp is recorded at point of capture on the device (not at point of upload)
- POD is uploaded to the system when mobile connectivity is available
- POD image and timestamp are stored against the delivery record and accessible from the reporting dashboard

---

## 6. User Roles & Permissions

### 6.1 Role Summary

| Role           | Access Scope                    | Capabilities |
|----------------|----------------------------------|--------------|
| Central Admin  | All depots — full system access | All configuration, all data, all reporting, user management across all depots, full audit log visibility |
| Depot Manager  | Own depot only — full depot access | All depot configuration, route and postcode maintenance, vehicle and driver management, order entry, manifest management, depot reporting. All changes audited. |
| Driver (Future Phase) | Own data only — read-only | View own profile, view allocated manifest for the day, capture and submit POD |

### 6.2 Data Scoping

- Every data entity in the system is depot-scoped
- Depot managers can only view and modify data belonging to their own depot — this is enforced at the **data layer**, not just the UI
- Central admin can view and modify data across all depots
- When a depot manager logs in, all screens and reports default to their depot context

### 6.3 Depot Manager Permissions Detail

Depot managers have full autonomy over their depot's operational configuration, subject to audit:

- Create and manage driver accounts for their depot
- Add and manage vehicles assigned to their depot
- Maintain postcode-to-route mappings for routes belonging to their depot
- Create, review and confirm manifests
- Enter orders manually
- View depot reporting and dashboard

### 6.4 Authentication

- **UI access:** username and password authentication
- **API access:** API key authentication for external system integrations
- API keys are managed by central admin and are separate from user accounts

---

## 7. Audit Trail

### 7.1 Audit Requirements

All changes to configuration and operational data are audited. The audit trail is a **system-wide requirement** and is not optional. The following is captured for every change:

- **What** was changed (entity, field, before value, after value)
- **Who** made the change (user account)
- **What role** the user held at the time of the change
- **When** the change was made (timestamp)
- **Which depot** the change relates to

### 7.2 Audit Access

- Central admin can view the full audit log across all depots
- Depot managers can view the audit log for their own depot
- Audit records are **read-only** — they cannot be modified or deleted

### 7.3 Postcode Rule History

In addition to the system-wide audit trail, postcode-to-route rules maintain their own history via effective date ranges. This enables full reconstruction of routing decisions for any historical date, independent of the audit log.

---

## 8. Reporting & Dashboard

### 8.1 Depot Daily Monitor

The primary operational report is a depot-level daily delivery monitor. It provides a real-time view of delivery progress for a selected depot on a selected date (defaulting to today).

**Filters**

- **Depot** — central admin can switch between depots; depot managers default to their own depot
- **Date** — defaults to today

**Summary View — One Row Per Route**

| Route   | Vehicle  | Driver   | Deliveries     | Boxes      | Progress   |
|---------|----------|----------|----------------|------------|------------|
| Route A | AB12 XYZ | J. Smith | 23 deliveries  | 120 boxes  | 67% / 54%  |
| Route B | CD34 LMN | P. Jones | 18 deliveries  | 84 boxes   | 100% / 100%|

Progress is shown as two percentages: **deliveries completed (%)** and **boxes delivered (%)**, for example 67% / 54%. For historical dates, these figures represent final completion rates.

### 8.2 Route Drill Down

Selecting a route from the summary view opens a detailed view showing all delivery stops for that route on that date.

**Detail View — One Row Per Delivery Stop**

| Address / Postcode       | Boxes | Status    | Delivery Time | POD        |
|--------------------------|-------|-----------|---------------|------------|
| 123 High Street, SW1A 1AA | 3     | Delivered | 10:34         | View image |
| 45 Park Road, SW1A 2BB   | 1     | Delivered | 10:52         | View image |
| 67 Church Lane, SW1B 1CC | 2     | Pending   | —             | —          |

- POD link is only shown once a POD image has been submitted by the driver (future mobile phase)
- Delivery time is the timestamp recorded on the driver's device at point of delivery
- For today's date, the view updates in near real time as drivers complete deliveries and submit PODs

---

## 9. Technical Considerations

### 9.1 Architecture Principles

- The system should be built **API-first** — all functionality exposed through a well-defined API layer, consumed by both the web UI and future mobile clients
- Data is depot-scoped at the data layer, not just the presentation layer
- The postcode routing lookup must be **performant** — it is called on every order ingestion
- The system must support concurrent users across 25+ depots

### 9.2 Postcode Routing Implementation

- Implement as a longest-match lookup — analogous to IP routing table lookups
- Index postcode rules for fast lookup by postcode prefix at each level of the hierarchy
- Always evaluate against the active rule set for the relevant date
- At system initialisation, seed single-letter fallback rules (A–Z) to guarantee all postcodes resolve

### 9.3 API Design

- RESTful API with JSON request and response bodies
- Order ingestion endpoint must return: success with allocated route details, or failure with specific validation errors
- Duplicate order (same Order ID + Despatch ID) returns a clear rejection response
- API key authentication for external system integrations
- API versioning to support future changes without breaking existing integrations

### 9.4 Mobile Readiness (Future Phase)

- All manifest and delivery data exposed via API endpoints suitable for mobile consumption
- POD image upload endpoint must support: offline timestamping (timestamp set on device), upload when connectivity restored, idempotent upload (retry safe)
- Manifest download should support offline caching on device
- Design data sync model now to avoid costly rework when mobile phase begins

### 9.5 Image Storage

- POD images require scalable object storage (e.g. cloud blob storage)
- Images are linked to delivery records by reference — store URL/path, not binary in database
- Consider image retention policy in line with operational and legal requirements

### 9.6 Audit Implementation

- Audit logging should be implemented at the **service/application layer**, not via database triggers, to capture user context
- Audit table should be **append-only** with no update or delete operations permitted
- Consider separate audit log storage for long-term retention

---

## 10. Future Phases

| Feature                 | Description |
|-------------------------|-------------|
| Mobile Driver App       | Native or mobile web application for drivers. Downloads daily manifest when available, supports offline use, captures POD image per delivery stop with device timestamp, confirms boxes delivered, syncs back to centre when connectivity restored. |
| Delivery Ordering & ETAs | Optimised stop ordering within a route. ETA calculation and communication based on delivery sequence and real-time progress. |
| Customer Notifications  | Automated notifications to customers (SMS/email) with delivery updates, ETAs and confirmation of delivery. |
| Returns Management      | Process for capturing and managing returned goods at point of delivery or at depot. |
| Financial Processing   | Delivery charge calculation, invoicing and financial reporting. |

---

## 11. Open Items & Assumptions

### 11.1 Assumptions Made

- UK postcode format is assumed throughout the routing hierarchy
- A route belongs to exactly one depot and this relationship is permanent
- A single-letter fallback rule (A–Z) will be seeded at system initialisation, guaranteeing all postcodes resolve to a route
- The manifest is confirmed when all expected source deliveries have arrived — the system will need a mechanism to indicate when the final source delivery has been received
- POD is captured once per delivery stop, not once per box
- Progress percentages on the dashboard are calculated independently for deliveries and boxes

### 11.2 Items to Confirm

- Exact vehicle data fields required (beyond registration, make, model, capacity, MOT/service dates)
- Driver data fields required (beyond name, contact, licence)
- Order data fields required from external systems — a formal API schema to be agreed with integration partners
- Image retention policy for POD images
- Whether depot managers require approval workflow for postcode boundary changes, or whether audit trail alone is sufficient oversight
- Password policy and session management requirements
- System availability and performance SLAs
