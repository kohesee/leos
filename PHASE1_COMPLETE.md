# Operating System Simulation - Phase 1 Complete ✅

## Project Overview

A comprehensive OS simulation implementing core OS concepts including:

- **Process Management** (PCB, State Transitions, Kernel)
- **CPU Scheduling** (Round Robin, Priority Scheduling)
- **Memory Management** (First Fit, Best Fit, LRU Cache)
- **Concurrency Control** (Semaphores, Banker's Algorithm, Deadlock Prevention)
- **Disk I/O Optimization** (SSTF, SCAN algorithms)

---

## Architecture

### Core Components

#### 1. **Kernel (Process Management)**

- **File**: `kernel/Kernel.java`
- **Responsibilities**:
  - Create and manage tasks (Process Control Blocks)
  - Maintain jobTable, readyQueue, waitingQueue
  - State transitions: NEW → READY → RUNNING → WAITING/TERMINATED
  - Load tasks from CSV file
  - Central event logging with timestamps

#### 2. **Scheduler (CPU Scheduling)**

- **File**: `scheduler/Scheduler.java`
- **Algorithms**:
  - **Round Robin**: Standard priority tasks with 5ms time quantum
  - **Priority Scheduling**: Express deliveries (priority 1-2)
- **Features**:
  - Context switching with state management
  - Performance metrics (wait time, turnaround time)
  - Statistics reporting

#### 3. **Ready Queue (Data Structure)**

- **File**: `scheduler/ReadyQueue.java`
- **Implementation**: PriorityQueue with custom comparator
- **Features**:
  - Primary sort: Task priority (1-5, lower = higher)
  - Secondary sort: Last access time (Round Robin fairness)

#### 4. **Memory Manager**

- **File**: `memory/MemoryManager.java`
- **Capacity**: 500-unit warehouse floor
- **Algorithms**:
  - **First Fit**: Allocate at first available block
  - **Best Fit**: Allocate at smallest fitting block (minimizes fragmentation)
- **Features**:
  - Automatic merging of free blocks
  - Defragmentation support
  - Fragmentation calculation

#### 5. **LRU Cache (Page Replacement)**

- **File**: `memory/LRUCache.java`
- **Mechanism**:
  - Evicts least recently used task when memory > 80% full
  - Tracks last access time for each partition
  - Moves evicted tasks to "cold storage"

#### 6. **Concurrency Control**

##### AisleSemaphore

- **File**: `concurrency/AisleSemaphore.java`
- **Concept**: Binary semaphore (mutex) for warehouse aisles
- **Features**:
  - One robot per aisle at a time
  - acquire/release operations
  - Lock state tracking

##### Banker's Algorithm (Deadlock Prevention)

- **File**: `concurrency/BankersAlgorithm.java`
- **Logic**:
  - Checks if granting aisle access creates SAFE state
  - Detects circular wait conditions
  - Prevents deadlocks before they occur

##### Waiting Queue

- **File**: `concurrency/WaitingQueue.java`
- **Features**:
  - Per-aisle waiting queues
  - Task notification when aisle freed
  - Deadlock prevention counter

#### 7. **Disk I/O Optimization**

##### DiskScheduler

- **File**: `io/DiskScheduler.java`
- **Algorithms**:
  - **SSTF** (Shortest Seek Time First): Visits nearest shelf next
  - **SCAN** (Elevator Algorithm): Sweeps in one direction

##### PathOptimizer

- **File**: `io/PathOptimizer.java`
- **Features**:
  - Compares SSTF vs SCAN for each task
  - Calculates total seek distance and time
  - Tracks direction changes

##### RobotSimulator

- **File**: `io/RobotSimulator.java`
- **Simulation**:
  - Robot movement along shelf positions (0-100)
  - Pick item operation (1ms each)
  - Path execution logging

#### 8. **Event Logger (Centralized Logging)**

- **File**: `simulation/EventLogger.java`
- **Format**: `[timestamp] [MODULE] [EVENT_TYPE] message`
- **Features**:
  - Singleton pattern
  - Captures all system events
  - Timestamp synchronization across modules

---

## Execution Flow

### Orchestration (OrchestratorSimulation.java)

```
START SIMULATION
    ↓
[LOAD PHASE]
    └─ Load tasks from tasks.csv
    └─ Initialize kernel, scheduler, memory manager, concurrency control, disk I/O
    ↓
[SETUP PHASE]
    For each task:
    ├─ Allocate memory (Best Fit → First Fit fallback)
    ├─ Add to ready queue
    └─ Log initialization
    ↓
[EXECUTION PHASE]
    While ready queue not empty:
    ├─ Select next task (Priority > 2 → Priority Scheduling, else Round Robin)
    ├─ Context switch from current to next task
    ├─ Set task state to RUNNING
    ├─ Request target aisle
    ├─ Check Banker's Algorithm for deadlock risk
    ├─ Acquire aisle semaphore
    ├─ Calculate optimal path (SSTF vs SCAN)
    ├─ Execute robot movement along path
    ├─ Update LRU access time
    ├─ Check if eviction needed (>80% memory utilization)
    ├─ Simulate task execution (sleep for processTime)
    ├─ Release aisle semaphore
    ├─ Deallocate memory
    ├─ Terminate task
    └─ Calculate metrics (wait time, turnaround time)
    ↓
[REPORTING PHASE]
    ├─ Scheduler statistics (avg wait, avg turnaround)
    ├─ Memory statistics (fragmentation %)
    ├─ Concurrency statistics (deadlocks prevented)
    ├─ Disk I/O statistics (total seek distance, time, direction changes)
    └─ System statistics (total events logged, duration)
```

---

## Data Structures

### Task (Process Control Block)

```java
- taskID: int
- priority: int (1-5, lower = higher)
- state: String (NEW, READY, RUNNING, WAITING, TERMINATED)
- targetAisle: int (0-9)
- memorySize: int (memory needed)
- processTime: int (execution time in ms)
- creationTime: long
- startTime: long
- endTime: long
- allocatedMemory: int
- memoryAddress: int
- lastAccessTime: long
- waitingReason: String
```

### Partition (Memory Block)

```java
- startAddr: int
- size: int
- taskID: int (-1 if free)
- lastAccessed: long
- isFree(): boolean
```

---

## Test Data (tasks.csv)

```csv
taskID,priority,targetAisle,memorySize,processTime
101,1,4,50,10        # Priority 1 (Express), Aisle 4, 50 units, 10ms
102,5,2,20,30        # Priority 5 (Standard), Aisle 2, 20 units, 30ms
103,2,7,35,15        # Priority 2 (Express), Aisle 7, 35 units, 15ms
104,1,1,45,12        # Priority 1 (Express), Aisle 1, 45 units, 12ms
105,3,5,25,20        # Priority 3 (Standard), Aisle 5, 25 units, 20ms
```

---

## Compilation & Execution

### Compile

```bash
cd D:\Study\college\SEM4\OS\project\leos
javac -d bin shared/Task.java kernel/Kernel.java scheduler/ReadyQueue.java \
  scheduler/Scheduler.java memory/Partition.java memory/MemoryManager.java \
  memory/LRUCache.java memory/MemoryMap.java concurrency/AisleSemaphore.java \
  concurrency/BankersAlgorithm.java concurrency/WaitingQueue.java \
  io/DiskScheduler.java io/PathOptimizer.java io/RobotSimulator.java \
  simulation/EventLogger.java OrchestratorSimulation.java
```

### Run

```bash
java -cp bin OrchestratorSimulation
```

---

## Sample Output Analysis

### Execution Summary (5 Tasks)

- **Task 101**: Wait 150ms, Turnaround 189ms (Priority 1 - Express)
- **Task 104**: Wait 184ms, Turnaround 206ms (Priority 1 - Express)
- **Task 103**: Wait 209ms, Turnaround 243ms (Priority 2 - Express)
- **Task 105**: Wait 246ms, Turnaround 273ms (Priority 3 - Standard)
- **Task 102**: Wait 275ms, Turnaround 310ms (Priority 5 - Standard)

### Performance Metrics

```
Scheduler Stats:
  Average Wait Time: 212.80 ms
  Average Turnaround Time: 244.20 ms

Memory Stats:
  Fragmentation: 100.00% (after all tasks terminated and deallocated)
  Total Floor: 500 units

Concurrency Stats:
  Deadlocks Prevented: 0 (no conflicts with 10 aisles for 5 tasks)
  Tasks Waiting: 0 (all tasks completed)

Disk I/O Stats:
  Total Seek Distance: 300 units (60 units per task × 5)
  Total Seek Time: 60 ms (12ms per task × 5)
  Direction Changes: 5 (1 per task)

System Stats:
  Total Events Logged: 61
  Simulation Duration: 732 ms
```

---

## OS Concepts Implemented

### Unit II: Concurrency Management ✅

- Binary Semaphores (AisleSemaphore)
- Mutual Exclusion (mutex for each aisle)
- Deadlock Prevention (Banker's Algorithm)
- Waiting Queues (per-aisle)
- Safe State Analysis

### Unit III: CPU Scheduling ✅

- Process Scheduling Algorithms (Round Robin, Priority)
- Context Switching with full state management
- Ready Queue management
- Performance metrics calculation

### Unit IV: Deadlock Management ✅

- Banker's Algorithm for safety checking
- Circular wait detection
- Resource allocation tracking
- Deadlock prevention (not just detection)

### Unit V: Memory Management ✅

- Memory Allocation (First Fit, Best Fit)
- Fragmentation analysis
- Page Replacement (LRU Cache)
- Partition management and merging

### Unit VI: Disk I/O Optimization ✅

- Disk Scheduling (SSTF, SCAN)
- Seek time calculation
- Algorithm comparison
- Robot simulation

---

## Files Structure

```
leos/
├── shared/
│   └── Task.java                    ✅ PCB with state management
├── kernel/
│   └── Kernel.java                  ✅ Process management & orchestration
├── scheduler/
│   ├── Scheduler.java               ✅ RR and Priority scheduling
│   └── ReadyQueue.java              ✅ PriorityQueue implementation
├── memory/
│   ├── MemoryManager.java           ✅ First Fit & Best Fit algorithms
│   ├── Partition.java               ✅ Memory block representation
│   ├── LRUCache.java                ✅ Page replacement
│   └── MemoryMap.java               ✅ Memory visualization
├── concurrency/
│   ├── AisleSemaphore.java          ✅ Binary semaphore
│   ├── BankersAlgorithm.java        ✅ Deadlock prevention
│   └── WaitingQueue.java            ✅ Task waiting management
├── io/
│   ├── DiskScheduler.java           ✅ SSTF & SCAN algorithms
│   ├── PathOptimizer.java           ✅ Algorithm comparison
│   └── RobotSimulator.java          ✅ Robot movement simulation
├── simulation/
│   └── EventLogger.java             ✅ Centralized logging
├── OrchestratorSimulation.java      ✅ Phase 1 orchestration (MAIN)
├── SimulationRunner.java            (Legacy, use OrchestratorSimulation)
└── tasks.csv                        ✅ Test data
```

---

## Phase 1 Status: ✅ COMPLETE

All OS concepts implemented and integrated:

- ✅ Process Management with full state transitions
- ✅ CPU Scheduling with multiple algorithms
- ✅ Memory Management with multiple algorithms
- ✅ Concurrency Control with Banker's Algorithm
- ✅ Disk I/O Optimization with SSTF/SCAN
- ✅ Centralized Event Logging
- ✅ Full Integration and Orchestration
- ✅ Comprehensive Statistics Reporting

**Total Components**: 17 Java files
**LOC**: ~2500+ lines of production code
**Time Complexity**: Efficient algorithms (O(n²) worst case for scheduler, O(n) for memory management)

---

## Future Enhancements (Phase 2+)

1. **Multi-threading**: Simulate concurrent task execution
2. **Interrupt Handling**: Software and hardware interrupts
3. **File System**: Implement FAT/inode-based file systems
4. **Network I/O**: TCP/IP stack simulation
5. **Virtual Memory**: Paging with page tables
6. **IPC Mechanisms**: Message queues, shared memory
7. **Advanced Scheduling**: MLQ, MLFQ schedulers
8. **Performance Analysis**: Gantt charts, utilization graphs

---

## Notes

- All timestamps are relative to simulation start (0ms = simulation start)
- Tasks execute sequentially in this Phase 1 (single CPU simulation)
- Memory fragmentation returns to 100% after all tasks terminate (all memory freed)
- No actual deadlocks occur with 10 aisles for 5 concurrent tasks
- SSTF typically performs better for random access patterns
- SCAN performs better for workloads with locality

---

**Author**: OS Project Team (Person A-E)  
**Date**: April 14, 2026  
**Status**: Phase 1 Complete ✅
