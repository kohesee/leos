package memory;

import shared.Task;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MemoryManager {
    private static final int EMPTY = -1;
    private final int floorSize;
    private final List<Partition> partitions;

    public MemoryManager(int floorSize) {
        if (floorSize <= 0) {
            throw new IllegalArgumentException("floorSize must be positive");
        }
        this.floorSize = floorSize;
        this.partitions = new ArrayList<>();
        // Initialize with a single free block mapping the entire floor
        this.partitions.add(new Partition(0, floorSize, EMPTY));
    }

    public boolean allocate(Task task) {
        return allocate(task.getTaskID(), task.getMemorySize());
    }

    public boolean allocate(int taskID, int size) {
        return firstFit(size, taskID) != -1;
    }

    public int allocateBestFit(int taskID, int size) {
        return bestFit(size, taskID);
    }

    public void free(int taskID) {
        deallocate(taskID);
    }

    public int firstFit(int memorySize, int taskID) {
        if (memorySize <= 0 || memorySize > floorSize) {
            return -1;
        }

        for (int i = 0; i < partitions.size(); i++) {
            Partition p = partitions.get(i);
            if (p.isFree() && p.getSize() >= memorySize) {
                int startAddr = p.getStartAddr();
                int remainingSize = p.getSize() - memorySize;

                // Allocate memory
                p.setTaskID(taskID);
                p.setSize(memorySize);
                p.setLastAccessed(System.currentTimeMillis());

                System.out.println("Task " + taskID + " allocated " + memorySize + 
                    " units at Floor Index " + startAddr + "-" + (startAddr + memorySize - 1) + " (First Fit)");

                // If exact size, no need to split block
                if (remainingSize > 0) {
                    partitions.add(i + 1, new Partition(startAddr + memorySize, remainingSize, EMPTY));
                }

                return startAddr;
            }
        }
        System.out.println("Memory allocation failed for Task " + taskID + " (size " + memorySize + ")");
        return -1;
    }

    public int bestFit(int memorySize, int taskID) {
        if (memorySize <= 0 || memorySize > floorSize) {
            return -1;
        }

        int bestIndex = -1;
        int minDiff = Integer.MAX_VALUE;

        for (int i = 0; i < partitions.size(); i++) {
            Partition p = partitions.get(i);
            if (p.isFree() && p.getSize() >= memorySize) {
                int diff = p.getSize() - memorySize;
                if (diff < minDiff) {
                    minDiff = diff;
                    bestIndex = i;
                }
            }
        }

        if (bestIndex != -1) {
            Partition p = partitions.get(bestIndex);
            int startAddr = p.getStartAddr();
            int remainingSize = p.getSize() - memorySize;

            p.setTaskID(taskID);
            p.setSize(memorySize);
            p.setLastAccessed(System.currentTimeMillis());

            System.out.println("Task " + taskID + " allocated " + memorySize + 
                " units at Floor Index " + startAddr + "-" + (startAddr + memorySize - 1) + " (Best Fit)");

            if (remainingSize > 0) {
                partitions.add(bestIndex + 1, new Partition(startAddr + memorySize, remainingSize, EMPTY));
            }

            return startAddr;
        }

        System.out.println("Memory allocation failed for Task " + taskID + " (size " + memorySize + ")");
        return -1;
    }

    public void deallocate(int taskID) {
        boolean merged = false;
        // Mark as free
        for (Partition p : partitions) {
            if (p.getTaskID() == taskID) {
                p.setTaskID(EMPTY);
                System.out.println("Memory for Task " + taskID + " deallocated (" + p.getSize() + " units freed)");
                merged = true;
            }
        }

        // Merge adjacent free blocks
        if (merged) {
            for (int i = 0; i < partitions.size() - 1; i++) {
                Partition current = partitions.get(i);
                Partition next = partitions.get(i + 1);
                
                if (current.isFree() && next.isFree()) {
                    current.setSize(current.getSize() + next.getSize());
                    partitions.remove(i + 1);
                    i--; // Check this merged block with the next one
                }
            }
        }
    }

    public void defragment() {
        int nextStartAddr = 0;
        int totalFreeSize = 0;
        Iterator<Partition> iterator = partitions.iterator();

        while(iterator.hasNext()) {
            Partition p = iterator.next();
            if (p.isFree()) {
                totalFreeSize += p.getSize();
                iterator.remove();
            } else {
                p.setStartAddr(nextStartAddr);
                nextStartAddr += p.getSize();
            }
        }

        if (totalFreeSize > 0) {
            partitions.add(new Partition(nextStartAddr, totalFreeSize, EMPTY));
        }
        System.out.println("Memory defragmentation completed. Total free block size: " + totalFreeSize);
    }

    public double getFragmentation() {
        int totalWasted = 0;
        int freeBlocksCount = 0;
        
        for (Partition p : partitions) {
            if (p.isFree()) {
                totalWasted += p.getSize();
                freeBlocksCount++;
            }
        }
        
        System.out.println("Fragmentation: " + freeBlocksCount + " free blocks. Total free space: " + totalWasted + " / " + floorSize);
        return ((double) totalWasted / floorSize) * 100.0;
    }
    
    public List<Partition> getPartitions() {
        return partitions;
    }
    
    public int getFloorSize() {
        return floorSize;
    }
}
