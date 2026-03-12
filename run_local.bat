@echo off
echo =========================================
echo       BUILDING WAW LAUNDRY ANDROID APP
echo =========================================
echo.

:: Build the Debug APK using Gradle
echo [1/3] Assembling Debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Gradle Build Failed! Check your code/logs above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Installing APK to Emulator/Connected Device...
:: Find the generated APK and install it using ADB
call adb install -r -t app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] ADB Install Failed! Make sure your emulator/device is running.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Launching the App...
:: Launch the Main Activity
call adb shell am start -n net.rcdevgames.wawlaundry/net.rcdevgames.wawlaundry.MainActivity

echo.
echo =========================================
echo       SUCCESS: App is now running!
echo =========================================
pause
