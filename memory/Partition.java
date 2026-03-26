package memory;

public class Partition {
    private int startAddr;
    private int size;
    private int taskID; // -1 indicates FREE
    private long lastAccessed;

    public Partition(int startAddr, int size, int taskID) {
        this.startAddr = startAddr;
        this.size = size;
        this.taskID = taskID;
        this.lastAccessed = System.currentTimeMillis();
    }

    public int getStartAddr() {
        return startAddr;
    }

    public void setStartAddr(int startAddr) {
        this.startAddr = startAddr;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public boolean isFree() {
        return this.taskID == -1;
    }
}
