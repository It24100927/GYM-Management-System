@echo off
echo ================================================
echo   Gym Management System - Startup Script
echo ================================================
echo.

REM Set JAVA_HOME if not set
if "%JAVA_HOME%"=="" (
    echo Looking for Java installation...
    if exist "C:\Users\USER\.jdks\ms-21.0.9" (
        set JAVA_HOME=C:\Users\USER\.jdks\ms-21.0.9
        echo Found Java at: %JAVA_HOME%
    ) else (
        echo ERROR: JAVA_HOME is not set and Java was not found.
        echo Please set JAVA_HOME to your JDK 21 installation path.
        pause
        exit /b 1
    )
)

echo.
echo Using JAVA_HOME: %JAVA_HOME%
echo.

echo Starting Gym Management System...
echo ================================================
echo.
echo Once started, access the application at:
echo   - Home:      http://localhost:8080
echo   - Login:     http://localhost:8080/login
echo   - Register:  http://localhost:8080/register
echo.
echo Admin Credentials:
echo   - Email:     admin@gym.com
echo   - Password:  admin123
echo.
echo ================================================
echo.

call mvnw.cmd spring-boot:run

pause
