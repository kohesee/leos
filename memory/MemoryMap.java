package memory;

import java.util.List;

public class MemoryMap {
    private final MemoryManager memoryManager;

    public MemoryMap(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    public void visualize() {
        List<Partition> partitions = memoryManager.getPartitions();
        StringBuilder sb = new StringBuilder();
        
        sb.append("Warehouse Floor Memory Map:\n");
        sb.append("========================================================\n");
        
        for (Partition p : partitions) {
            if (p.isFree()) {
                sb.append(String.format("[FREE: %d] ", p.getSize()));
            } else {
                sb.append(String.format("[TASK%d: %d] ", p.getTaskID(), p.getSize()));
            }
        }
        
        sb.append("\n========================================================");
        System.out.println(sb.toString());
    }

    // This could be run in a separate thread, but for our simulation
    // we can invoke it on command to show the system state
    public void startPeriodicVisualization(int intervalMs) {
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    visualize();
                    System.out.println(String.format("Fragmentation: %.2f%%", memoryManager.getFragmentation()));
                    Thread.sleep(intervalMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
