# 🧪 Sample Test Project — For Testing Flakewatch

This is a **demo Java project** with intentionally flaky tests. Use it to see Flakewatch detect real flakes.

## What's Inside

| File | Description |
|---|---|
| `Calculator.java` | Simple production code (add, subtract, multiply, divide) |
| `CalculatorTest.java` | ✅ **5 stable tests** — always pass |
| `FlakyTest.java` | 🪲 **3 flaky tests** — randomly pass or fail |
| `run-and-report.sh` | Runs tests + sends results to Flakewatch |

### The Flaky Tests Simulate Real Problems

| Test | Simulates | Why It's Flaky |
|---|---|---|
| `testAsyncProcessing` | Race condition | Depends on CPU timing — sometimes fast, sometimes slow |
| `testRandomDataValidation` | Random test data | Uses `Math.random()` — 50/50 chance |
| `testTimeDependentLogic` | Clock dependency | Passes on even seconds, fails on odd seconds |

## How to Use

### One-time run
```bash
cd sample-test-project
chmod +x run-and-report.sh
./run-and-report.sh
```

### Run 5 times (to trigger flake detection)
```bash
./run-and-report.sh --loop 5
```

### What happens
1. Script runs `mvn test` → some tests pass, some fail randomly
2. Script parses the JUnit XML reports in `target/surefire-reports/`
3. Script builds a JSON payload with real test names and real pass/fail statuses
4. Script sends the payload to Flakewatch via the webhook API
5. After ~3 runs, Flakewatch detects the flaky tests and quarantines them
6. Open `http://localhost:4200` → Dashboard shows quarantined tests!

## Prerequisites

Make sure Flakewatch is running:
```bash
# Terminal 1: Infrastructure
cd /path/to/Flakewatch && docker compose up -d

# Terminal 2: Backend
cd /path/to/Flakewatch && mvn spring-boot:run

# Terminal 3: Frontend  
cd /path/to/Flakewatch/flakewatch-ui && npm start
```
