@echo off
REM Start/stop/status for all 5 standalone Task 1 microservices.
REM Usage: start.bat [start|stop|status|urls]  (default: start)
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

set "SERVICES=bc01-identity-access:8081 bc02-fleet-management:8082 bc03-booking:8083 bc04-payment:8084 bc05-rating:8085"

set "CMD=%~1"
if "%CMD%"=="" set "CMD=start"

if /I "%CMD%"=="start" goto :do_start
if /I "%CMD%"=="stop" goto :do_stop
if /I "%CMD%"=="status" goto :do_status
if /I "%CMD%"=="urls" goto :show_urls
echo Usage: %~nx0 [start, stop, status, or urls]  (default: start)
exit /b 1

REM ── start ──────────────────────────────────────────────────────────────────
:do_start
for %%S in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :port_in_use %%b
        if "!INUSE!"=="1" (
            echo Port %%b is already in use - run "%~nx0 stop" first if these are stale processes.
            exit /b 1
        )
    )
)

echo Building all modules...
call mvnw.cmd -q clean install -DskipTests
if errorlevel 1 (
    echo Build failed.
    exit /b 1
)

if not exist logs mkdir logs

for %%S in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        echo Starting %%a...
        start "" /B cmd /c "mvnw.cmd -q -pl %%a spring-boot:run > logs\%%a.log 2>&1"
    )
)

echo Waiting for all services to report healthy...
for %%S in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :wait_healthy %%a %%b
    )
)

call :show_urls
exit /b 0

REM ── stop ───────────────────────────────────────────────────────────────────
:do_stop
for %%S in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :kill_port %%a %%b
    )
)
echo Stopped.
exit /b 0

REM ── status ─────────────────────────────────────────────────────────────────
:do_status
for %%S in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :check_health %%a %%b
    )
)
exit /b 0

REM ── urls ───────────────────────────────────────────────────────────────────
:show_urls
echo.
echo   Service                 UI                                    API docs
echo   -----------------------------------------------------------------------------------------
echo   bc01 Identity ^& Access   http://localhost:8081/ui              http://localhost:8081/swagger-ui.html
echo   bc02 Fleet Management    http://localhost:8082/ui              http://localhost:8082/swagger-ui.html
echo   bc03 Booking             http://localhost:8083/ui              http://localhost:8083/swagger-ui.html
echo   bc04 Payment             http://localhost:8084/ui              http://localhost:8084/swagger-ui.html
echo   bc05 Rating              http://localhost:8085/ui              http://localhost:8085/swagger-ui.html
echo.
echo   H2 console for any service: http://localhost:PORT/h2-console
exit /b 0

REM ── helpers ────────────────────────────────────────────────────────────────

:port_in_use
set "INUSE=0"
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /C:":%~1 " ^| findstr "LISTENING"') do set "INUSE=1"
exit /b 0

:wait_healthy
set "NAME=%~1"
set "PORT=%~2"
set "UP=0"
for /L %%i in (1,1,40) do (
    if "!UP!"=="0" (
        curl -s http://localhost:!PORT!/actuator/health 2>nul | findstr /C:"\"status\":\"UP\"" >nul
        if not errorlevel 1 (
            set "UP=1"
        ) else (
            ping -n 4 127.0.0.1 >nul
        )
    )
)
if "!UP!"=="1" (
    echo   !NAME! ^(:!PORT!^) is UP
) else (
    echo   !NAME! ^(:!PORT!^) did NOT come up in time - check logs\!NAME!.log
)
exit /b 0

:check_health
set "NAME=%~1"
set "PORT=%~2"
curl -s http://localhost:%PORT%/actuator/health 2>nul | findstr /C:"\"status\":\"UP\"" >nul
if not errorlevel 1 (
    echo %NAME% ^(:%PORT%^) UP
) else (
    echo %NAME% ^(:%PORT%^) DOWN
)
exit /b 0

:kill_port
set "NAME=%~1"
set "PORT=%~2"
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /C:":%PORT% " ^| findstr "LISTENING"') do (
    echo Stopping %NAME% ^(:%PORT%^)...
    taskkill /F /PID %%p /T >nul 2>&1
)
exit /b 0
