-- Driver location samples (mobile app ingest; v1: user + coordinates + timestamps only)

CREATE TABLE driver_location_samples (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    latitude NUMERIC(11, 8) NOT NULL,
    longitude NUMERIC(11, 8) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_driver_location_samples_user ON driver_location_samples(user_id);
CREATE INDEX idx_driver_location_samples_received ON driver_location_samples(received_at);
