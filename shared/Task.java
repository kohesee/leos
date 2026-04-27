package shared;

public class Task {
    private int taskID;
    private int userID;
    private String username;
    private int priority;
    private String state;
    private int targetAisle;
    private int memorySize;
    private int processTime;
    private long creationTime;
    private long startTime;
    private long endTime;
    private int allocatedMemory;
    private int memoryAddress;
    private long lastAccessTime;
    private String waitingReason;
    private int aisleAccessCount;
    
    private static final String[] VALID_STATES = {"NEW", "READY", "RUNNING", "WAITING", "TERMINATED"};

    public Task(int taskID, int priority, int targetAisle, int memorySize, int processTime) {
        this(taskID, -1, "SYSTEM", priority, targetAisle, memorySize, processTime);
    }

    public Task(int taskID, int userID, String username, int priority, int targetAisle, int memorySize, int processTime) {
        this.taskID = taskID;
        this.userID = userID;
        this.username = username;
        this.priority = priority;
        this.targetAisle = targetAisle;
        this.memorySize = memorySize;
        this.processTime = processTime;
        this.state = "NEW";
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = this.creationTime;
        this.allocatedMemory = 0;
        this.memoryAddress = -1;
        this.waitingReason = "";
        this.aisleAccessCount = 0;
    }

    public int getTaskID() {
        return taskID;
    }

    public int getTaskId() {
        return taskID;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public int getPriority() {
        return priority;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (isValidState(state)) {
            this.state = state;
        } else {
            throw new IllegalArgumentException("Invalid state: " + state);
        }
    }
    
    private static boolean isValidState(String state) {
        for (String valid : VALID_STATES) {
            if (valid.equals(state)) return true;
        }
        return false;
    }

    public int getTargetAisle() {
        return targetAisle;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public int getProcessTime() {
        return processTime;
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
    
    public int getAllocatedMemory() {
        return allocatedMemory;
    }
    
    public void setAllocatedMemory(int allocatedMemory) {
        this.allocatedMemory = allocatedMemory;
    }
    
    public int getMemoryAddress() {
        return memoryAddress;
    }
    
    public void setMemoryAddress(int memoryAddress) {
        this.memoryAddress = memoryAddress;
    }
    
    public long getLastAccessTime() {
        return lastAccessTime;
    }
    
    public void updateLastAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    public String getWaitingReason() {
        return waitingReason;
    }
    
    public void setWaitingReason(String reason) {
        this.waitingReason = reason;
    }
    
    public int getAisleAccessCount() {
        return aisleAccessCount;
    }
    
    public void incrementAisleAccessCount() {
        this.aisleAccessCount++;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskID=" + taskID +
                ", priority=" + priority +
                ", state='" + state + '\'' +
                ", targetAisle=" + targetAisle +
                ", memorySize=" + memorySize +
                ", allocated=" + allocatedMemory +
                ", addr=" + memoryAddress +
                '}';
    }
}
