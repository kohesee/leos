package kernel;

import java.util.*;
import java.util.concurrent.*;

/**
 * Multi-threaded Kernel for Phase 2
 * Handles concurrent task execution per user with thread pools
 */
public class MultiThreadedKernel {
    private Map<Integer, shared.User> userSessions;
    private Map<Integer, shared.Task> jobTable;
    private ExecutorService globalExecutor;
    private Map<Integer, ExecutorService> userExecutors;
    private java.util.Queue<shared.Task> globalReadyQueue;
    private java.util.Queue<shared.Task> globalWaitingQueue;
    private List<String> eventLog;
    private final long startTime = System.currentTimeMillis();
    private final int THREAD_POOL_SIZE = 3;
    private boolean isRunning = true;

    public MultiThreadedKernel() {
        this.userSessions = new ConcurrentHashMap<>();
        this.jobTable = new ConcurrentHashMap<>();
        this.globalExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.userExecutors = new ConcurrentHashMap<>();
        this.globalReadyQueue = new ConcurrentLinkedQueue<>();
        this.globalWaitingQueue = new ConcurrentLinkedQueue<>();
        this.eventLog = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Create a new user session
     */
    public shared.User createUserSession(int userID, String username) {
        shared.User user = new shared.User(userID, username);
        userSessions.put(userID, user);
        userExecutors.put(userID, Executors.newFixedThreadPool(2));
        log("USER_LOGIN", "User " + username + " (ID: " + userID + ") logged in");
        return user;
    }

    /**
     * Create a task for a specific user (multi-user support)
     */
    public shared.Task createUserTask(int userID, int taskID, int priority, int targetAisle, 
                                      int memorySize, int processTime) {
        shared.User user = userSessions.get(userID);
        if (user == null) {
            log("ERROR", "User " + userID + " not found");
            return null;
        }

        String username = user.getUsername();
        shared.Task task = new shared.Task(taskID, userID, username, priority, targetAisle, memorySize, processTime);
        
        jobTable.put(taskID, task);
        user.submitTask(task);
        
        task.setState("READY");
        globalReadyQueue.add(task);
        
        log("TASK_CREATE", "Task " + taskID + " created for user " + username + " (Priority: " + priority + ")");
        return task;
    }

    /**
     * Terminate a task (thread-safe)
     */
    public void terminateTask(shared.Task task) {
        if (task == null) return;
        
        task.setState("TERMINATED");
        task.setEndTime(System.currentTimeMillis());
        
        shared.User user = userSessions.get(task.getUserID());
        if (user != null) {
            user.completeTask(task);
            log("TASK_TERMINATE", "Task " + task.getTaskID() + " completed for user " + task.getUsername() + 
                " (Wait: " + task.getWaitTime() + "ms, Turnaround: " + task.getTurnaroundTime() + "ms)");
        }
        
        jobTable.remove(task.getTaskID());
    }

    /**
     * Submit a task to the appropriate user's executor (threading)
     */
    public Future<?> submitTaskForExecution(shared.Task task) {
        ExecutorService userExecutor = userExecutors.get(task.getUserID());
        if (userExecutor == null) {
            log("ERROR", "No executor for user " + task.getUserID());
            return null;
        }

        return userExecutor.submit(() -> {
            log("THREAD_EXEC", "Task " + task.getTaskID() + " executing in thread " + 
                Thread.currentThread().getName() + " for user " + task.getUsername());
            
            task.setState("RUNNING");
            task.setStartTime(System.currentTimeMillis());
            
            try {
                Thread.sleep(task.getProcessTime());
            } catch (InterruptedException e) {
                log("THREAD_INTERRUPT", "Task " + task.getTaskID() + " interrupted");
                Thread.currentThread().interrupt();
            }
            
            terminateTask(task);
        });
    }

    /**
     * Submit a runnable task to the appropriate user's executor (for custom execution logic)
     */
    public Future<?> submitTaskForExecution(Runnable taskRunnable, int userID) {
        ExecutorService userExecutor = userExecutors.get(userID);
        if (userExecutor == null) {
            log("ERROR", "No executor for user " + userID);
            return null;
        }

        return userExecutor.submit(taskRunnable);
    }

    /**
     * Execute all user tasks concurrently
     */
    public void executeAllTasksConcurrently() throws InterruptedException {
        List<Future<?>> futures = new ArrayList<>();

        // Submit all tasks to their respective user executors
        while (!globalReadyQueue.isEmpty()) {
            shared.Task task = globalReadyQueue.poll();
            if (task != null) {
                Future<?> future = submitTaskForExecution(task);
                if (future != null) {
                    futures.add(future);
                }
            }
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                log("ERROR", "Task execution failed: " + e.getMessage());
            }
        }
    }

    /**
     * Logout user (close their thread pool and session)
     */
    public void logoutUser(int userID) {
        shared.User user = userSessions.get(userID);
        if (user == null) return;

        user.setLogoutTime(System.currentTimeMillis());
        log("USER_LOGOUT", "User " + user.getUsername() + " logged out. Session duration: " + 
            user.getSessionDuration() + "ms, Tasks completed: " + user.getTotalTasksCompleted());
        
        ExecutorService userExecutor = userExecutors.get(userID);
        if (userExecutor != null) {
            userExecutor.shutdown();
            try {
                userExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log("ERROR", "Executor shutdown interrupted for user " + userID);
            }
        }
    }

    /**
     * Get user statistics
     */
    public String getUserStats(int userID) {
        shared.User user = userSessions.get(userID);
        if (user == null) return "User not found";
        
        return String.format(
            "USER STATS [%s]:\n" +
            "  Tasks Submitted: %d\n" +
            "  Tasks Completed: %d\n" +
            "  Average Wait Time: %.2fms\n" +
            "  Average Turnaround: %.2fms\n" +
            "  Session Duration: %dms\n" +
            "  Status: %s",
            user.getUsername(),
            user.getTotalTasksSubmitted(),
            user.getTotalTasksCompleted(),
            user.getAverageWaitTime(),
            user.getAverageTurnaroundTime(),
            user.getSessionDuration(),
            user.getSessionStatus()
        );
    }

    /**
     * Get all active user statistics
     */
    public String getAllUsersStats() {
        StringBuilder sb = new StringBuilder("=== ALL USERS STATISTICS ===\n");
        for (shared.User user : userSessions.values()) {
            sb.append(user.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Shutdown all executors and close kernel
     */
    public void shutdown() {
        isRunning = false;
        globalExecutor.shutdown();
        for (ExecutorService executor : userExecutors.values()) {
            executor.shutdown();
        }
        log("KERNEL_SHUTDOWN", "Kernel shutdown initiated");
    }

    /**
     * Thread-safe logging
     */
    private void log(String eventType, String message) {
        long now = System.currentTimeMillis();
        String logEntry = String.format("[%dms] [%s] %s", now - startTime, eventType, message);
        System.out.println(logEntry);
        eventLog.add(logEntry);
    }

    // Getters
    public Map<Integer, shared.User> getUserSessions() {
        return Collections.unmodifiableMap(userSessions);
    }

    public Map<Integer, shared.Task> getJobTable() {
        return Collections.unmodifiableMap(jobTable);
    }

    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
