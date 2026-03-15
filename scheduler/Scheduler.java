// Scheduler.java
// Ready Queue and Round Robin

import java.util.LinkedList;
import java.util.List;

public class Scheduler {
    private final LinkedList<Task> readyQueue;

    public Scheduler() {
        this.readyQueue = new LinkedList<>();
    }

    public void addToReadyQueue(Task task) {
        if (task == null) {
            return;
        }

        task.setState("READY");
        readyQueue.offer(task);
    }

    public void addAllToReadyQueue(List<Task> tasks) {
        if (tasks == null) {
            return;
        }

        for (Task task : tasks) {
            addToReadyQueue(task);
        }
    }

    public boolean hasReadyTasks() {
        return !readyQueue.isEmpty();
    }

    public int getReadyQueueSize() {
        return readyQueue.size();
    }

    public void runRoundRobin(int cycles) {
        if (readyQueue.isEmpty()) {
            System.out.println("Scheduler: ReadyQueue is empty.");
            return;
        }

        int executedCycles = 0;
        while (!readyQueue.isEmpty() && executedCycles < cycles) {
            Task currentTask = readyQueue.poll();
            currentTask.setState("RUNNING");

            Task nextTask = readyQueue.peek();
            if (nextTask != null) {
                System.out.println("Robot processing Task " + currentTask.getTaskID()
                        + "... now Task " + nextTask.getTaskID());
            } else {
                System.out.println("Robot processing Task " + currentTask.getTaskID()
                        + "... no next task yet");
            }

            currentTask.setState("READY");
            readyQueue.offer(currentTask);
            executedCycles++;
        }
    }
}
