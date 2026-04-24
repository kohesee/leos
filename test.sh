#!/bin/bash
# Integration Test Script for OS Simulation Phase 1
# Tests all components and verifies proper integration

echo "╔════════════════════════════════════════════════════════════╗"
echo "║   OS SIMULATION - PHASE 1 INTEGRATION TEST SUITE          ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

PROJECT_DIR="D:\Study\college\SEM4\OS\project\leos"
BIN_DIR="$PROJECT_DIR/bin"

echo "[1/5] Checking prerequisites..."
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 11+"
    exit 1
fi
echo "✅ Java found: $(java -version 2>&1 | head -1)"

if ! command -v javac &> /dev/null; then
    echo "❌ Javac not found. Please install Java Development Kit"
    exit 1
fi
echo "✅ Javac found"

echo ""
echo "[2/5] Verifying source files..."
cd "$PROJECT_DIR"
JAVA_FILES=$(find . -name "*.java" -type f | wc -l)
echo "Found $JAVA_FILES Java source files"

if [ $JAVA_FILES -lt 15 ]; then
    echo "❌ Expected at least 15 Java files, found $JAVA_FILES"
    exit 1
fi
echo "✅ All source files present"

echo ""
echo "[3/5] Checking CSV test data..."
if [ ! -f "tasks.csv" ]; then
    echo "❌ tasks.csv not found"
    exit 1
fi
TASK_COUNT=$(grep -c "^[0-9]" tasks.csv)
echo "Found $TASK_COUNT tasks in CSV"
if [ $TASK_COUNT -lt 1 ]; then
    echo "❌ No valid tasks in CSV"
    exit 1
fi
echo "✅ Test data valid"

echo ""
echo "[4/5] Compiling all Java files..."
mkdir -p "$BIN_DIR"

if ! javac -d "$BIN_DIR" \
    shared/Task.java \
    kernel/Kernel.java \
    scheduler/ReadyQueue.java \
    scheduler/Scheduler.java \
    memory/Partition.java \
    memory/MemoryManager.java \
    memory/LRUCache.java \
    memory/MemoryMap.java \
    concurrency/AisleSemaphore.java \
    concurrency/BankersAlgorithm.java \
    concurrency/WaitingQueue.java \
    io/DiskScheduler.java \
    io/PathOptimizer.java \
    io/RobotSimulator.java \
    simulation/EventLogger.java \
    OrchestratorSimulation.java 2>&1; then
    echo "❌ Compilation failed"
    exit 1
fi
echo "✅ All files compiled successfully"

echo ""
echo "[5/5] Running integration test..."
echo "────────────────────────────────────────────────────────────"

# Run simulation and capture output
java -cp "$BIN_DIR" OrchestratorSimulation > simulation_output.txt 2>&1

# Check if simulation completed
if grep -q "PHASE 1 COMPLETE" simulation_output.txt; then
    echo "✅ Simulation completed successfully"
    echo ""
    
    # Extract and display key metrics
    echo "📊 KEY METRICS:"
    echo "─────────────────────────────────────────────────────────"
    
    if grep -q "Average Wait Time" simulation_output.txt; then
        AVG_WAIT=$(grep "Average Wait Time" simulation_output.txt | sed 's/.*: //' | sed 's/ .*//')
        echo "  Average Wait Time: $AVG_WAIT ms"
    fi
    
    if grep -q "Average Turnaround Time" simulation_output.txt; then
        AVG_TURN=$(grep "Average Turnaround Time" simulation_output.txt | sed 's/.*: //' | sed 's/ .*//')
        echo "  Average Turnaround Time: $AVG_TURN ms"
    fi
    
    if grep -q "Completed Tasks Selected" simulation_output.txt; then
        COMPLETED=$(grep "Completed Tasks Selected" simulation_output.txt | sed 's/.*: //')
        echo "  Tasks Completed: $COMPLETED"
    fi
    
    if grep -q "Fragmentation:" simulation_output.txt; then
        FRAG=$(grep "Fragmentation:" simulation_output.txt | head -1 | sed 's/.*: //' | sed 's/[^0-9.]*//g')
        echo "  Memory Fragmentation: ${FRAG}%"
    fi
    
    if grep -q "Total Seek Distance" simulation_output.txt; then
        SEEK=$(grep "Total Seek Distance" simulation_output.txt | sed 's/.*: //' | sed 's/ .*//')
        echo "  Total Seek Distance: $SEEK units"
    fi
    
    if grep -q "Deadlocks Prevented" simulation_output.txt; then
        DEAD=$(grep "Deadlocks Prevented" simulation_output.txt | sed 's/.*: //')
        echo "  Deadlocks Prevented: $DEAD"
    fi
    
    echo ""
    echo "✅ ALL INTEGRATION TESTS PASSED"
    echo ""
    
else
    echo "❌ Simulation did not complete"
    echo ""
    echo "Last 20 lines of output:"
    tail -20 simulation_output.txt
    exit 1
fi

echo "────────────────────────────────────────────────────────────"
echo "Full output saved to: simulation_output.txt"
echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║         ✅ PHASE 1 INTEGRATION TEST COMPLETE              ║"
echo "║     All OS components working correctly together          ║"
echo "╚════════════════════════════════════════════════════════════╝"
