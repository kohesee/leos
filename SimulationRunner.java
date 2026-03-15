// SimulationRunner.java
// THE INTEGRATION: Main file to run the 1-week demo

import io.PathOptimizer;
import concurrency.SafetyGuard;   // Person C

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimulationRunner {

    public static void main(String[] args) {

        System.out.println("Starting OS simulation...");
        System.out.println();

        /* =========================
           Person C - Concurrency
           ========================= */

        System.out.println("[Person C] Concurrency Demo");

        SafetyGuard guard = new SafetyGuard(5);

        guard.enterAisle(101,1);
        guard.enterAisle(102,1);

        guard.leaveAisle(101,1);
        guard.leaveAisle(102,1);

        System.out.println();

        /* =========================
           Person E - Path Optimizer
           (UNCHANGED)
           ========================= */

        PathOptimizer optimizer = new PathOptimizer();

        List<Integer> weeklyDemoRequests = Arrays.asList(50, 12);
        int weeklyDemoStart = 10;
        List<Integer> sstfDemoOrder = optimizer.sstfOrder(weeklyDemoStart, weeklyDemoRequests);

        System.out.println("[Person E] SSTF Weekly Demo");
        System.out.println("Robot moving: " + optimizer.formatMovementTrace(weeklyDemoStart, sstfDemoOrder));
        System.out.println("Total seek distance: " + optimizer.totalSeekDistance(weeklyDemoStart, sstfDemoOrder));
        System.out.println();

        List<Integer> csvRequests = loadShelfRequestsFromCsv("tasks.csv");
        int startShelf = 0;

        List<Integer> sstfOrder = optimizer.sstfOrder(startShelf, csvRequests);
        System.out.println("[Person E] SSTF (tasks.csv)");
        System.out.println("Requests: " + csvRequests);
        System.out.println("Robot moving: " + optimizer.formatMovementTrace(startShelf, sstfOrder));
        System.out.println("Total seek distance: " + optimizer.totalSeekDistance(startShelf, sstfOrder));
        System.out.println();

        int scanStartShelf = 4;
        List<Integer> scanOrder = optimizer.scanOrder(scanStartShelf, csvRequests, 100, true);
        System.out.println("[Person E] SCAN (tasks.csv, move up first)");
        System.out.println("Robot moving: " + optimizer.formatMovementTrace(scanStartShelf, scanOrder));
        System.out.println("Total seek distance: " + optimizer.totalSeekDistance(scanStartShelf, scanOrder));
    }

    private static List<Integer> loadShelfRequestsFromCsv(String filePath) {

        List<Integer> requests = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",");

                if (parts.length < 3) {
                    continue;
                }

                try {

                    int targetAisle = Integer.parseInt(parts[2].trim());
                    requests.add(targetAisle);

                } catch (NumberFormatException ignored) {}

            }

        } catch (IOException e) {

            System.out.println("Could not read tasks.csv: " + e.getMessage());

        }

        return requests;
    }
}
