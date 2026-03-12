@echo off
REM ================================================
REM WAW LAUNDRY - Build Script
REM Cleans and builds Release APKs
REM ================================================

setlocal enabledelayedexpansion

echo ================================================
echo   WAW LAUNDRY - Build Script
echo ================================================
echo.

REM Get current version
if exist "app\version.properties" (
    for /f "tokens=1,2 delims==" %%a in ('type app\version.properties ^| findstr "VERSION_"') do (
        if "%%a"=="VERSION_CODE" set VER_CODE=%%b
        if "%%a"=="VERSION_NAME" set VER_NAME=%%b
    )
    echo Current Version: %VER_NAME% (code %VER_CODE%)
) else (
    echo Version: Not found (will use default)
)
echo.

REM Create output directory
set OUTPUT_DIR=release-output
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
mkdir "%OUTPUT_DIR%"

echo ================================================
echo   STEP 1: Clean Project
echo ================================================
call gradlew clean
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)
echo [OK] Clean completed
echo.

echo ================================================
echo   STEP 2: Build Release APKs
echo ================================================
call gradlew assembleRelease --warning-mode none
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] APK build failed!
    pause
    exit /b 1
)
echo [OK] APK build completed
echo.

echo ================================================
echo   STEP 3: Copy to Output Folder
echo ================================================

REM Copy APKs
set "copied=0"
if exist "app\build\outputs\apk\release\app-armeabi-v7a-release.apk" (
    copy /Y "app\build\outputs\apk\release\app-armeabi-v7a-release.apk" "%OUTPUT_DIR%\waw-laundry-%VER_NAME%-armeabi-v7a.apk" >nul
    echo [OK] armv7 APK copied
    set "copied=1"
)

if exist "app\build\outputs\apk\release\app-arm64-v8a-release.apk" (
    copy /Y "app\build\outputs\apk\release\app-arm64-v8a-release.apk" "%OUTPUT_DIR%\waw-laundry-%VER_NAME%-arm64-v8a.apk" >nul
    echo [OK] arm64 APK copied
    set "copied=1"
)

if %copied%==0 (
    echo [WARNING] No APKs found to copy!
)
echo.

echo ================================================
echo   BUILD SUMMARY
echo ================================================
echo.
echo Output location: %CD%\%OUTPUT_DIR%\
echo.

echo Files created:
dir "%OUTPUT_DIR%" /b 2>nul
echo.

echo ================================================
echo   FILE SIZES
echo ================================================
echo.
for %%f in ("%OUTPUT_DIR%\*.*") do (
    set "size=%%~zf"
    set /a "sizeMB=!size!/1048576"
    echo %%~nxf: !size! bytes (!sizeMB! MB)
)
echo.

echo ================================================
echo   RECOMMENDATION FOR UPLOAD
echo ================================================
echo.
echo Direct distribution: Use armeabi-v7a APK (smallest, compatible)
echo   - Covers 99%% of Indonesian Android devices
echo   - Most efficient for spek kentang phones
echo.

echo Press any key to open output folder...
pause >nul
start "" explorer "%OUTPUT_DIR%"

endlocal
