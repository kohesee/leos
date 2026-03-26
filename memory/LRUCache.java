package memory;

import java.util.List;

public class LRUCache {
    private final MemoryManager memoryManager;
    private final double utilizationThreshold = 0.8;

    public LRUCache(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    public void markAccess(int taskID) {
        List<Partition> partitions = memoryManager.getPartitions();
        for (Partition p : partitions) {
            if (p.getTaskID() == taskID) {
                p.setLastAccessed(System.currentTimeMillis());
                return;
            }
        }
    }

    public boolean isFull() {
        int allocatedSpace = 0;
        List<Partition> partitions = memoryManager.getPartitions();
        
        for (Partition p : partitions) {
            if (!p.isFree()) {
                allocatedSpace += p.getSize();
            }
        }

        double utilization = (double) allocatedSpace / memoryManager.getFloorSize();
        return utilization > utilizationThreshold;
    }

    public void evictLRU() {
        if (!isFull()) return;

        List<Partition> partitions = memoryManager.getPartitions();
        Partition oldest = null;

        for (Partition p : partitions) {
            if (!p.isFree()) {
                if (oldest == null || p.getLastAccessed() < oldest.getLastAccessed()) {
                    oldest = p;
                }
            }
        }

        if (oldest != null) {
            System.out.println("LRU Cache Threshold (>80%) Reached. Evicting Task " + oldest.getTaskID() + " to 'Cold Storage'.");
            memoryManager.deallocate(oldest.getTaskID());
        }
    }
}
