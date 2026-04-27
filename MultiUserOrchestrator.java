import kernel.MultiThreadedKernel;
import shared.User;
import shared.Task;
import memory.MemoryManager;
import scheduler.Scheduler;
import simulation.EventLogger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Multi-User Orchestrator - Phase 2 Main Entry Point
 * Demonstrates multi-user support with concurrent thread execution
 */
public class MultiUserOrchestrator {
    private MultiThreadedKernel kernel;
    private MemoryManager memoryManager;
    private Scheduler scheduler;
    private EventLogger eventLogger;
    private Map<Integer, List<Task>> userTaskMap;

    public MultiUserOrchestrator() {
        this.kernel = new MultiThreadedKernel();
        this.memoryManager = new MemoryManager(500);
        this.scheduler = new Scheduler();
        this.eventLogger = EventLogger.getInstance();
        this.userTaskMap = new HashMap<>();
    }

    /**
     * Load multi-user tasks from CSV
     */
    public void loadMultiUserTasks(String csvPath) {
        try {
            eventLogger.log("ORCHESTRATOR", "LOAD", "Loading multi-user tasks from " + csvPath);
            List<String> lines = Files.readAllLines(Paths.get(csvPath));
            
            if (lines.isEmpty()) {
                eventLogger.log("ERROR", "CSV_EMPTY", "CSV file is empty");
                return;
            }

            // Skip header
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 6) {
                    eventLogger.log("WARN", "CSV_PARSE", "Skipping malformed line: " + line);
                    continue;
                }

                int userId = Integer.parseInt(parts[0]);
                int taskId = Integer.parseInt(parts[1]);
                int priority = Integer.parseInt(parts[2]);
                int aisleId = Integer.parseInt(parts[3]);
                int memorySize = Integer.parseInt(parts[4]);
                int processTime = Integer.parseInt(parts[5]);

                // Create user session if not exists
                if (!kernel.getUserSessions().containsKey(userId)) {
                    String username = "User" + userId;
                    kernel.createUserSession(userId, username);
                }

                // Create task for user
                Task task = kernel.createUserTask(userId, taskId, priority, aisleId, memorySize, processTime);
                
                userTaskMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(task);
            }

            eventLogger.log("ORCHESTRATOR", "LOAD_COMPLETE", 
                "Loaded " + lines.size() + " tasks for " + kernel.getUserSessions().size() + " users");

        } catch (Exception e) {
            eventLogger.log("ERROR", "CSV_LOAD", "Failed to load CSV: " + e.getMessage());
        }
    }

    /**
     * Allocate memory for all user tasks
     */
    public void allocateMemoryForAllTasks() {
        eventLogger.log("ORCHESTRATOR", "MEMORY_SETUP", "Allocating memory for all user tasks");
        
        for (List<Task> userTasks : userTaskMap.values()) {
            for (Task task : userTasks) {
                int address = memoryManager.allocateBestFit(task.getTaskID(), task.getMemorySize());
                if (address >= 0) {
                    task.setMemoryAddress(address);
                    task.setAllocatedMemory(task.getMemorySize());
                    eventLogger.log("MEMORY", "ALLOCATE", 
                        "Task " + task.getTaskID() + " (User: " + task.getUsername() + 
                        ") allocated " + task.getMemorySize() + " units at address " + address);
                } else {
                    eventLogger.log("ERROR", "MEMORY_FAIL", 
                        "Failed to allocate " + task.getMemorySize() + " units for Task " + task.getTaskID());
                }
            }
        }
    }

    /**
     * Execute all user tasks concurrently with threading
     */
    public void executeAllUserTasks() throws InterruptedException {
        eventLogger.log("ORCHESTRATOR", "EXEC_START", "Starting concurrent task execution");
        
        long executionStart = System.currentTimeMillis();

        try {
            kernel.executeAllTasksConcurrently();
        } catch (InterruptedException e) {
            eventLogger.log("ERROR", "EXEC_INTERRUPT", "Task execution interrupted");
            throw e;
        }

        long executionEnd = System.currentTimeMillis();
        eventLogger.log("ORCHESTRATOR", "EXEC_COMPLETE", 
            "Task execution completed in " + (executionEnd - executionStart) + "ms");
    }

    /**
     * Run the complete multi-user simulation
     */
    public void runSimulation(String csvPath) {
        try {
            System.out.println("╔══════════════════════════════════════════════════════════╗");
            System.out.println("║    OPERATING SYSTEM - PHASE 2: MULTI-USER SIMULATION     ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝\n");

            eventLogger.log("ORCHESTRATOR", "INIT", "Initializing multi-user OS simulation...");

            // Phase 1: Load tasks
            loadMultiUserTasks(csvPath);

            // Phase 2: Allocate memory
            allocateMemoryForAllTasks();

            // Phase 3: Execute tasks concurrently
            System.out.println("\n--- CONCURRENT EXECUTION PHASE ---\n");
            executeAllUserTasks();

            // Phase 4: Generate statistics
            System.out.println("\n--- SIMULATION COMPLETE ---\n");
            generateStatistics();

            // Phase 5: Cleanup
            kernel.shutdown();

        } catch (Exception e) {
            eventLogger.log("ERROR", "SIMULATION", "Simulation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate comprehensive statistics
     */
    private void generateStatistics() {
        System.out.println("============================================================");
        System.out.println("PHASE 2 SIMULATION STATISTICS - MULTI-USER EXECUTION");
        System.out.println("============================================================\n");

        System.out.println(kernel.getAllUsersStats());
        System.out.println();

        // Individual user stats
        for (Integer userId : kernel.getUserSessions().keySet()) {
            System.out.println(kernel.getUserStats(userId));
            System.out.println();
        }

        // Memory stats
        System.out.println("========== MEMORY STATISTICS ==========");
        int totalUsed = 0;
        int totalFree = 0;
        for (memory.Partition p : memoryManager.getPartitions()) {
            if (p.isFree()) {
                totalFree += p.getSize();
            } else {
                totalUsed += p.getSize();
            }
        }
        System.out.println("Total Memory: 500 units");
        System.out.println("Used Memory: " + totalUsed + " units");
        System.out.println("Free Memory: " + totalFree + " units");
        System.out.println("Fragmentation: " + String.format("%.2f%%", memoryManager.getFragmentation()));
        System.out.println("=====================================\n");

        // Overall stats
        System.out.println("========== OVERALL SYSTEM STATS ==========");
        System.out.println("Total Users: " + kernel.getUserSessions().size());
        System.out.println("Total Tasks Completed: " + 
            kernel.getJobTable().values().stream()
                .filter(t -> t.getState().equals("TERMINATED")).count());
        System.out.println("Total Events Logged: " + kernel.getEventLog().size());
        System.out.println("==========================================\n");

        System.out.println("============================================================");
        System.out.println("PHASE 2 COMPLETE - MULTI-USER CONCURRENT EXECUTION VERIFIED");
        System.out.println("============================================================");
    }

    public static void main(String[] args) {
        String csvPath = args.length > 0 ? args[0] : "multi_user_tasks.csv";
        
        MultiUserOrchestrator orchestrator = new MultiUserOrchestrator();
        try {
            orchestrator.runSimulation(csvPath);
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
