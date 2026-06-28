-- V2: Add indexes for pagination and search performance

CREATE INDEX IF NOT EXISTS idx_test_metadata_identifier ON test_metadata (test_identifier);
CREATE INDEX IF NOT EXISTS idx_test_metadata_suite ON test_metadata (suite_name);
CREATE INDEX IF NOT EXISTS idx_test_metadata_quarantined ON test_metadata (is_quarantined);
CREATE INDEX IF NOT EXISTS idx_test_metadata_flake_score ON test_metadata (flake_score);
