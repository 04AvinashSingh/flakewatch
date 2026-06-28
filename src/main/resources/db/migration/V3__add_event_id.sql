-- V3: Add event_id for idempotency

ALTER TABLE test_runs ADD COLUMN event_id VARCHAR(100);

-- Backfill existing data with random UUIDs so we can set it to NOT NULL
UPDATE test_runs SET event_id = gen_random_uuid()::text WHERE event_id IS NULL;

ALTER TABLE test_runs ALTER COLUMN event_id SET NOT NULL;

-- Add the unique constraint to enforce idempotency
ALTER TABLE test_runs ADD CONSTRAINT uk_test_runs_event_id UNIQUE (event_id);
