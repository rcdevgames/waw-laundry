#!/bin/bash
# ================================================
# WAW LAUNDRY - Build Script (Linux/macOS)
# Cleans and builds Release APK + App Bundle
# ================================================

set -e

echo "================================================"
echo "  WAW LAUNDRY - Build Script"
echo "================================================"
echo ""

# Get current version
if [ -f "app/version.properties" ]; then
    VER_CODE=$(grep "VERSION_CODE" app/version.properties | cut -d'=' -f2 | tr -d ' ')
    VER_NAME=$(grep "VERSION_NAME" app/version.properties | cut -d'=' -f2 | tr -d ' ')
    echo "Current Version: $VER_NAME (code $VER_CODE)"
else
    echo "Version: Not found (will use default)"
fi
echo ""

# Create output directory
OUTPUT_DIR="release-output"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

echo "================================================"
echo "  STEP 1: Clean Project"
echo "================================================"
./gradlew clean
echo "[OK] Clean completed"
echo ""

echo "================================================"
echo "  STEP 2: Build Release APKs"
echo "================================================"
./gradlew assembleRelease --warning-mode none
echo "[OK] APK build completed"
echo ""

echo "================================================"
echo "  STEP 3: Build App Bundle (AAB)"
echo "================================================"
./gradlew bundleRelease --warning-mode none || echo "[WARNING] AAB build failed (but APKs are OK)"
echo "[OK] AAB build completed"
echo ""

echo "================================================"
echo "  STEP 4: Copy to Output Folder"
echo "================================================"

# Copy APKs
if [ -f "app/build/outputs/apk/release/app-armeabi-v7a-release.apk" ]; then
    cp "app/build/outputs/apk/release/app-armeabi-v7a-release.apk" "$OUTPUT_DIR/waw-laundry-${VER_NAME}-armeabi-v7a.apk"
    echo "[OK] armv7 APK copied"
fi

if [ -f "app/build/outputs/apk/release/app-arm64-v8a-release.apk" ]; then
    cp "app/build/outputs/apk/release/app-arm64-v8a-release.apk" "$OUTPUT_DIR/waw-laundry-${VER_NAME}-arm64-v8a.apk"
    echo "[OK] arm64 APK copied"
fi

if [ -f "app/build/outputs/apk/release/app-universal-release.apk" ]; then
    cp "app/build/outputs/apk/release/app-universal-release.apk" "$OUTPUT_DIR/waw-laundry-${VER_NAME}-universal.apk"
    echo "[OK] universal APK copied"
fi

# Copy AAB
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    cp "app/build/outputs/bundle/release/app-release.aab" "$OUTPUT_DIR/waw-laundry-${VER_NAME}.aab"
    echo "[OK] AAB copied"
fi
echo ""

echo "================================================"
echo "  BUILD SUMMARY"
echo "================================================"
echo ""
ls -lh "$OUTPUT_DIR" | awk '{print $9, $5}'
echo ""

echo "================================================"
echo "  FILE SIZES"
echo "================================================"
echo ""
for file in "$OUTPUT_DIR"/*.*; do
    filename=$(basename "$file")
    size=$(stat -f%z "$file" 2>/dev/null || stat -c%s "$file" 2>/dev/null)
    size_mb=$((size / 1048576))
    echo "$filename: $size bytes ($size_mb MB)"
done
echo ""

echo "================================================"
echo "  RECOMMENDATION FOR UPLOAD"
echo "================================================"
echo ""
echo "Google Play Store: Use AAB file"
echo "Direct distribution: Use armeabi-v7a APK (smallest, compatible)"
echo ""
echo "Output location: $(pwd)/$OUTPUT_DIR/"
echo ""

# Open output folder in Finder (macOS) or file manager (Linux with xdg-open)
if [[ "$OSTYPE" == "darwin"* ]]; then
    open "$OUTPUT_DIR"
elif command -v xdg-open &> /dev/null; then
    xdg-open "$OUTPUT_DIR"
fi
