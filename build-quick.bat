@echo off
REM ================================================
REM WAW LAUNDRY - Quick Build (No Clean)
REM Faster builds for development/testing
REM ================================================

setlocal enabledelayedexpansion

echo ================================================
echo   WAW LAUNDRY - Quick Build
echo ================================================
echo.

REM Get current version
if exist "app\version.properties" (
    for /f "tokens=1,2 delims==" %%a in ('type app\version.properties ^| findstr "VERSION_"') do (
        if "%%a"=="VERSION_CODE" set VER_CODE=%%b
        if "%%a"=="VERSION_NAME" set VER_NAME=%%b
    )
    echo Building version: %VER_NAME% (code %VER_CODE%)
) else (
    echo Building version: unknown
)
echo.

echo Building Release APKs...
call gradlew assembleRelease --warning-mode none

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo   BUILD SUCCESSFUL
    echo ================================================
    echo.
    echo APKs location: app\build\outputs\apk\release\
    echo.

    REM Show sizes
    echo File sizes:
    for %%f in (app\build\outputs\apk\release\*.apk) do (
        set "size=%%~zf"
        set /a "sizeMB=!size!/1048576"
        echo   %%~nxf: !size! bytes (!sizeMB! MB)
    )
    echo.
) else (
    echo.
    echo ================================================
    echo   BUILD FAILED
    echo ================================================
    echo.
)

pause
