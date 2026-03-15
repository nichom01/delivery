-- Delivery System Database Schema
-- Version 1: Initial schema

-- Depots table
CREATE TABLE depots (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7)
);

CREATE INDEX idx_depots_name ON depots(name);

-- Routes table
CREATE TABLE routes (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    depot_id VARCHAR(36) NOT NULL REFERENCES depots(id) ON DELETE CASCADE
);

CREATE INDEX idx_routes_depot ON routes(depot_id);
CREATE INDEX idx_routes_code ON routes(code);

-- Vehicles table
CREATE TABLE vehicles (
    id VARCHAR(36) PRIMARY KEY,
    registration VARCHAR(50) NOT NULL UNIQUE,
    make VARCHAR(100),
    model VARCHAR(100),
    capacity VARCHAR(100),
    mot_date DATE,
    last_service_date DATE,
    next_service_due DATE,
    depot_id VARCHAR(36) NOT NULL REFERENCES depots(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_vehicles_depot ON vehicles(depot_id);
CREATE INDEX idx_vehicles_registration ON vehicles(registration);

-- Drivers table
CREATE TABLE drivers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact VARCHAR(255),
    licence_number VARCHAR(100),
    licence_expiry DATE,
    shift_info VARCHAR(255),
    depot_id VARCHAR(36) NOT NULL REFERENCES depots(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_drivers_depot ON drivers(depot_id);

-- Orders table
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    despatch_id VARCHAR(100) NOT NULL,
    customer_address TEXT,
    delivery_postcode VARCHAR(20) NOT NULL,
    order_date DATE,
    requested_delivery_date DATE,
    route_id VARCHAR(36) REFERENCES routes(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    UNIQUE(order_id, despatch_id)
);

CREATE INDEX idx_orders_route ON orders(route_id);
CREATE INDEX idx_orders_postcode ON orders(delivery_postcode);
CREATE INDEX idx_orders_composite ON orders(order_id, despatch_id);

-- Boxes table
CREATE TABLE boxes (
    id VARCHAR(36) PRIMARY KEY,
    identifier VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'EXPECTED',
    received_at TIMESTAMP,
    order_id VARCHAR(36) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    manifest_id VARCHAR(36)
);

CREATE INDEX idx_boxes_order ON boxes(order_id);
CREATE INDEX idx_boxes_manifest ON boxes(manifest_id);
CREATE INDEX idx_boxes_status ON boxes(status);

-- Postcode Rules table
CREATE TABLE postcode_rules (
    id VARCHAR(36) PRIMARY KEY,
    pattern VARCHAR(20) NOT NULL,
    level VARCHAR(20) NOT NULL,
    route_id VARCHAR(36) NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    effective_from DATE NOT NULL,
    effective_to DATE
);

CREATE INDEX idx_postcode_pattern ON postcode_rules(pattern);
CREATE INDEX idx_postcode_level ON postcode_rules(level);
CREATE INDEX idx_postcode_dates ON postcode_rules(effective_from, effective_to);
CREATE INDEX idx_postcode_route ON postcode_rules(route_id);

-- Manifests table
CREATE TABLE manifests (
    id VARCHAR(36) PRIMARY KEY,
    route_id VARCHAR(36) NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    vehicle_id VARCHAR(36) NOT NULL REFERENCES vehicles(id),
    driver_id VARCHAR(36) NOT NULL REFERENCES drivers(id),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    UNIQUE(route_id, date)
);

CREATE INDEX idx_manifests_route ON manifests(route_id);
CREATE INDEX idx_manifests_date ON manifests(date);
CREATE INDEX idx_manifests_vehicle ON manifests(vehicle_id);
CREATE INDEX idx_manifests_driver ON manifests(driver_id);

-- Add foreign key constraint for boxes.manifest_id after manifests table is created
ALTER TABLE boxes ADD CONSTRAINT fk_boxes_manifest FOREIGN KEY (manifest_id) REFERENCES manifests(id) ON DELETE SET NULL;

-- PODs table
CREATE TABLE pods (
    id VARCHAR(36) PRIMARY KEY,
    manifest_id VARCHAR(36) NOT NULL,
    order_id VARCHAR(36) NOT NULL,
    delivery_address TEXT,
    postcode VARCHAR(20),
    image_url TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    uploaded_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_pods_manifest ON pods(manifest_id);
CREATE INDEX idx_pods_order ON pods(order_id);

-- Users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    depot_id VARCHAR(36) REFERENCES depots(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_login TIMESTAMP
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_depot ON users(depot_id);
CREATE INDEX idx_users_role ON users(role);

-- Audit Events table
CREATE TABLE audit_events (
    id VARCHAR(36) PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(36),
    depot_id VARCHAR(36),
    before_value TEXT,
    after_value TEXT,
    detail TEXT
);

CREATE INDEX idx_audit_user ON audit_events(user_id);
CREATE INDEX idx_audit_depot ON audit_events(depot_id);
CREATE INDEX idx_audit_timestamp ON audit_events(timestamp);
CREATE INDEX idx_audit_entity ON audit_events(entity_type, entity_id);

-- Seed A-Z postcode fallback rules
-- These will be created programmatically, but we can add a comment here
-- Note: A-Z fallback rules should be seeded via application initialization
