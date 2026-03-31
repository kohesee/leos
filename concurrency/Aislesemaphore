// AisleSemaphore.java
// Concept: Semaphore for Mutual Exclusion
// Each warehouse aisle is represented as a binary semaphore (only 1 robot at a time)
package concurrency;

import java.util.concurrent.Semaphore;

public class AisleSemaphore {

    private final Semaphore semaphore;
    private final int aisleID;
    private boolean locked;
    private long clock = 0;

    public AisleSemaphore(int aisleID) {
        this.aisleID   = aisleID;
        this.semaphore = new Semaphore(1); // binary semaphore — only 1 robot per aisle
        this.locked    = false;
    }

    public void setClock(long clock) {
        this.clock = clock;
    }

    private void log(String message) {
        System.out.println("[" + clock + "ms] CONCURRENCY: " + message);
        clock++;
    }

    /** Locks aisle — task enters RUNNING state in this aisle */
    public void acquire(int taskID) {
        try {
            log("Task " + taskID + " requesting Aisle " + aisleID);
            semaphore.acquire();
            locked = true;
            log("Aisle " + aisleID + " Semaphore ACQUIRED by Task " + taskID);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Task " + taskID + " interrupted while waiting for Aisle " + aisleID);
        }
    }

    /** Unlocks aisle — task leaves */
    public void release(int taskID) {
        semaphore.release();
        locked = false;
        log("Aisle " + aisleID + " Semaphore RELEASED by Task " + taskID);
    }

    /** Returns true if aisle is currently locked */
    public boolean isLocked() {
        return locked;
    }

    public int getAisleID() {
        return aisleID;
    }
}
