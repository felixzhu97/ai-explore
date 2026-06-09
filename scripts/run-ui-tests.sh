#!/bin/bash

# Playwright UI Interaction Test Script
# Usage: ./scripts/run-ui-tests.sh [options]

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
echo -e "${BLUE}  UI Interaction Tests${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Run UI interaction tests
npx playwright test \
  ui-interactions.spec.ts \
  --project=chromium \
  --reporter=list,html \
  "$@"

# Check result
if [ $? -eq 0 ]; then
  echo ""
  echo -e "${GREEN}UI tests completed successfully!${NC}"
else
  echo ""
  echo -e "${RED}UI tests failed${NC}"
  exit 1
fi
