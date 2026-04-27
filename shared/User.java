package shared;

import java.util.*;

/**
 * User Class - Represents a system user with their own task queue
 * Phase 2: Multi-user support for concurrent user sessions
 */
public class User {
    private int userID;
    private String username;
    private long loginTime;
    private long logoutTime;
    private List<Task> userTasks;
    private List<Task> completedTasks;
    private String sessionStatus; // ACTIVE, IDLE, LOGGED_OUT
    private int totalTasksSubmitted;
    private int totalTasksCompleted;
    private long totalWaitTime;
    private long totalTurnaroundTime;

    public User(int userID, String username) {
        this.userID = userID;
        this.username = username;
        this.loginTime = System.currentTimeMillis();
        this.logoutTime = -1;
        this.userTasks = new ArrayList<>();
        this.completedTasks = new ArrayList<>();
        this.sessionStatus = "ACTIVE";
        this.totalTasksSubmitted = 0;
        this.totalTasksCompleted = 0;
        this.totalWaitTime = 0;
        this.totalTurnaroundTime = 0;
    }

    // Getters and Setters
    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public long getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(long logoutTime) {
        this.logoutTime = logoutTime;
        this.sessionStatus = "LOGGED_OUT";
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String status) {
        this.sessionStatus = status;
    }

    public List<Task> getUserTasks() {
        return Collections.unmodifiableList(userTasks);
    }

    public List<Task> getCompletedTasks() {
        return Collections.unmodifiableList(completedTasks);
    }

    public void submitTask(Task task) {
        userTasks.add(task);
        totalTasksSubmitted++;
    }

    public void completeTask(Task task) {
        userTasks.remove(task);
        completedTasks.add(task);
        totalTasksCompleted++;
        totalWaitTime += task.getWaitTime();
        totalTurnaroundTime += task.getTurnaroundTime();
    }

    public int getTotalTasksSubmitted() {
        return totalTasksSubmitted;
    }

    public int getTotalTasksCompleted() {
        return totalTasksCompleted;
    }

    public int getPendingTasksCount() {
        return userTasks.size();
    }

    public double getAverageWaitTime() {
        return totalTasksCompleted > 0 ? (double) totalWaitTime / totalTasksCompleted : 0;
    }

    public double getAverageTurnaroundTime() {
        return totalTasksCompleted > 0 ? (double) totalTurnaroundTime / totalTasksCompleted : 0;
    }

    public long getSessionDuration() {
        if (logoutTime == -1) {
            return System.currentTimeMillis() - loginTime;
        }
        return logoutTime - loginTime;
    }

    public boolean isActive() {
        return sessionStatus.equals("ACTIVE");
    }

    @Override
    public String toString() {
        return String.format("User[ID=%d, Name=%s, Status=%s, Tasks: %d/%d, AvgWait: %.2fms]",
                userID, username, sessionStatus, totalTasksCompleted, totalTasksSubmitted,
                getAverageWaitTime());
    }
}
