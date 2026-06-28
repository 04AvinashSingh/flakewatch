-- Users Table (For Webhook Auth / Dashboard Access)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Metadata Table (The overall definition of a test)
CREATE TABLE test_metadata (
    id BIGSERIAL PRIMARY KEY,
    test_identifier VARCHAR(500) UNIQUE NOT NULL, -- e.g. "com.example.UserServiceTest.testLogin"
    suite_name VARCHAR(255) NOT NULL,
    is_quarantined BOOLEAN DEFAULT FALSE,
    flake_score INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test Runs Table (Historical executions)
CREATE TABLE test_runs (
    id BIGSERIAL PRIMARY KEY,
    test_metadata_id BIGINT NOT NULL REFERENCES test_metadata(id),
    status VARCHAR(20) NOT NULL, -- PASS, FAIL, SKIPPED
    commit_hash VARCHAR(40) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    duration_ms BIGINT,
    executed_at TIMESTAMP NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_test_runs_commit ON test_runs(commit_hash);
CREATE INDEX idx_test_runs_test_metadata ON test_runs(test_metadata_id);
