package scheduler;

import shared.Task;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ReadyQueue {
    // Data structure: PriorityQueue
    private PriorityQueue<Task> queue;

    public ReadyQueue() {
        // Lower number = higher priority (Priority 1 is highest, 5 is lowest).
        // For equal priority, we sort by last access time so we can implement Round Robin.
        this.queue = new PriorityQueue<>(
            Comparator.comparingInt(Task::getPriority)
                      .thenComparingLong(Task::getLastAccessTime)
        );
    }

    // Add task to queue
    public void enqueue(Task t) {
        if (t != null) {
            queue.add(t);
        }
    }

    // Remove and return next task
    public Task dequeue() {
        return queue.poll();
    }

    // See next without removing
    public Task peek() {
        return queue.peek();
    }

    // Check if queue empty
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // Number of waiting tasks
    public int size() {
        return queue.size();
    }
}
