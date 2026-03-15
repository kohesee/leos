// MemoryManager.java
// First Fit allocation for a fixed-size warehouse floor

public class MemoryManager {
    private static final int EMPTY = -1;
    private final int[] floor;

    public MemoryManager(int floorSize) {
        if (floorSize <= 0) {
            throw new IllegalArgumentException("floorSize must be positive");
        }
        this.floor = new int[floorSize];
        for (int i = 0; i < floor.length; i++) {
            floor[i] = EMPTY;
        }
    }

    public boolean allocate(Task task) {
        return allocate(task.getTaskID(), task.getMemorySize());
    }

    public boolean allocate(int taskID, int size) {
        if (size <= 0 || size > floor.length) {
            return false;
        }

        int startIndex = findFirstFit(size);
        if (startIndex == -1) {
            System.out.println("Memory allocation failed for Task " + taskID + " (size " + size + ")");
            return false;
        }

        int endIndex = startIndex + size - 1;
        for (int i = startIndex; i <= endIndex; i++) {
            floor[i] = taskID;
        }

        System.out.println(
                "Task " + taskID + " allocated " + size + " units at Floor Index " + startIndex + "-" + endIndex
        );
        return true;
    }

    public void free(int taskID) {
        for (int i = 0; i < floor.length; i++) {
            if (floor[i] == taskID) {
                floor[i] = EMPTY;
            }
        }
    }

    private int findFirstFit(int size) {
        int freeCount = 0;
        int startIndex = 0;

        for (int i = 0; i < floor.length; i++) {
            if (floor[i] == EMPTY) {
                if (freeCount == 0) {
                    startIndex = i;
                }
                freeCount++;
                if (freeCount == size) {
                    return startIndex;
                }
            } else {
                freeCount = 0;
            }
        }

        return -1;
    }
}
