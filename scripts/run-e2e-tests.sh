#!/bin/bash

# Playwright E2E Test Runner Script
# Usage: ./scripts/run-e2e-tests.sh [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
BROWSER="chromium"
TEST_GROUP=""
HEADLESS=""
CI_MODE=""
REPORT_DIR="test-results"

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $# in
    --browser=*)
      BROWSER="${1#*=}"
      shift
      ;;
    --group=*)
      TEST_GROUP="--grep=${1#*=}"
      shift
      ;;
    --headless)
      HEADLESS="HEADLESS=true"
      shift
      ;;
    --ci)
      CI_MODE="CI=true"
      shift
      ;;
    --help)
      echo "Usage: $0 [options]"
      echo ""
      echo "Options:"
      echo "  --browser=<name>    Browser to use: chromium, firefox, webkit (default: chromium)"
      echo "  --group=<pattern>   Run tests matching pattern"
      echo "  --headless         Run in headless mode"
      echo "  --ci               CI mode (more retries, parallel execution)"
      echo "  --help             Show this help message"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Playwright E2E Test Runner${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if Playwright is installed
if ! npx playwright --version > /dev/null 2>&1; then
  echo -e "${YELLOW}Installing Playwright...${NC}"
  pnpm install @playwright/test
  npx playwright install --with-deps chromium
fi

# Ensure test results directory exists
mkdir -p "$REPORT_DIR"

# Set environment variables
export BASE_URL="${BASE_URL:-http://localhost:4200}"
export GATEWAY_URL="${GATEWAY_URL:-http://localhost:9000}"
export $HEADLESS
export $CI_MODE

echo -e "${GREEN}Configuration:${NC}"
echo "  Base URL: $BASE_URL"
echo "  Gateway URL: $GATEWAY_URL"
echo "  Browser: $BROWSER"
echo "  Report Directory: $REPORT_DIR"
echo ""

# Check if services are running
check_service() {
  local name=$1
  local url=$2
  echo -n "Checking $name... "
  if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|404"; then
    echo -e "${GREEN}OK${NC}"
    return 0
  else
    echo -e "${RED}FAILED${NC}"
    return 1
  fi
}

echo -e "${GREEN}Checking services...${NC}"
check_service "Frontend" "http://localhost:4200" || true
check_service "Java Gateway" "http://localhost:9000/actuator/health" || true
echo ""

# Build Playwright command
PW_CMD="npx playwright test"

if [ -n "$TEST_GROUP" ]; then
  PW_CMD="$PW_CMD $TEST_GROUP"
fi

if [ -n "$CI_MODE" ]; then
  PW_CMD="$PW_CMD --reporter=list,html,json"
else
  PW_CMD="$PW_CMD --reporter=list,html"
fi

PW_CMD="$PW_CMD --project=$BROWSER"

# Run tests
echo -e "${GREEN}Running tests...${NC}"
echo ""

if $PW_CMD; then
  echo ""
  echo -e "${GREEN}========================================${NC}"
  echo -e "${GREEN}  Tests completed successfully!${NC}"
  echo -e "${GREEN}========================================${NC}"

  if [ -d "$REPORT_DIR/playwright-report" ]; then
    echo ""
    echo -e "${BLUE}HTML Report:${NC} file://$(pwd)/$REPORT_DIR/playwright-report/index.html"
  fi

  exit 0
else
  echo ""
  echo -e "${RED}========================================${NC}"
  echo -e "${RED}  Some tests failed${NC}"
  echo -e "${RED}========================================${NC}"

  if [ -d "$REPORT_DIR/playwright-report" ]; then
    echo ""
    echo -e "${BLUE}HTML Report:${NC} file://$(pwd)/$REPORT_DIR/playwright-report/index.html"
  fi

  exit 1
fi
