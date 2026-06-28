#!/bin/bash
# ============================================================================
# Flakewatch — Universal Test Reporter Script
# ============================================================================
# This script works with ANY CI/CD system. Just run it after your tests.
#
# HOW TO USE:
# 1. Set environment variables:
#    export FLAKEWATCH_URL="http://your-flakewatch-server:8080"
#    export FLAKEWATCH_API_KEY="your_api_key_here"
#
# 2. Run your tests first, then run this script:
#    mvn test           # (or: npm test, pytest, go test, etc.)
#    ./report-to-flakewatch.sh
#
# 3. That's it! Check your Flakewatch dashboard for results.
# ============================================================================

set -euo pipefail

# ---- Configuration ----
FLAKEWATCH_URL="${FLAKEWATCH_URL:-http://localhost:8080}"
FLAKEWATCH_API_KEY="${FLAKEWATCH_API_KEY:-fw_test_1234567890abcdef}"

# ---- Auto-detect Git info ----
COMMIT_HASH=$(git rev-parse HEAD 2>/dev/null || echo "unknown")
BRANCH_NAME=$(git branch --show-current 2>/dev/null || echo "unknown")
RUNNER_ID=$(hostname)
EVENT_ID=$(uuidgen 2>/dev/null || cat /proc/sys/kernel/random/uuid 2>/dev/null || echo "evt-$(date +%s)")

echo "╔══════════════════════════════════════════════════╗"
echo "║       Flakewatch — Test Result Reporter          ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║  Server:  ${FLAKEWATCH_URL}"
echo "║  Commit:  ${COMMIT_HASH:0:8}..."
echo "║  Branch:  ${BRANCH_NAME}"
echo "║  Runner:  ${RUNNER_ID}"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# ---- Build payload ----
# 📝 CUSTOMIZE THIS: Replace the test names and statuses below 
#    with your actual test results.
#
# In a production setup, you'd parse JUnit XML / pytest JSON / Jest JSON
# and build this payload dynamically.

PAYLOAD=$(cat <<EOF
{
  "eventId": "${EVENT_ID}",
  "commitHash": "${COMMIT_HASH}",
  "branchName": "${BRANCH_NAME}",
  "runnerId": "${RUNNER_ID}",
  "results": [
    {
      "testIdentifier": "com.example.UserServiceTest.testLogin",
      "suiteName": "UserServiceTest",
      "status": "PASS",
      "durationMs": 1200
    },
    {
      "testIdentifier": "com.example.UserServiceTest.testSignup",
      "suiteName": "UserServiceTest",
      "status": "FAIL",
      "durationMs": 800,
      "errorMessage": "Expected status 200 but got 500"
    },
    {
      "testIdentifier": "com.example.PaymentTest.testCheckout",
      "suiteName": "PaymentTest",
      "status": "PASS",
      "durationMs": 2500
    },
    {
      "testIdentifier": "com.example.PaymentTest.testRefund",
      "suiteName": "PaymentTest",
      "status": "PASS",
      "durationMs": 900
    }
  ]
}
EOF
)

# ---- Send to Flakewatch ----
echo "📡 Sending ${PAYLOAD}" | python3 -c "import sys,json; d=json.load(sys.stdin.buffer if hasattr(sys.stdin,'buffer') else sys.stdin); print(f'📡 Sending {len(d.get(\"results\",[]))} test results to Flakewatch...')" 2>/dev/null || echo "📡 Sending test results to Flakewatch..."

RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "${FLAKEWATCH_URL}/api/v1/ingest/test-results" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: ${FLAKEWATCH_API_KEY}" \
  -d "${PAYLOAD}")

echo ""
if [ "$RESPONSE" -eq 200 ]; then
  echo "✅ Success! Results sent to Flakewatch."
  echo "   View your dashboard: ${FLAKEWATCH_URL/8080/4200}"
elif [ "$RESPONSE" -eq 401 ]; then
  echo "❌ Authentication failed (HTTP 401)."
  echo "   Check your FLAKEWATCH_API_KEY."
elif [ "$RESPONSE" -eq 429 ]; then
  echo "⚠️ Rate limited (HTTP 429)."
  echo "   You've exceeded 100 requests/minute. Wait and try again."
else
  echo "⚠️ Unexpected response: HTTP ${RESPONSE}"
  echo "   Make sure Flakewatch is running at ${FLAKEWATCH_URL}"
fi
echo ""
