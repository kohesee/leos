package kernel;

public class Kernel {
    private java.util.List<shared.Task> jobTable = new java.util.ArrayList<>();
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

    private void log(String message) {
        long now = System.currentTimeMillis();
        System.out.println("[" + (now - startTime) + "ms] KERNEL: " + message);
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
