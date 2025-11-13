#!/bin/bash
# Material Design 3 Migration Verification Script
# This script performs basic verification of the MD3 migration

echo "================================================"
echo "Material Design 3 Migration Verification"
echo "================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
PASS=0
FAIL=0
WARN=0

# Function to check if file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} Found: $1"
        ((PASS++))
        return 0
    else
        echo -e "${RED}✗${NC} Missing: $1"
        ((FAIL++))
        return 1
    fi
}

# Function to check if string exists in file
check_content() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $3"
        ((PASS++))
        return 0
    else
        echo -e "${RED}✗${NC} $3"
        ((FAIL++))
        return 1
    fi
}

# Function to check warning
check_warning() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${YELLOW}⚠${NC} $3"
        ((WARN++))
        return 0
    else
        echo -e "${GREEN}✓${NC} $3"
        ((PASS++))
        return 1
    fi
}

echo "1. Checking Theme Files"
echo "------------------------"
check_file "app/src/main/res/values/themes.xml"
check_file "app/src/main/res/values-night/themes.xml"
check_file "app/src/main/res/values-v31/themes.xml"
echo ""

echo "2. Checking Color Files"
echo "-----------------------"
check_file "app/src/main/res/values/colors.xml"
check_file "app/src/main/res/values-night/colors.xml"
echo ""

echo "3. Checking Material 3 Theme Usage"
echo "-----------------------------------"
check_content "app/src/main/res/values/themes.xml" "Theme.Material3.DayNight" "Base theme uses Material 3"
check_content "app/src/main/res/values-night/themes.xml" "Theme.Material3.DayNight" "Dark theme uses Material 3"
check_content "app/src/main/res/values-v31/themes.xml" "dynamicColorThemeOverlay" "Android 12+ has dynamic colors"
echo ""

echo "4. Checking MD3 Color Scheme"
echo "-----------------------------"
check_content "app/src/main/res/values/colors.xml" "md_theme_light_primary" "Light theme primary color defined"
check_content "app/src/main/res/values/colors.xml" "md_theme_dark_primary" "Dark theme primary color defined"
check_content "app/src/main/res/values/colors.xml" "md_theme_light_surface" "Surface colors defined"
check_content "app/src/main/res/values/colors.xml" "colorPrimary" "Legacy colors preserved"
echo ""

echo "5. Checking Material Component Updates"
echo "---------------------------------------"
check_content "app/src/main/res/layout/fragment_route.xml" "Widget.Material3.Button.OutlinedButton" "Route fragment button updated"
check_content "app/src/main/res/layout/activity_station_details.xml" "Widget.Material3.Button.OutlinedButton" "Station details button updated"
check_content "app/src/main/res/layout/dialog_settings.xml" "Widget.Material3.Button.OutlinedButton" "Settings dialog button updated"
echo ""

echo "6. Checking Legacy Component References"
echo "----------------------------------------"
check_warning "app/src/main/res/layout" "Widget.MaterialComponents" "No old Material Components styles found (good)"
echo ""

echo "7. Checking Build Configuration"
echo "--------------------------------"
check_content "app/build.gradle" "com.google.android.material:material:1.11" "Material library updated to 1.11+"
check_content "app/build.gradle" "minSdk 24" "Min SDK is 24 (Android 7)"
check_content "app/build.gradle" "compileSdk 34" "Compile SDK is 34"
echo ""

echo "8. Checking Documentation"
echo "-------------------------"
check_file "MATERIAL_DESIGN_3_MIGRATION.md"
check_file "MD3_TESTING_CHECKLIST.md"
check_file "IMPLEMENTATION_SUMMARY.md"
check_content "README.md" "Material Design 3" "README mentions MD3"
check_content "PROJECT_SUMMARY.md" "Material Design 3" "PROJECT_SUMMARY mentions MD3"
echo ""

echo "9. Checking Android Manifest"
echo "-----------------------------"
check_content "app/src/main/AndroidManifest.xml" "Theme.RiyadhTransport" "App uses custom theme"
echo ""

echo "================================================"
echo "Verification Summary"
echo "================================================"
echo -e "${GREEN}Passed: $PASS${NC}"
echo -e "${RED}Failed: $FAIL${NC}"
echo -e "${YELLOW}Warnings: $WARN${NC}"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ All critical checks passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Build the app: ./gradlew clean assembleDebug"
    echo "2. Test on Android 7 device"
    echo "3. Test on Android 12+ device for dynamic colors"
    echo "4. Follow MD3_TESTING_CHECKLIST.md for comprehensive testing"
    echo ""
    exit 0
else
    echo -e "${RED}✗ Some checks failed. Please review the issues above.${NC}"
    echo ""
    exit 1
fi
