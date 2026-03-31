package io;

import shared.Task;

import java.util.List;

public class RobotSimulator {
    private int currentPos;
    private long clock;

    public RobotSimulator(int startPos) {
        this.currentPos = startPos;
        this.clock = 0;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setClock(long clock) {
        this.clock = clock;
    }

    public int moveRobot(int currentPos, int targetPos) {
        int distance = Math.abs(targetPos - currentPos);
        log("DISK", "Robot moving from shelf " + currentPos + " to " + targetPos + " (distance: " + distance + ")");
        this.currentPos = targetPos;
        return this.currentPos;
    }

    public void pickItem(int shelf) {
        log("DISK", "Robot picked item at shelf " + shelf + " (pick cost: 1ms)");
    }

    public void executePath(Task task, List<Integer> shelves) {
        if (task == null || shelves == null || shelves.isEmpty()) {
            return;
        }

        log("DISK", "Task " + task.getTaskID() + " executing path: " + shelves);
        int head = this.currentPos;
        for (int shelf : shelves) {
            head = moveRobot(head, shelf);
            pickItem(shelf);
        }
        this.currentPos = head;
    }

    private void log(String module, String message) {
        System.out.println("[" + clock + "ms] " + module + ": " + message);
        clock++;
    }
}
