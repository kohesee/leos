package kernel;

public class Kernel {
    private java.util.List<shared.Task> jobTable = new java.util.ArrayList<>();
    private java.util.Queue<shared.Task> readyQueue = new java.util.LinkedList<>();
    private java.util.Queue<shared.Task> waitingQueue = new java.util.LinkedList<>();
    private java.util.List<String> eventLog = new java.util.ArrayList<>();
    private final long startTime = System.currentTimeMillis();

    public shared.Task createTask(int taskID, int priority, int targetAisle, int memorySize, int processTime) {
        shared.Task t = new shared.Task(taskID, priority, targetAisle, memorySize, processTime);
        jobTable.add(t);
        log("Task " + t.getTaskID() + " created (" + t.getState() + ")");

        t.setState("READY");
        log("Task " + t.getTaskID() + " validated (" + t.getState() + ")");
        return t;
    }

    public void terminateTask(shared.Task t) {
        if (t == null) return;
        t.setState("TERMINATED");
        t.setEndTime(System.currentTimeMillis());
        jobTable.remove(t);
        log("Task " + t.getTaskID() + " terminated (" + t.getState() + ")");
        logStats(t);
    }

    public java.util.List<shared.Task> getJobTable() {
        return java.util.Collections.unmodifiableList(jobTable);
    }

    public java.util.Queue<shared.Task> getReadyQueue() {
        return readyQueue;
    }

    public java.util.Queue<shared.Task> getWaitingQueue() {
        return waitingQueue;
    }

    public shared.Task getTaskById(int taskID) {
        for (shared.Task t : jobTable) {
            if (t.getTaskID() == taskID) return t;
        }
        return null;
    }

    public java.util.List<String> getEventLog() {
        return java.util.Collections.unmodifiableList(eventLog);
    }

    private void log(String message) {
        long now = System.currentTimeMillis();
        String logEntry = "[" + (now - startTime) + "ms] KERNEL: " + message;
        System.out.println(logEntry);
        eventLog.add(logEntry);
    }

    private void logStats(shared.Task t) {
        String stats = String.format("Task %d Stats -> Wait: %dms, Turnaround: %dms",
                t.getTaskID(),
                t.getStartTime() == 0 ? 0 : (t.getStartTime() - t.getCreationTime()),
                t.getEndTime() == 0 ? 0 : (t.getEndTime() - t.getCreationTime()));
        System.out.println(stats);
    }

    public java.util.List<shared.Task> loadTasksFromCsv(String path) throws java.io.IOException {
        java.util.List<shared.Task> created = new java.util.ArrayList<>();
        java.nio.file.Path p = java.nio.file.Paths.get(path);
        java.util.List<String> lines = java.nio.file.Files.readAllLines(p);
        boolean first = true;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            if (first && line.toLowerCase().contains("taskid")) { first = false; continue; }
            String[] parts = line.split(",");
            if (parts.length < 5) continue;
            int taskID = Integer.parseInt(parts[0].trim());
            int priority = Integer.parseInt(parts[1].trim());
            int targetAisle = Integer.parseInt(parts[2].trim());
            int memorySize = Integer.parseInt(parts[3].trim());
            int processTime = Integer.parseInt(parts[4].trim());
            created.add(createTask(taskID, priority, targetAisle, memorySize, processTime));
        }
        return created;
    }
    
    
    public void validateTask(shared.Task t) {
        if (t == null || !t.getState().equals("NEW")) return;
        t.setState("READY");
        readyQueue.add(t);
        log("Task " + t.getTaskID() + " validated and moved to READY queue");
    }
    
    public void runTask(shared.Task t) {
        if (t == null || !t.getState().equals("READY")) return;
        if (t.getStartTime() == 0) {
            t.setStartTime(System.currentTimeMillis());
        }
        t.setState("RUNNING");
        t.updateLastAccessTime();
        log("Task " + t.getTaskID() + " transitioned to RUNNING");
    }
    
    public void waitTask(shared.Task t, String reason) {
        if (t == null || !t.getState().equals("RUNNING")) return;
        t.setState("WAITING");
        t.setWaitingReason(reason);
        waitingQueue.add(t);
        log("Task " + t.getTaskID() + " moved to WAITING (" + reason + ")");
    }
    
    public void resumeTask(shared.Task t) {
        if (t == null || !t.getState().equals("WAITING")) return;
        t.setState("READY");
        t.setWaitingReason("");
        waitingQueue.remove(t);
        readyQueue.add(t);
        log("Task " + t.getTaskID() + " resumed from WAITING to READY");
    }
    
    public void printSystemStats() {
        System.out.println("\n========== SYSTEM STATISTICS ==========");
        System.out.println("Total tasks created: " + jobTable.size());
        System.out.println("Ready queue size: " + readyQueue.size());
        System.out.println("Waiting queue size: " + waitingQueue.size());
        System.out.println("Total events logged: " + eventLog.size());
        System.out.println("=======================================\n");
    }
    
    public void printEventLog() {
        System.out.println("\n========== EVENT LOG ==========");
        for (String event : eventLog) {
            System.out.println(event);
        }
        System.out.println("===============================\n");
    }

    public static void main(String[] args) throws Exception {
        Kernel k = new Kernel();

        String csv = "tasks.csv";
        java.util.List<shared.Task> tasks = k.loadTasksFromCsv(csv);

        for (shared.Task t : new java.util.ArrayList<>(k.getJobTable())) {
            t.setStartTime(System.currentTimeMillis());
            k.log("Task " + t.getTaskID() + " starting execution (" + t.getState() + ")");
            Thread.sleep(t.getProcessTime());
            k.terminateTask(t);
        }
    }
}
