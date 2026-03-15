// SafetyGuard.java
// Semaphores and Banker’s Algorithm

package concurrency;

import java.util.concurrent.Semaphore;

public class SafetyGuard {

    // Example: warehouse has 5 aisles
    private Semaphore[] aisles;

    // Track how many robots are inside
    private int availableAisles;

    public SafetyGuard(int numberOfAisles) {

        aisles = new Semaphore[numberOfAisles];

        for (int i = 0; i < numberOfAisles; i++) {
            aisles[i] = new Semaphore(1);   // only 1 robot allowed
        }

        availableAisles = numberOfAisles;
    }

    // Simple Banker style safety check
    public boolean isSafe() {

        if (availableAisles > 0) {
            return true;
        }

        return false;
    }

    // Robot entering aisle
    public void enterAisle(int taskID, int aisle) {

        try {

            System.out.println("Task " + taskID + " requesting Aisle " + aisle);

            if (!isSafe()) {
                System.out.println("System NOT SAFE. Task waiting.");
                return;
            }

            aisles[aisle].acquire();

            availableAisles--;

            System.out.println("Aisle " + aisle + " LOCKED by Task " + taskID);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Robot leaving aisle
    public void leaveAisle(int taskID, int aisle) {

        aisles[aisle].release();

        availableAisles++;

        System.out.println("Task " + taskID + " leaving Aisle " + aisle);

        System.out.println("Aisle " + aisle + " RELEASED");
    }
}
