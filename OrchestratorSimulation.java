import concurrency.AisleSemaphore;
import concurrency.BankersAlgorithm;
import concurrency.WaitingQueue;
import io.PathOptimizer;
import io.RobotSimulator;
import kernel.Kernel;
import memory.LRUCache;
import memory.MemoryManager;
import memory.MemoryMap;
import scheduler.ReadyQueue;
import scheduler.Scheduler;
import shared.Task;
import simulation.EventLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * OrchestratorSimulation.java
 * Phase 1: Complete OS Simulation with all modules integrated
 * Orchestrates: Process Management, CPU Scheduling, Memory Management, 
 * Concurrency Control, and Disk I/O
 */
public class OrchestratorSimulation {
    private static final int ROBOT_SPEED = 5;
    private static final int NUM_AISLES = 10;
    private static final long TIME_QUANTUM_MS = 5;

    private Kernel kernel;
    private Scheduler scheduler;
    private ReadyQueue readyQueue;
    private MemoryManager memoryManager;
    private MemoryMap memoryMap;
    private LRUCache lruCache;
    private PathOptimizer pathOptimizer;
    private RobotSimulator robotSimulator;

    // Concurrency control
    private AisleSemaphore[] aisles;
    private BankersAlgorithm bankersAlgorithm;
    private WaitingQueue waitingQueue;

    private EventLogger eventLogger;
    private long simulationStartTime;

    public OrchestratorSimulation() {
        this.eventLogger = EventLogger.getInstance();
        this.simulationStartTime = System.currentTimeMillis();
        initializeComponents();
    }

    private void initializeComponents() {
        eventLogger.log("ORCHESTRATOR", "INIT", "Initializing all OS components...");

        // Kernel
        this.kernel = new Kernel();

        // Scheduler & Ready Queue
        this.scheduler = new Scheduler();
        this.readyQueue = new ReadyQueue();

        // Memory Management (500-unit floor)
        this.memoryManager = new MemoryManager(500);
        this.memoryMap = new MemoryMap(memoryManager);
        this.lruCache = new LRUCache(memoryManager);

        // Concurrency Control (10 aisles)
        this.aisles = new AisleSemaphore[NUM_AISLES];
        for (int i = 0; i < NUM_AISLES; i++) {
            aisles[i] = new AisleSemaphore(i);
        }
        this.bankersAlgorithm = new BankersAlgorithm(NUM_AISLES, aisles);
        this.waitingQueue = new WaitingQueue(NUM_AISLES);

        // Disk I/O
        this.pathOptimizer = new PathOptimizer();
        this.robotSimulator = new RobotSimulator(0);

        eventLogger.log("ORCHESTRATOR", "INIT", "All components initialized successfully");
    }

    public void runSimulation(String csvPath) throws Exception {
        eventLogger.log("ORCHESTRATOR", "START", "Starting OS simulation with CSV: " + csvPath);

        // Phase 1: Load tasks from CSV
        List<Task> allTasks = kernel.loadTasksFromCsv(csvPath);
        eventLogger.log("ORCHESTRATOR", "LOAD", "Loaded " + allTasks.size() + " tasks from CSV");

        if (allTasks.isEmpty()) {
            eventLogger.log("ORCHESTRATOR", "ERROR", "No tasks loaded from CSV!");
            return;
        }

        // Phase 2: Initial setup - allocate memory and add to ready queue
        for (Task task : allTasks) {
            eventLogger.log("ORCHESTRATOR", "SETUP", "Setting up Task " + task.getTaskID());

            // Memory allocation
            int allocatedAddress = memoryManager.allocateBestFit(task.getTaskID(), task.getMemorySize());
            if (allocatedAddress == -1) {
                allocatedAddress = memoryManager.firstFit(task.getMemorySize(), task.getTaskID());
            }

            if (allocatedAddress != -1) {
                task.setAllocatedMemory(task.getMemorySize());
                task.setMemoryAddress(allocatedAddress);
                eventLogger.log("MEMORY", "ALLOCATE", "Task " + task.getTaskID() + " allocated at " + allocatedAddress);
            } else {
                eventLogger.log("MEMORY", "FAILED", "Task " + task.getTaskID() + " memory allocation failed");
                continue; // Skip this task
            }

            // Add to ready queue
            readyQueue.enqueue(task);
            eventLogger.log("SCHEDULER", "QUEUE", "Task " + task.getTaskID() + " added to Ready Queue");
        }

        eventLogger.log("ORCHESTRATOR", "STATUS", "Initial setup complete. Ready to execute tasks.");
        Thread.sleep(100);

        // Phase 3: Main execution loop
        Task currentTask = null;
        int tasksCompleted = 0;

        while (!readyQueue.isEmpty()) {
            // Select next task
            Task nextTask = selectNextTask(scheduler, readyQueue);
            if (nextTask == null) {
                break;
            }

            eventLogger.log("ORCHESTRATOR", "EXECUTE", "Starting execution of Task " + nextTask.getTaskID());

            // Context switch
            scheduler.switchContext(currentTask, nextTask);
            currentTask = nextTask;

            // Set task to RUNNING state
            kernel.runTask(nextTask);

            int targetAisle = nextTask.getTargetAisle();
            eventLogger.log("ORCHESTRATOR", "AISLE_REQUEST", "Task " + nextTask.getTaskID() + 
                    " requesting Aisle " + targetAisle);

            // Concurrency Control: Check with Banker's Algorithm
            boolean isSafe = bankersAlgorithm.isSafeState(nextTask, targetAisle);

            if (!isSafe && bankersAlgorithm.checkDeadlock(nextTask)) {
                eventLogger.log("CONCURRENCY", "DEADLOCK_PREVENTED", "Task " + nextTask.getTaskID() + 
                        " deadlock risk detected");
                waitingQueue.incrementDeadlocksPrevented();
                waitingQueue.addToQueue(nextTask.getTaskID(), targetAisle);
                kernel.waitTask(nextTask, "AISLE_LOCK_DEADLOCK");
                readyQueue.enqueue(nextTask); // Re-queue for later attempt
                continue;
            }

            // Acquire aisle semaphore
            aisles[targetAisle].acquire(nextTask.getTaskID());
            bankersAlgorithm.registerHolding(nextTask.getTaskID(), targetAisle);
            eventLogger.log("CONCURRENCY", "AISLE_ACQUIRED", "Task " + nextTask.getTaskID() + 
                    " acquired Aisle " + targetAisle);

            // Disk I/O: Calculate optimal path
            List<Integer> shelfRequests = buildShelfRequests(nextTask);
            PathOptimizer.ComparisonResult comparison = pathOptimizer.compareAlgorithms(
                    shelfRequests,
                    robotSimulator.getCurrentPos(),
                    ROBOT_SPEED
            );

            List<Integer> selectedPath = selectPath(comparison);
            String selectedAlgorithm = comparison.getBetterAlgorithm().equals("TIE") ? "SSTF" : 
                    comparison.getBetterAlgorithm();

            eventLogger.log("DISK", "PATH_SELECTED", "Task " + nextTask.getTaskID() + 
                    " using " + selectedAlgorithm + " algorithm");

            // Execute robot movement
            robotSimulator.executePath(nextTask, selectedPath);
            pathOptimizer.recordTaskMetrics(nextTask.getTaskID(), robotSimulator.getCurrentPos(), 
                    selectedPath, ROBOT_SPEED);

            // Update memory access time
            lruCache.markAccess(nextTask.getTaskID());

            // Check LRU eviction
            if (lruCache.isFull()) {
                eventLogger.log("MEMORY", "LRU_EVICTION", "Memory >80% full, evicting LRU task");
                lruCache.evictLRU();
            }

            // Simulate task execution
            eventLogger.log("ORCHESTRATOR", "RUNNING", "Task " + nextTask.getTaskID() + 
                    " executing for " + nextTask.getProcessTime() + "ms");
            Thread.sleep(nextTask.getProcessTime());

            // Release aisle semaphore
            aisles[targetAisle].release(nextTask.getTaskID());
            bankersAlgorithm.unregisterHolding(nextTask.getTaskID());
            eventLogger.log("CONCURRENCY", "AISLE_RELEASED", "Task " + nextTask.getTaskID() + 
                    " released Aisle " + targetAisle);

            // Deallocate memory
            memoryManager.deallocate(nextTask.getTaskID());
            eventLogger.log("MEMORY", "DEALLOCATE", "Memory deallocated for Task " + nextTask.getTaskID());

            // Terminate task
            kernel.terminateTask(nextTask);
            scheduler.calculateMetrics(nextTask);

            tasksCompleted++;
            eventLogger.log("ORCHESTRATOR", "COMPLETE", "Task " + nextTask.getTaskID() + 
                    " execution completed");
        }

        eventLogger.log("ORCHESTRATOR", "FINISH", "Simulation finished. Tasks completed: " + tasksCompleted);
        printFinalStatistics();
    }

    private Task selectNextTask(Scheduler scheduler, ReadyQueue readyQueue) {
        Task candidate = readyQueue.peek();
        if (candidate == null) {
            return null;
        }

        // Priority-based or Round Robin selection
        if (candidate.getPriority() <= 2) {
            return scheduler.schedulePriority(readyQueue);
        }
        return scheduler.scheduleRoundRobin(readyQueue);
    }

    private List<Integer> buildShelfRequests(Task task) {
        int base = Math.max(0, Math.min(100, task.getTargetAisle() * 10));
        List<Integer> shelves = new ArrayList<>();
        shelves.add(base);
        shelves.add(Math.min(100, base + 20));
        shelves.add(Math.max(0, base - 10));
        return shelves;
    }

    private List<Integer> selectPath(PathOptimizer.ComparisonResult comparison) {
        if (comparison.getBetterAlgorithm().equals("SCAN")) {
            return comparison.getScanPath();
        }
        return comparison.getSstfPath();
    }

    private void printFinalStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PHASE 1 SIMULATION COMPLETE - FINAL STATISTICS");
        System.out.println("=".repeat(60));

        // Scheduler stats
        scheduler.printSchedulerStats();

        // Memory stats
        System.out.println("\n========== MEMORY STATISTICS ==========");
        System.out.println(String.format("Fragmentation: %.2f%%", memoryManager.getFragmentation()));
        System.out.println("Total Memory: 500 units");
        System.out.println("=========================================\n");

        // Concurrency stats
        waitingQueue.printStats();

        // Disk I/O stats
        System.out.println("\n========== DISK I/O STATISTICS ==========");
        System.out.println("Total Seek Distance: " + pathOptimizer.getCumulativeSeekDistance() + " units");
        System.out.println("Total Seek Time: " + pathOptimizer.getCumulativeSeekTime() + " ms");
        System.out.println("Total Direction Changes: " + pathOptimizer.getCumulativeDirectionChanges());
        System.out.println("Robot Speed: " + ROBOT_SPEED + " units/ms");
        System.out.println("=========================================\n");

        // System stats
        System.out.println("\n========== SYSTEM STATISTICS ==========");
        System.out.println("Total Events Logged: " + eventLogger.getEventCount());
        System.out.println("Simulation Duration: " + 
                (System.currentTimeMillis() - simulationStartTime) + " ms");
        System.out.println("=========================================\n");

        System.out.println("=".repeat(60));
        System.out.println("PHASE 1 COMPLETE");
        System.out.println("=".repeat(60));
    }

    public static void main(String[] args) {
        try {
            System.out.println("╔" + "═".repeat(58) + "╗");
            System.out.println("║" + " ".repeat(10) + "OPERATING SYSTEM SIMULATION - PHASE 1" + 
                    " ".repeat(12) + "║");
            System.out.println("╚" + "═".repeat(58) + "╝");
            System.out.println();

            OrchestratorSimulation orchestrator = new OrchestratorSimulation();
            orchestrator.runSimulation("tasks.csv");

        } catch (Exception e) {
            System.err.println("ERROR: Simulation failed!");
            e.printStackTrace();
        }
    }
}
