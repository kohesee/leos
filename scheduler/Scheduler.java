package scheduler;

import shared.Task;

public class Scheduler {
    private long totalWaitTime = 0;
    private long totalTurnaroundTime = 0;
    private int completedTasks = 0;
    
    // Track scheduler initialization to match output timestamps
    private final long schedulerStartTimeMs;

    public Scheduler() {
        this.schedulerStartTimeMs = System.currentTimeMillis();
    }

    // Algorithm 1: Round Robin (RR) for standard priority tasks
    // Selects a task from the current ReadyQueue 
    public Task scheduleRoundRobin(ReadyQueue rq) {
        if (rq.isEmpty()) {
            return null;
        }

        Task nextTask = rq.dequeue();
        if (nextTask != null) {
            log("Task " + nextTask.getTaskID() + " selected (Round Robin)");
        }
        
        // Note: Orchestrator should run task up to 5ms time quantum.
        // If task not done after 5ms, the orchestrator should invoke preemptive logic,
        // update task last access time, and enqueue back to rq.
        return nextTask;
    }

    // Helper for RR preemption if orchestrated externally
    public void preemptTask(Task task, ReadyQueue rq) {
        log("Task " + task.getTaskID() + " preempted after 5ms. Moved to back of queue");
        task.updateLastAccessTime();
        rq.enqueue(task);
    }

    // Algorithm 2: Priority Scheduling for express deliveries
    // Priority 1 = Express (highest), Priority 5 = Standard (lowest)
    // Lower number = higher priority
    public Task schedulePriority(ReadyQueue rq) {
        if (rq.isEmpty()) {
            return null;
        }

        Task nextTask = rq.dequeue();
        if (nextTask != null) {
            log("Task " + nextTask.getTaskID() + " selected (Priority Scheduling)");
        }
        return nextTask;
    }

    // Context Switching Logic
    // Update task state and log contextual changes
    public void switchContext(Task currentTask, Task nextTask) {
        if (currentTask != null) {
            log("Context Switch - Saving state of Task " + currentTask.getTaskID());
            
            // Only convert non-terminated states back to ready.
            if (!currentTask.getState().equals("TERMINATED") && 
                !currentTask.getState().equals("WAITING")) {
                currentTask.setState("READY");
            }
        }

        if (nextTask != null) {
            log("Context Switch - Loading state of Task " + nextTask.getTaskID());
            
            // Update task state to RUNNING
            nextTask.setState("RUNNING");
            
            // Ensures StartTime is set exactly when CPU allocates to it for the first time
            if (nextTask.getStartTime() == 0) {
                nextTask.setStartTime(System.currentTimeMillis());
            }
        }
    }

    // Performance Metrics Calculator
    // Wait Time = Start Time - Creation Time 
    // Turnaround Time = End Time - Creation Time
    public void calculateMetrics(Task t) {
        long waitTime = t.getWaitTime();
        long turnaroundTime = t.getTurnaroundTime();
        
        totalWaitTime += waitTime;
        totalTurnaroundTime += turnaroundTime;
        completedTasks++;
        
        // Print for every completed task
        log("Task " + t.getTaskID() + " Stats -> Wait: " + waitTime + "ms, Turnaround: " + turnaroundTime + "ms");
    }

    // Print scheduler statistics on completion
    public void printSchedulerStats() {
        if (completedTasks == 0) {
            System.out.println("\n========== SCHEDULER STATS ==========");
            System.out.println("Wait Time / Turnaround unavailable. No completed tasks.");
            System.out.println("=====================================\n");
            return;
        }

        double avgWait = (double) totalWaitTime / completedTasks;
        double avgTurnaround = (double) totalTurnaroundTime / completedTasks;

        System.out.println("\n========== SCHEDULER STATS ==========");
        System.out.println("Completed Tasks Selected: " + completedTasks);
        System.out.println(String.format("Average Wait Time: %.2f ms", avgWait));
        System.out.println(String.format("Average Turnaround Time: %.2f ms", avgTurnaround));
        System.out.println("CPU Utilization: Calculated externally (based on active running times)");
        System.out.println("=====================================\n");
    }

    private void log(String message) {
        long timeOffsetMs = System.currentTimeMillis() - schedulerStartTimeMs;
        System.out.println(String.format("[%dms] SCHEDULER: %s", timeOffsetMs, message));
    }
}
