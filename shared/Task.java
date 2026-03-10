public class Task {
    private int taskID;
    private int priority;
    private String state;
    private int targetAisle;
    private int memorySize;
    private long creationTime;
    private long startTime;
    private long endTime;

    public Task(int taskID, int priority, int targetAisle, int memorySize) {
        this.taskID = taskID;
        this.priority = priority;
        this.targetAisle = targetAisle;
        this.memorySize = memorySize;
        this.state = "NEW";
        this.creationTime = System.currentTimeMillis();
    }

    public int getTaskID() {
        return taskID;
    }

    public int getPriority() {
        return priority;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getTargetAisle() {
        return targetAisle;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getWaitTime() {
        return startTime - creationTime;
    }

    public long getTurnaroundTime() {
        return endTime - creationTime;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskID=" + taskID +
                ", priority=" + priority +
                ", state='" + state + '\'' +
                ", targetAisle=" + targetAisle +
                ", memorySize=" + memorySize +
                '}';
    }
}
