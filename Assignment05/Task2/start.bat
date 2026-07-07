@echo off
REM Start/stop/status for the full Task 2 integrated system: Eureka, Config
REM Server, the 5 bounded-context services, and the combined web portal.
REM Usage: start.bat [start|stop|status|urls]  (default: start)
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0"

set "INFRA=infra-eureka-server:8761 infra-config-server:8888"
set "SERVICES=bc01-identity-access:8081 bc02-fleet-management:8082 bc03-booking:8083 bc04-payment:8084 bc05-rating:8085 infra-api-gateway:8080"
set "ALL=%INFRA% %SERVICES%"

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
for %%S in (%ALL%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :port_in_use %%b
        if "!INUSE!"=="1" (
            echo Port %%b is already in use - run "%~nx0 stop" first if these are stale processes.
            exit /b 1
        )
    )
)

echo Building all modules ^(infra + 5 services + gateway^)...
call mvnw.cmd -q clean install -DskipTests
if errorlevel 1 (
    echo Build failed.
    exit /b 1
)

if not exist logs mkdir logs

echo Starting infrastructure ^(Eureka, Config Server^) first...
for %%S in (%INFRA%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        start "" /B cmd /c "mvnw.cmd -q -pl %%a spring-boot:run > logs\%%a.log 2>&1"
    )
)
for %%S in (%INFRA%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :wait_healthy %%a %%b
    )
)

echo Starting the 5 bounded-context services + portal...
for %%S in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        start "" /B cmd /c "mvnw.cmd -q -pl %%a spring-boot:run > logs\%%a.log 2>&1"
    )
)
for %%S in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :wait_healthy %%a %%b
    )
)

call :wait_discovery
call :show_urls
exit /b 0

REM ── stop ───────────────────────────────────────────────────────────────────
:do_stop
for %%S in (%ALL%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :kill_port %%a %%b
    )
)
echo Stopped.
exit /b 0

REM ── status ─────────────────────────────────────────────────────────────────
:do_status
for %%S in (%ALL%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%S") do (
        call :check_health %%a %%b
    )
)
exit /b 0

REM ── urls ───────────────────────────────────────────────────────────────────
:show_urls
echo.
echo   *** Start here ***
echo   Combined web portal        http://localhost:8080          ^(register, log in, search ^& book, end ride, rate - one UI^)
echo.
echo   Infrastructure
echo   Eureka dashboard           http://localhost:8761
echo   Config server              http://localhost:8888/bc03-booking/default   ^(example: shows resolved config for a service^)
echo.
echo   Per-service API/UI ^(still available individually^)
echo   bc01 Identity ^& Access   http://localhost:8081/ui       http://localhost:8081/swagger-ui.html
echo   bc02 Fleet Management    http://localhost:8082/ui       http://localhost:8082/swagger-ui.html
echo   bc03 Booking             http://localhost:8083/ui       http://localhost:8083/swagger-ui.html
echo   bc04 Payment             http://localhost:8084/ui       http://localhost:8084/swagger-ui.html
echo   bc05 Rating              http://localhost:8085/ui       http://localhost:8085/swagger-ui.html
echo.
echo   H2 console for any bc0X service: http://localhost:PORT/h2-console
exit /b 0

REM ── helpers ────────────────────────────────────────────────────────────────

REM infra-config-server's own /{application}/{profile} route shadows
REM /actuator/health on its main port, so its actuator lives on 8889 instead.
:health_port
set "HPORT=%~2"
if /I "%~1"=="infra-config-server" set "HPORT=8889"
exit /b 0

:port_in_use
set "INUSE=0"
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /C:":%~1 " ^| findstr "LISTENING"') do set "INUSE=1"
exit /b 0

:wait_healthy
set "NAME=%~1"
set "PORT=%~2"
call :health_port "%NAME%" "%PORT%"
set "UP=0"
for /L %%i in (1,1,40) do (
    if "!UP!"=="0" (
        curl -s http://localhost:!HPORT!/actuator/health 2>nul | findstr /C:"\"status\":\"UP\"" >nul
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
call :health_port "%NAME%" "%PORT%"
curl -s http://localhost:%HPORT%/actuator/health 2>nul | findstr /C:"\"status\":\"UP\"" >nul
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

REM Waits for Eureka's own registry to list every expected service name
REM before treating the stack as ready, so cross-service calls don't briefly
REM hit fallback data because a client's local registry cache is still empty.
:wait_discovery
echo Waiting for services to discover each other via Eureka...
set "ALLUP=0"
for /L %%i in (1,1,20) do (
    if "!ALLUP!"=="0" (
        curl -s -H "Accept: application/json" http://localhost:8761/eureka/apps > "%TEMP%\twx_eureka_apps.json" 2>nul
        set "MISSING=0"
        for %%N in (BC01-IDENTITY-ACCESS BC02-FLEET-MANAGEMENT BC03-BOOKING BC04-PAYMENT BC05-RATING INFRA-API-GATEWAY) do (
            findstr /C:"%%N" "%TEMP%\twx_eureka_apps.json" >nul
            if errorlevel 1 set /a MISSING+=1
        )
        if "!MISSING!"=="0" (
            echo   All services registered with Eureka.
            ping -n 7 127.0.0.1 >nul
            set "ALLUP=1"
        ) else (
            ping -n 3 127.0.0.1 >nul
        )
    )
)
if "!ALLUP!"=="0" echo   Warning: not all services showed up in Eureka's registry in time - cross-service calls may briefly use fallback data.
del "%TEMP%\twx_eureka_apps.json" >nul 2>&1
exit /b 0
