-- Add index on box identifier for faster lookups
CREATE INDEX IF NOT EXISTS idx_boxes_identifier ON boxes(identifier);
