# LEOS - Warehouse OS Simulator

A Java command-line simulation that maps core OS concepts to a warehouse setting: tasks as processes, aisles as shared resources, floor slots as memory, and robot travel as disk I/O.

## What it simulates

- Process lifecycle (`NEW -> READY -> RUNNING -> WAITING -> TERMINATED`)
- CPU scheduling (Priority + Round Robin)
- Concurrency via aisle semaphores
- Deadlock safety checks (Banker's algorithm)
- Memory allocation + fragmentation tracking (Best Fit + LRU support)
- I/O path optimization (SSTF / SCAN-style behavior)
- Timestamped logging and end-of-run statistics

## Project layout

- `kernel/` - kernel and lifecycle management
- `scheduler/` - scheduler and ready queue
- `memory/` - memory manager, partitions, LRU, map
- `concurrency/` - semaphores, waiting queue, deadlock safety
- `io/` - path optimization and robot simulation
- `shared/` - shared task model
- `simulation/` - output/log utilities
- `CompleteMultiUserOrchestrator.java` - main entry point

## Input CSV

Use `multi_user_tasks.csv` with header:

```csv
userId,taskId,priority,aisleId,memorySize,processTime
```

## All features (short)

- **Process management**: task creation, PCB-style task model, validated state transitions (`NEW -> READY -> RUNNING -> WAITING -> TERMINATED`)
- **Multi-user orchestration**: tasks grouped by users and executed in one unified run
- **CPU scheduling**: Priority + Round Robin selection, ready queue handling, context-switch logging, wait/turnaround reporting
- **Synchronization**: aisle-level semaphores, acquire/release flow, waiting queue integration
- **Deadlock handling**: Banker's safety checks before resource grant, unsafe-state prevention events
- **Memory management**: dynamic partitions, Best Fit/First Fit allocation flow, deallocation, fragmentation tracking, LRU updates
- **I/O + path optimization**: disk/path scheduling with SSTF/SCAN-style logic (`PathOptimizer`, `DiskScheduler`, `RobotSimulator`)
- **Observability**: timestamped event logs, concise terminal output, per-run file logs, final scheduler/memory/concurrency/system stats

## Run

```powershell
java -cp bin CompleteMultiUserOrchestrator multi_user_tasks.csv
```

If needed, recompile key entry components:

```powershell
javac -cp "bin;." -d bin simulation\OutputManager.java concurrency\BankersAlgorithm.java CompleteMultiUserOrchestrator.java
```

## Output

- concise console trace
- per-run log in `logs/` (for example `warehouse_os_YYYYMMDD_HHMMSS.log`)
- final scheduler, memory, concurrency, and system stats

## Test scenarios

Scenario pack: `csv_cases/ALL_CSV_CASES.md`.

Includes normal flow, contention, priority behavior, memory pressure, and mixed stress cases.

---

Built for OS course demos and algorithm behavior analysis.
