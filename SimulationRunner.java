import io.PathOptimizer;
import io.RobotSimulator;
import kernel.Kernel;
import memory.LRUCache;
import memory.MemoryManager;
import scheduler.ReadyQueue;
import scheduler.Scheduler;
import shared.Task;

import java.util.ArrayList;
import java.util.List;

public class SimulationRunner {
    private static final int ROBOT_SPEED = 5;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting OS simulation...");

        Kernel kernel = new Kernel();
        Scheduler scheduler = new Scheduler();
        ReadyQueue readyQueue = new ReadyQueue();
        MemoryManager memoryManager = new MemoryManager(500);
        LRUCache lruCache = new LRUCache(memoryManager);

        PathOptimizer pathOptimizer = new PathOptimizer();
        RobotSimulator robotSimulator = new RobotSimulator(0);

        List<Task> tasks = kernel.loadTasksFromCsv("tasks.csv");
        for (Task task : tasks) {
            int allocatedAddress = memoryManager.allocateBestFit(task.getTaskID(), task.getMemorySize());
            if (allocatedAddress == -1) {
                allocatedAddress = memoryManager.firstFit(task.getMemorySize(), task.getTaskID());
            }

            if (allocatedAddress != -1) {
                task.setAllocatedMemory(task.getMemorySize());
                task.setMemoryAddress(allocatedAddress);
            }

            readyQueue.enqueue(task);
            System.out.println("SCHEDULER: Task " + task.getTaskID() + " added to Ready Queue");
        }

        Task currentTask = null;
        while (!readyQueue.isEmpty()) {
            Task nextTask = selectTask(scheduler, readyQueue);
            if (nextTask == null) {
                break;
            }

            scheduler.switchContext(currentTask, nextTask);
            currentTask = nextTask;

            int robotStartPos = robotSimulator.getCurrentPos();
            List<Integer> shelfRequests = buildShelfRequests(nextTask);
            PathOptimizer.ComparisonResult comparison = pathOptimizer.compareAlgorithms(
                    shelfRequests,
                    robotStartPos,
                    ROBOT_SPEED
            );

            List<Integer> selectedPath = selectPath(comparison);
            String selectedAlgorithm = comparison.getBetterAlgorithm().equals("TIE") ? "SSTF" : comparison.getBetterAlgorithm();

            System.out.println("DISK: Task " + nextTask.getTaskID() + " visiting shelves: " + selectedPath +
                    " using " + selectedAlgorithm);
            robotSimulator.executePath(nextTask, selectedPath);

            pathOptimizer.recordTaskMetrics(nextTask.getTaskID(), robotStartPos, selectedPath, ROBOT_SPEED);
            lruCache.markAccess(nextTask.getTaskID());
            if (lruCache.isFull()) {
                lruCache.evictLRU();
            }

            Thread.sleep(nextTask.getProcessTime());

            memoryManager.deallocate(nextTask.getTaskID());
            kernel.terminateTask(nextTask);
            scheduler.calculateMetrics(nextTask);
        }

        scheduler.printSchedulerStats();
        System.out.println("DISK STATS: totalSeekDistance=" + pathOptimizer.getCumulativeSeekDistance() +
                ", totalSeekTime=" + pathOptimizer.getCumulativeSeekTime() + "ms" +
                ", directionChanges=" + pathOptimizer.getCumulativeDirectionChanges());
        System.out.println(String.format("MEMORY STATS: fragmentation=%.2f%%", memoryManager.getFragmentation()));
    }

    private static Task selectTask(Scheduler scheduler, ReadyQueue readyQueue) {
        Task candidate = readyQueue.peek();
        if (candidate == null) {
            return null;
        }

        if (candidate.getPriority() <= 2) {
            return scheduler.schedulePriority(readyQueue);
        }
        return scheduler.scheduleRoundRobin(readyQueue);
    }

    private static List<Integer> buildShelfRequests(Task task) {
        int base = Math.max(0, Math.min(100, task.getTargetAisle() * 10));
        List<Integer> shelves = new ArrayList<>();
        shelves.add(base);
        shelves.add(Math.min(100, base + 20));
        shelves.add(Math.max(0, base - 10));
        return shelves;
    }

    private static List<Integer> selectPath(PathOptimizer.ComparisonResult comparison) {
        if (comparison.getBetterAlgorithm().equals("SCAN")) {
            return comparison.getScanPath();
        }
        return comparison.getSstfPath();
    }
}
