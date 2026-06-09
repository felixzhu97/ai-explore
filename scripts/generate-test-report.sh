#!/bin/bash

# Playwright Test Report Generator
# Usage: ./scripts/generate-test-report.sh

set -e

# Colors for output
BLUE='\033[0;34m'
GREEN='\033[0;32m'
NC='\033[0m'

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

REPORT_DIR="test-results/playwright-report"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Generating Test Report${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if report exists
if [ -d "$REPORT_DIR" ]; then
  echo -e "${GREEN}Report found at: file://$(pwd)/$REPORT_DIR/index.html${NC}"
  echo ""
  echo "Opening report..."
  open "$REPORT_DIR/index.html" 2>/dev/null || echo "Please open the report manually"
else
  echo "No report found. Please run tests first."
  exit 1
fi

# Generate summary from JSON results
if [ -f "$PROJECT_ROOT/test-results/results.json" ]; then
  echo ""
  echo "Test Summary:"
  echo "========================================"

  # Use Node.js to parse JSON (if available)
  if command -v node &> /dev/null; then
    node -e "
      const fs = require('fs');
      const data = JSON.parse(fs.readFileSync('$PROJECT_ROOT/test-results/results.json', 'utf8'));

      const stats = data.stats || {};
      console.log('  Total Tests:', stats.tests || 0);
      console.log('  Passed:', stats.passed || 0);
      console.log('  Failed:', stats.failed || 0);
      console.log('  Skipped:', stats.skipped || 0);
      console.log('  Duration:', (stats.duration || 0) / 1000, 'seconds');
    "
  fi
fi
