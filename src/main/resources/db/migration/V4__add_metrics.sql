-- V4: Add daily test metrics for aggregation

CREATE TABLE daily_test_metrics (
    id BIGSERIAL PRIMARY KEY,
    test_metadata_id BIGINT NOT NULL REFERENCES test_metadata(id),
    metric_date DATE NOT NULL,
    total_runs INT NOT NULL DEFAULT 0,
    total_failures INT NOT NULL DEFAULT 0,
    flake_flips INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_test_date UNIQUE (test_metadata_id, metric_date)
);

CREATE INDEX idx_daily_metrics_date ON daily_test_metrics (metric_date);
