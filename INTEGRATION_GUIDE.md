# LEOS Project - Integration Guide

## ✅ Person A (Kernel & Process Architect) - COMPLETE

### Deliverables:

1. **`shared/Task.java`** - Process Control Block (PCB)
   - ✅ Core attributes: `taskID`, `priority`, `state`, `targetAisle`, `memorySize`, `processTime`
   - ✅ Timestamps: `creationTime`, `startTime`, `endTime`
   - ✅ Memory tracking: `allocatedMemory`, `memoryAddress`
   - ✅ LRU support: `lastAccessTime`, `updateLastAccessTime()`
   - ✅ State validation: Only allows NEW → READY → RUNNING → WAITING/TERMINATED
   - ✅ Waiting support: `waitingReason`
   - ✅ Aisle tracking: `aisleAccessCount`

2. **`kernel/Kernel.java`** - Kernel Management
   - ✅ `jobTable` - List of all tasks
   - ✅ `readyQueue` - Ready queue for Person B (Scheduler)
   - ✅ `waitingQueue` - Waiting queue for Person C (Concurrency)
   - ✅ `eventLog` - Centralized logging system
   - ✅ Integration methods:
     - `validateTask()` - NEW → READY
     - `runTask()` - READY → RUNNING
     - `waitTask()` - RUNNING → WAITING
     - `resumeTask()` - WAITING → READY
     - `terminateTask()` - RUNNING/WAITING → TERMINATED
   - ✅ System stats & event log printing

3. **`tasks.csv`** - Sample input file
   - Format: `TaskID,Priority,TargetAisle,MemorySize,ProcessTime`

---

## 📋 Next Steps - Integration Points for Each Person

### Person B (Scheduler) - NEEDS:

- Read from `kernel.Kernel.readyQueue` (ready tasks)
- Implement `Scheduler.selectNextTask()` that pulls from ready queue
- Use `Kernel.runTask(task)` to transition selected task to RUNNING
- Use `Kernel.terminateTask(task)` when task completes
- Calculate and print Wait Time & Turnaround Time

### Person C (Concurrency Guard) - NEEDS:

- Access `kernel.Kernel.waitingQueue` for tasks awaiting resources
- Implement `AisleSemaphore.acquire(taskID)` to lock aisles
- Implement `BankersAlgorithm.isSafeState(task, aisle)` before allowing access
- Use `Kernel.waitTask(task, reason)` to move task to WAITING if deadlock detected
- Use `Kernel.resumeTask(task)` when aisle is released

### Person D (Memory Manager) - NEEDS:

- `Task.getAllocatedMemory()` and `setAllocatedMemory()`
- `Task.getMemoryAddress()` and `setMemoryAddress()`
- `Task.updateLastAccessTime()` for LRU tracking
- Allocate memory in `Kernel.createTask()` flow
- Deallocate in `Kernel.terminateTask()` flow

### Person E (Disk Optimizer) - NEEDS:

- `Task.incrementAisleAccessCount()` to track visits
- Calculate paths when task needs to move within aisle
- Log path planning events with timestamps

---

## 🔄 Integration Workflow

```
1. KERNEL STARTS
   └─ Load tasks from CSV → Creates Task objects with state=NEW

2. FOR EACH TASK:
   ├─ PERSON A: Validate task → validateTask() → state=READY, added to readyQueue
   ├─ PERSON D: Allocate memory → setMemoryAddress(), setAllocatedMemory()
   ├─ PERSON B: Select from readyQueue → runTask() → state=RUNNING
   ├─ PERSON C: Acquire aisle semaphore (check safe state first)
   ├─ PERSON E: Calculate & execute path using SSTF or SCAN
   ├─ Wait for processTime ms
   ├─ PERSON C: Release aisle semaphore
   ├─ PERSON D: Deallocate memory
   ├─ PERSON A: Terminate task → terminateTask() → state=TERMINATED
   └─ PERSON B: Log performance metrics

3. PRINT FINAL STATS
   └─ Kernel.printSystemStats(), printEventLog()
```

---

## 📤 Expected Console Output Format

```
[0ms] KERNEL: Task 101 created (NEW)
[1ms] KERNEL: Task 101 validated and moved to READY queue (READY)
[2ms] MEMORY: Best-Fit allocated 50 units at address 0 for Task 101
[3ms] SCHEDULER: Task 101 selected by Round Robin
[4ms] KERNEL: Task 101 transitioned to RUNNING
[5ms] CONCURRENCY: Task 101 requesting Aisle 4
[6ms] CONCURRENCY: Banker's Algorithm checking... SAFE STATE APPROVED
[7ms] CONCURRENCY: Aisle 4 Semaphore ACQUIRED by Task 101
[8ms] DISK: Task 101 executing path: [10, 20, 30] using SSTF
[14ms] CONCURRENCY: Aisle 4 Semaphore RELEASED by Task 101
[15ms] MEMORY: Task 101 deallocated 50 units from address 0
[16ms] KERNEL: Task 101 terminated (TERMINATED)
[17ms] SCHEDULER: Task 101 Stats → Wait: 1ms, Turnaround: 16ms
```

---

## 🧪 Testing Person A's Work

```bash
javac shared/Task.java kernel/Kernel.java
java -cp . kernel.Kernel
```

Expected: Should load tasks.csv, create tasks, and log events

---

## ✅ Task Object Methods Reference

### Basic Info

- `int getTaskID()`
- `int getPriority()`
- `String getState()`
- `int getTargetAisle()`
- `int getMemorySize()`
- `int getProcessTime()`

### Timestamps

- `long getCreationTime()`
- `long getStartTime()` / `void setStartTime(long)`
- `long getEndTime()` / `void setEndTime(long)`
- `long getWaitTime()` - calculated as (startTime - creationTime)
- `long getTurnaroundTime()` - calculated as (endTime - creationTime)

### Memory Management

- `int getAllocatedMemory()` / `void setAllocatedMemory(int)`
- `int getMemoryAddress()` / `void setMemoryAddress(int)`

### State Management

- `void setState(String)` - Only allows valid states
- Valid states: NEW, READY, RUNNING, WAITING, TERMINATED

### LRU Tracking

- `long getLastAccessTime()`
- `void updateLastAccessTime()` - Updates to current time

### Waiting

- `String getWaitingReason()` / `void setWaitingReason(String)`

### Aisle Tracking

- `int getAisleAccessCount()`
- `void incrementAisleAccessCount()`

---

## 🎯 Key Kernel Methods for Integration

```java
// Create and load tasks
List<Task> loadTasksFromCsv(String path) throws IOException

// Task lifecycle
void createTask(int taskID, ...)
void terminateTask(Task t)
void validateTask(Task t)
void runTask(Task t)
void waitTask(Task t, String reason)
void resumeTask(Task t)

// Data access
List<Task> getJobTable()
Queue<Task> getReadyQueue()
Queue<Task> getWaitingQueue()
Task getTaskById(int taskID)
List<String> getEventLog()

// Output
void printSystemStats()
void printEventLog()
```

---

## 📌 Important Notes

1. **Task states are validated** - Invalid state transitions will throw `IllegalArgumentException`
2. **Centralized logging** - All events logged to `eventLog` with relative timestamps from kernel start
3. **Thread-safe operations needed** - When Person B/C/E access queues, ensure synchronization
4. **Memory address tracking** - Address identifies location in warehouse floor (0-500 range)
5. **Aisle semaphores** - Each aisle (1-10) needs its own semaphore managed by Person C

---

## 🔗 Commit Order Reminder

1. ✅ **Person A** → Commit `shared/Task.java` + `kernel/Kernel.java`
2. → **Person D** → Memory Manager
3. → **Person B** → Scheduler
4. → **Person C** → Concurrency Guard
5. → **Person E** → Disk Optimizer
6. → **Person A** → Final Integration in main()
