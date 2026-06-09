#!/bin/bash

# Playwright API Integration Test Script
# Usage: ./scripts/run-api-tests.sh [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  API Integration Tests${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if gateway is running
echo "Checking Java Gateway..."
GATEWAY_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9000/actuator/health 2>/dev/null || echo "000")
if [ "$GATEWAY_STATUS" != "200" ]; then
  echo -e "${YELLOW}Warning: Gateway returned status $GATEWAY_STATUS${NC}"
  echo "Tests will run but may fail if services are not available"
fi
echo ""

# Run API integration tests
npx playwright test \
  gateway-api.spec.ts \
  api-integration.spec.ts \
  --project=chromium \
  --reporter=list,html \
  "$@"

# Check result
if [ $? -eq 0 ]; then
  echo ""
  echo -e "${GREEN}API tests completed successfully!${NC}"
else
  echo ""
  echo -e "${RED}API tests failed${NC}"
  exit 1
fi
