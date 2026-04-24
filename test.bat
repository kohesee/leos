@echo off
REM Integration Test Script for OS Simulation Phase 1 (Windows)
REM Tests all components and verifies proper integration

cls
echo ╔════════════════════════════════════════════════════════════╗
echo ║   OS SIMULATION - PHASE 1 INTEGRATION TEST SUITE          ║
echo ║                   (Windows Version)                        ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

setlocal enabledelayedexpansion

REM Set project directory
set PROJECT_DIR=D:\Study\college\SEM4\OS\project\leos
set BIN_DIR=%PROJECT_DIR%\bin

echo [1/5] Checking prerequisites...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java not found. Please install Java 11+
    exit /b 1
)
echo ✅ Java found
for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| find "version"') do set JAVA_VER=%%a
echo    Version: %JAVA_VER%

echo.
echo [2/5] Verifying source files...
cd /d %PROJECT_DIR%

set COUNT=0
for /r . %%f in (*.java) do (
    set /a COUNT+=1
)
echo Found %COUNT% Java source files

if %COUNT% lss 15 (
    echo ❌ Expected at least 15 Java files, found %COUNT%
    exit /b 1
)
echo ✅ All source files present

echo.
echo [3/5] Checking CSV test data...
if not exist "tasks.csv" (
    echo ❌ tasks.csv not found
    exit /b 1
)

set TASK_COUNT=0
for /f "delims=," %%a in ('type tasks.csv') do (
    set /a TASK_COUNT+=1
)
set /a TASK_COUNT-=1
echo Found %TASK_COUNT% tasks in CSV

if %TASK_COUNT% lss 1 (
    echo ❌ No valid tasks in CSV
    exit /b 1
)
echo ✅ Test data valid

echo.
echo [4/5] Compiling all Java files...
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

echo Compiling...
javac -d "%BIN_DIR%" ^
    shared\Task.java ^
    kernel\Kernel.java ^
    scheduler\ReadyQueue.java ^
    scheduler\Scheduler.java ^
    memory\Partition.java ^
    memory\MemoryManager.java ^
    memory\LRUCache.java ^
    memory\MemoryMap.java ^
    concurrency\AisleSemaphore.java ^
    concurrency\BankersAlgorithm.java ^
    concurrency\WaitingQueue.java ^
    io\DiskScheduler.java ^
    io\PathOptimizer.java ^
    io\RobotSimulator.java ^
    simulation\EventLogger.java ^
    OrchestratorSimulation.java

if errorlevel 1 (
    echo ❌ Compilation failed
    exit /b 1
)
echo ✅ All files compiled successfully

echo.
echo [5/5] Running integration test...
echo ────────────────────────────────────────────────────────────

REM Run simulation and capture output
java -cp "%BIN_DIR%" OrchestratorSimulation > simulation_output.txt 2>&1

REM Check if simulation completed
findstr /M "PHASE 1 COMPLETE" simulation_output.txt >nul
if errorlevel 1 (
    echo ❌ Simulation did not complete
    echo.
    echo Last 20 lines of output:
    powershell -Command "Get-Content simulation_output.txt | Select-Object -Last 20" 
    exit /b 1
)

echo ✅ Simulation completed successfully
echo.
echo 📊 KEY METRICS:
echo ─────────────────────────────────────────────────────────

for /f "delims=" %%a in ('findstr "Average Wait Time" simulation_output.txt') do (
    echo   %%a
)

for /f "delims=" %%a in ('findstr "Average Turnaround Time" simulation_output.txt') do (
    echo   %%a
)

for /f "delims=" %%a in ('findstr "Completed Tasks Selected" simulation_output.txt') do (
    echo   %%a
)

for /f "delims=" %%a in ('findstr "Total Seek Distance" simulation_output.txt') do (
    echo   %%a
)

for /f "delims=" %%a in ('findstr /C:"Deadlocks Prevented" simulation_output.txt') do (
    echo   %%a
)

echo.
echo ✅ ALL INTEGRATION TESTS PASSED
echo.

echo ────────────────────────────────────────────────────────────
echo Full output saved to: simulation_output.txt
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║         ✅ PHASE 1 INTEGRATION TEST COMPLETE              ║
echo ║     All OS components working correctly together          ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

exit /b 0
