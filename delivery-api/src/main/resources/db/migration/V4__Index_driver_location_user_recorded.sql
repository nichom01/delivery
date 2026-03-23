CREATE INDEX idx_driver_location_samples_user_recorded
    ON driver_location_samples (user_id, recorded_at);
