#!/bin/bash
# ============================================================================
# run-and-report.sh — Run Tests & Send Results to Flakewatch
# ============================================================================
# This script:
#   1. Runs the sample JUnit tests
#   2. Parses the JUnit XML reports (target/surefire-reports/)
#   3. Builds a JSON payload with REAL test results
#   4. Sends them to Flakewatch
#
# Usage:
#   ./run-and-report.sh              # Run once
#   ./run-and-report.sh --loop 5     # Run 5 times (to trigger flake detection)
# ============================================================================

set -uo pipefail

FLAKEWATCH_URL="${FLAKEWATCH_URL:-http://localhost:8080}"
FLAKEWATCH_API_KEY="${FLAKEWATCH_API_KEY:-fw_test_1234567890abcdef}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOOP_COUNT=1

# Parse arguments
if [ "${1:-}" = "--loop" ] && [ -n "${2:-}" ]; then
  LOOP_COUNT=$2
fi

# Define commit/branch ONCE so they stay constant across loops.
# Flakewatch needs the status to flip on the SAME commit to detect a flake.
GLOBAL_COMMIT_HASH=$(git rev-parse HEAD 2>/dev/null || echo "sample-commit-$(date +%s)")
GLOBAL_BRANCH_NAME=$(git branch --show-current 2>/dev/null || echo "main")

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║   Sample Test Project → Flakewatch Reporter          ║"
echo "╠══════════════════════════════════════════════════════╣"
echo "║  Flakewatch:  ${FLAKEWATCH_URL}"
echo "║  Loop Count:  ${LOOP_COUNT} run(s)"
echo "║  Commit:      ${GLOBAL_COMMIT_HASH}"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

for RUN in $(seq 1 $LOOP_COUNT); do
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "  🔄 Run $RUN of $LOOP_COUNT"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

  # ---- Step 1: Run the tests ----
  echo ""
  echo "🧪 Running tests..."
  cd "$SCRIPT_DIR"
  mvn test -B -q 2>&1 | tail -20
  echo ""

  # ---- Step 2: Parse JUnit XML reports ----
  REPORT_DIR="$SCRIPT_DIR/target/surefire-reports"
  
  if [ ! -d "$REPORT_DIR" ]; then
    echo "❌ No test reports found at $REPORT_DIR"
    exit 1
  fi

  # Parse XML reports and build JSON results array
  RESULTS="["
  FIRST=true

  for XML_FILE in "$REPORT_DIR"/TEST-*.xml; do
    [ -f "$XML_FILE" ] || continue
    
    SUITE_NAME=$(grep -o 'name="[^"]*"' "$XML_FILE" | head -1 | sed 's/name="//;s/"//')
    
    # Extract each test case
    while IFS= read -r line; do
      TEST_NAME=$(echo "$line" | grep -o 'name="[^"]*"' | head -1 | sed 's/name="//;s/"//')
      CLASSNAME=$(echo "$line" | grep -o 'classname="[^"]*"' | head -1 | sed 's/classname="//;s/"//')
      TIME_SEC=$(echo "$line" | grep -o 'time="[^"]*"' | head -1 | sed 's/time="//;s/"//')
      
      [ -z "$TEST_NAME" ] && continue
      
      # Calculate duration in ms
      DURATION_MS=$(echo "$TIME_SEC" | awk '{printf "%d", $1 * 1000}')
      [ -z "$DURATION_MS" ] && DURATION_MS=100
      
      # Check if this test case has a <failure> or <error> element following it
      # We check for failure by looking at the XML structure
      FULL_TEST_ID="${CLASSNAME}.${TEST_NAME}"
      
      # Check if test failed by looking for failure element within the testcase
      if grep -A2 "name=\"${TEST_NAME}\"" "$XML_FILE" | grep -q "<failure\|<error"; then
        STATUS="FAIL"
        ERROR_MSG=$(grep -A3 "name=\"${TEST_NAME}\"" "$XML_FILE" | grep -o 'message="[^"]*"' | head -1 | sed 's/message="//;s/"//')
        [ -z "$ERROR_MSG" ] && ERROR_MSG="Test failed"
        ERROR_FIELD=", \"errorMessage\": \"${ERROR_MSG}\""
      else
        STATUS="PASS"
        ERROR_FIELD=""
      fi
      
      if [ "$FIRST" = true ]; then
        FIRST=false
      else
        RESULTS="${RESULTS},"
      fi
      
      RESULTS="${RESULTS}
      {
        \"testIdentifier\": \"${FULL_TEST_ID}\",
        \"suiteName\": \"${SUITE_NAME}\",
        \"status\": \"${STATUS}\",
        \"durationMs\": ${DURATION_MS}${ERROR_FIELD}
      }"
      
    done < <(grep "<testcase " "$XML_FILE")
    
  done

  RESULTS="${RESULTS}
    ]"

  # ---- Step 3: Build the full payload ----
  EVENT_ID=$(uuidgen 2>/dev/null || cat /proc/sys/kernel/random/uuid 2>/dev/null || echo "run-${RUN}-$(date +%s)")

  PAYLOAD="{
    \"eventId\": \"${EVENT_ID}\",
    \"commitHash\": \"${GLOBAL_COMMIT_HASH}\",
    \"branchName\": \"${GLOBAL_BRANCH_NAME}\",
    \"runnerId\": \"sample-project-runner\",
    \"results\": ${RESULTS}
  }"

  # ---- Step 4: Send to Flakewatch ----
  echo "📡 Sending results to Flakewatch..."
  
  RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST "${FLAKEWATCH_URL}/api/v1/ingest/test-results" \
    -H "Content-Type: application/json" \
    -H "X-API-KEY: ${FLAKEWATCH_API_KEY}" \
    -d "${PAYLOAD}")

  if [ "$RESPONSE" -eq 200 ] || [ "$RESPONSE" -eq 202 ]; then
    echo "✅ Run $RUN: Results sent successfully!"
  elif [ "$RESPONSE" -eq 401 ]; then
    echo "❌ Run $RUN: Auth failed (HTTP 401). Check API key."
  else
    echo "⚠️  Run $RUN: HTTP ${RESPONSE}"
  fi

  # Small delay between runs
  if [ "$RUN" -lt "$LOOP_COUNT" ]; then
    echo ""
    echo "⏳ Waiting 2 seconds before next run..."
    sleep 2
  fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  ✅ Done! Completed ${LOOP_COUNT} test run(s)."
echo ""
echo "  👉 Open the Flakewatch Dashboard to see results:"
echo "     http://localhost:4200"
echo ""
echo "  The flaky tests (FlakyTest.*) should start appearing"
echo "  in quarantine after their results flip between PASS/FAIL."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
