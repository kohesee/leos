package memory;
}    }        System.out.println("✓ Optimized path generated to minimize travel distance");        System.out.println("Total seek distance: " + totalSeek + " units");        System.out.println("SSTF Movement: " + movement);        System.out.println("Requested shelves: " + aisles);        System.out.println("Robot starting at Shelf " + startAisle);                int totalSeek = pathOptimizer.totalSeekDistance(startAisle, sstfOrder);        String movement = pathOptimizer.formatMovementTrace(startAisle, sstfOrder);                List<Integer> sstfOrder = pathOptimizer.sstfOrder(startAisle, aisles);        int startAisle = 0;        // Get SSTF order starting from aisle 0        }            aisles.add(t.getTargetAisle());        for (Task t : tasks) {        List<Integer> aisles = new ArrayList<>();        // Create a list of target aisles from tasks    private static void demonstrateSSTV(PathOptimizer pathOptimizer, List<Task> tasks) {     */     * Demonstrates SSTF algorithm for path optimization    /**    }        System.out.println("✓ Aisles accessed sequentially with mutual exclusion enforced");        }            safetyGuard.leaveAisle(t.getTaskID(), aisle);            }                e.printStackTrace();            } catch (InterruptedException e) {                Thread.sleep(50);            try {            safetyGuard.enterAisle(t.getTaskID(), aisle);                        int aisle = t.getTargetAisle();            Task t = tasks.get(i);        for (int i = 0; i < Math.min(2, tasks.size()); i++) {        int taskIndex = 0;        // Simulate first two tasks trying to access aisles    private static void demonstrateAisleConflict(SafetyGuard safetyGuard, List<Task> tasks) {     */     * Demonstrates aisles being locked/released with semaphores    /**    }        System.out.println();        System.out.println("  6. [Person E] PathOptimizer.java: SSTF disk scheduling");        System.out.println("  5. [Person C] SafetyGuard.java: Aisle mutual exclusion with Semaphores");        System.out.println("  4. [Person B] Scheduler.java: Round Robin scheduling");        System.out.println("  3. [Person A] Kernel.java: State transitions (NEW -> READY -> TERMINATED)");        System.out.println("  2. [Person D] MemoryManager.java: Allocated memory using First Fit");        System.out.println("  1. [Person A] Task.java: Created and managed 5 tasks (PCB)");        System.out.println("✓ All 6 components demonstrated:");        System.out.println("=".repeat(80));        System.out.println("MVP DEMONSTRATION COMPLETE");        System.out.println("=".repeat(80));        // ========== FINAL SUMMARY ==========        System.out.println();        }            memoryManager.free(t.getTaskID());            kernel.terminateTask(t);            t.setStartTime(System.currentTimeMillis());        for (Task t : tasks) {        System.out.println("-".repeat(80));        System.out.println("PHASE 7: Task Termination");        // ========== PHASE 7: Task Termination ==========        System.out.println();        demonstrateSSTV(pathOptimizer, tasks);        System.out.println("-".repeat(80));        System.out.println("PHASE 6: Path Optimization (SSTF - Shortest Seek Time First)");        // ========== PHASE 6: Path Optimization - SSTF ==========        System.out.println();        demonstrateAisleConflict(safetyGuard, tasks);        System.out.println("-".repeat(80));        System.out.println("PHASE 5: Mutual Exclusion (Semaphore-based Aisles)");        // ========== PHASE 5: Mutual Exclusion - Aisle Access ==========        }            System.out.println();            }                System.out.println("  ...no next task (this is the last one)");            } else {                System.out.println("  ...next Task " + tasks.get(i + 1).getTaskID());            if (i + 1 < tasks.size()) {            // Show next task if available                        System.out.println("  Robot processing Task " + currentTask.getTaskID());            System.out.println("Cycle " + (i + 1) + ":");                        Task currentTask = tasks.get(i);        for (int i = 0; i < tasks.size(); i++) {        System.out.println("-".repeat(80));        System.out.println("PHASE 4: Round Robin Scheduling");        // ========== PHASE 4: Scheduler - Round Robin Processing ==========        System.out.println();        System.out.println("✓ All tasks moved to READY state");        scheduler.addAllToReadyQueue(tasks);        System.out.println("-".repeat(80));        System.out.println("PHASE 3: Task State Transitions");        // ========== PHASE 3: Process State Transitions ==========        System.out.println();        }            memoryManager.allocate(t);        for (Task t : tasks) {        System.out.println("-".repeat(80));        System.out.println("PHASE 2: Memory Allocation (First Fit)");        // ========== PHASE 2: Allocate Memory for Each Task ==========        System.out.println();        }            return;            System.out.println("✗ Error loading CSV: " + e.getMessage());        } catch (Exception e) {            System.out.println("✓ Loaded " + tasks.size() + " tasks from CSV");            tasks = kernel.loadTasksFromCsv("tasks.csv");        try {        List<Task> tasks = new ArrayList<>();        System.out.println("-".repeat(80));        System.out.println("PHASE 1: Task Pool Creation");        // ========== PHASE 1: Load Tasks from CSV ==========        PathOptimizer pathOptimizer = new PathOptimizer();        MemoryManager memoryManager = new MemoryManager(WAREHOUSE_FLOOR_SIZE);        SafetyGuard safetyGuard = new SafetyGuard(NUM_AISLES);        Scheduler scheduler = new Scheduler();        Kernel kernel = new Kernel();        // Initialize all components        System.out.println();        System.out.println("=".repeat(80));        System.out.println("OS SIMULATION: 1-WEEK MVP INTEGRATION TEST");        System.out.println("=".repeat(80));    public static void main(String[] args) {    private static final int NUM_AISLES = 5;    private static final int WAREHOUSE_FLOOR_SIZE = 500;public class IntegrationTest { */ * Output: A unified "Log Stream" showing the order specified in the MVP. *  * 6. Person E (PathOptimizer.java): SSTF for disk/shelf movement * 5. Person C (SafetyGuard.java): Mutual Exclusion with Semaphores * 4. Person B (Scheduler.java): Ready Queue and Round Robin scheduling * 3. Person A (Kernel.java): 5-State Lifecycle (NEW -> READY) * 2. Person D (MemoryManager.java): Memory allocation with First Fit * 1. Person A (Task.java): Task creation and state management (PCB) * Demonstrates the complete 1-week MVP ("Walking Skeleton") with all 6 components: *  * IntegrationTest.java/**import java.util.List;import java.util.Arrays;import java.util.ArrayList;import shared.Task;import io.PathOptimizer;import memory.MemoryManager;import concurrency.SafetyGuard;// MemoryManager.java
// First Fit allocation for a fixed-size warehouse floor

import shared.Task;

public class MemoryManager {
    private static final int EMPTY = -1;
    private final int[] floor;

    public MemoryManager(int floorSize) {
        if (floorSize <= 0) {
            throw new IllegalArgumentException("floorSize must be positive");
        }
        this.floor = new int[floorSize];
        for (int i = 0; i < floor.length; i++) {
            floor[i] = EMPTY;
        }
    }

    public boolean allocate(Task task) {
        return allocate(task.getTaskID(), task.getMemorySize());
    }

    public boolean allocate(int taskID, int size) {
        if (size <= 0 || size > floor.length) {
            return false;
        }

        int startIndex = findFirstFit(size);
        if (startIndex == -1) {
            System.out.println("Memory allocation failed for Task " + taskID + " (size " + size + ")");
            return false;
        }

        int endIndex = startIndex + size - 1;
        for (int i = startIndex; i <= endIndex; i++) {
            floor[i] = taskID;
        }

        System.out.println(
                "Task " + taskID + " allocated " + size + " units at Floor Index " + startIndex + "-" + endIndex
        );
        return true;
    }

    public void free(int taskID) {
        for (int i = 0; i < floor.length; i++) {
            if (floor[i] == taskID) {
                floor[i] = EMPTY;
            }
        }
    }

    private int findFirstFit(int size) {
        int freeCount = 0;
        int startIndex = 0;

        for (int i = 0; i < floor.length; i++) {
            if (floor[i] == EMPTY) {
                if (freeCount == 0) {
                    startIndex = i;
                }
                freeCount++;
                if (freeCount == size) {
                    return startIndex;
                }
            } else {
                freeCount = 0;
            }
        }

        return -1;
    }
}
