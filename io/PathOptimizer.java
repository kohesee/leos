package io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

// PathOptimizer.java
// SSTF and SCAN algorithms
// [cite: 49, 50]
public class PathOptimizer {
    private final DiskScheduler diskScheduler;
    private final AtomicLong cumulativeSeekDistance;
    private final AtomicLong cumulativeSeekTime;
    private final AtomicLong cumulativeDirectionChanges;

    public PathOptimizer() {
        this.diskScheduler = new DiskScheduler();
        this.cumulativeSeekDistance = new AtomicLong(0);
        this.cumulativeSeekTime = new AtomicLong(0);
        this.cumulativeDirectionChanges = new AtomicLong(0);
    }

    public int calculateDistance(List<Integer> shelves) {
        if (shelves == null || shelves.size() < 2) {
            return 0;
        }

        int distance = 0;
        for (int i = 1; i < shelves.size(); i++) {
            distance += Math.abs(shelves.get(i) - shelves.get(i - 1));
        }
        return distance;
    }

    public int getTotalTime(int distance, int speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("speed must be > 0");
        }
        return (int) Math.ceil((double) distance / speed);
    }

    public ComparisonResult compareAlgorithms(List<Integer> shelves, int currentPos, int speed) {
        List<Integer> sstfPath = scheduleSSTF(shelves, currentPos);
        List<Integer> scanPath = scheduleSCAN(shelves, currentPos);

        int sstfDistance = totalSeekDistance(currentPos, sstfPath);
        int scanDistance = totalSeekDistance(currentPos, scanPath);
        int sstfTime = getTotalTime(sstfDistance, speed);
        int scanTime = getTotalTime(scanDistance, speed);

        String betterAlgorithm;
        if (sstfDistance < scanDistance) {
            betterAlgorithm = "SSTF";
        } else if (scanDistance < sstfDistance) {
            betterAlgorithm = "SCAN";
        } else {
            betterAlgorithm = "TIE";
        }

        return new ComparisonResult(
                sstfPath,
                scanPath,
                sstfDistance,
                scanDistance,
                sstfTime,
                scanTime,
                betterAlgorithm
        );
    }

    public void compareAlgorithms() {
        List<Integer> sample = new ArrayList<>();
        sample.add(10);
        sample.add(30);
        sample.add(70);
        sample.add(40);

        ComparisonResult result = compareAlgorithms(sample, 20, 5);
        System.out.println("DISK: compareAlgorithms sample -> " + result);
    }

    public void recordTaskMetrics(int taskID, int startPos, List<Integer> path, int speed) {
        int distance = totalSeekDistance(startPos, path);
        int time = getTotalTime(distance, speed);
        int directionChanges = countDirectionChanges(startPos, path);

        cumulativeSeekDistance.addAndGet(distance);
        cumulativeSeekTime.addAndGet(time);
        cumulativeDirectionChanges.addAndGet(directionChanges);

        System.out.println(String.format(Locale.US,
                "DISK: Task %d -> distance=%d, time=%dms, directionChanges=%d",
                taskID, distance, time, directionChanges));
    }

    public long getCumulativeSeekDistance() {
        return cumulativeSeekDistance.get();
    }

    public long getCumulativeSeekTime() {
        return cumulativeSeekTime.get();
    }

    public long getCumulativeDirectionChanges() {
        return cumulativeDirectionChanges.get();
    }

    public List<Integer> scheduleSSTF(List<Integer> shelves, int currentPos) {
        return diskScheduler.scheduleSSTF(shelves, currentPos);
    }

    public List<Integer> scheduleSCAN(List<Integer> shelves, int currentPos) {
        return diskScheduler.scheduleSCAN(shelves, currentPos);
    }

    public List<Integer> sstfOrder(int startShelf, List<Integer> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> converted = normalizeToShelfTracks(requests);
        return scheduleSSTF(converted, normalizeTrack(startShelf));
    }

    public List<Integer> scanOrder(int startShelf, List<Integer> requests, int maxShelf, boolean moveUpFirst) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> converted = normalizeToShelfTracks(requests);
        int start = normalizeTrack(startShelf);

        List<Integer> scanPath = scheduleSCAN(converted, start);
        if (moveUpFirst) {
            return scanPath;
        }

        List<Integer> reverse = new ArrayList<>(scanPath);
        Collections.reverse(reverse);
        return reverse;
    }

    public int totalSeekDistance(int startShelf, List<Integer> order) {
        if (order == null || order.isEmpty()) {
            return 0;
        }

        int total = 0;
        int current = startShelf;

        for (int shelf : order) {
            total += Math.abs(shelf - current);
            current = shelf;
        }

        return total;
    }

    public int countDirectionChanges(int startShelf, List<Integer> order) {
        if (order == null || order.size() < 2) {
            return 0;
        }

        int changes = 0;
        int previousDirection = Integer.signum(order.get(0) - startShelf);

        for (int i = 1; i < order.size(); i++) {
            int currentDirection = Integer.signum(order.get(i) - order.get(i - 1));
            if (currentDirection != 0 && previousDirection != 0 && currentDirection != previousDirection) {
                changes++;
            }
            if (currentDirection != 0) {
                previousDirection = currentDirection;
            }
        }

        return changes;
    }

    public String formatMovementTrace(int startShelf, List<Integer> order) {
        StringBuilder builder = new StringBuilder();
        builder.append("Shelf ").append(startShelf);
        for (int shelf : order) {
            builder.append(" -> ").append(shelf);
        }
        return builder.toString();
    }

    private int normalizeTrack(int value) {
        int bounded = Math.max(DiskScheduler.MIN_TRACK, Math.min(DiskScheduler.MAX_TRACK, value));
        return (bounded / DiskScheduler.SHELF_STEP) * DiskScheduler.SHELF_STEP;
    }

    private List<Integer> normalizeToShelfTracks(List<Integer> values) {
        List<Integer> normalized = new ArrayList<>();
        for (int value : values) {
            normalized.add(normalizeTrack(value));
        }
        return normalized;
    }

    public static class ComparisonResult {
        private final List<Integer> sstfPath;
        private final List<Integer> scanPath;
        private final int sstfDistance;
        private final int scanDistance;
        private final int sstfTime;
        private final int scanTime;
        private final String betterAlgorithm;

        public ComparisonResult(
                List<Integer> sstfPath,
                List<Integer> scanPath,
                int sstfDistance,
                int scanDistance,
                int sstfTime,
                int scanTime,
                String betterAlgorithm) {
            this.sstfPath = sstfPath;
            this.scanPath = scanPath;
            this.sstfDistance = sstfDistance;
            this.scanDistance = scanDistance;
            this.sstfTime = sstfTime;
            this.scanTime = scanTime;
            this.betterAlgorithm = betterAlgorithm;
        }

        public List<Integer> getSstfPath() {
            return sstfPath;
        }

        public List<Integer> getScanPath() {
            return scanPath;
        }

        public int getSstfDistance() {
            return sstfDistance;
        }

        public int getScanDistance() {
            return scanDistance;
        }

        public int getSstfTime() {
            return sstfTime;
        }

        public int getScanTime() {
            return scanTime;
        }

        public String getBetterAlgorithm() {
            return betterAlgorithm;
        }

        @Override
        public String toString() {
            return "ComparisonResult{" +
                    "sstfPath=" + sstfPath +
                    ", scanPath=" + scanPath +
                    ", sstfDistance=" + sstfDistance +
                    ", scanDistance=" + scanDistance +
                    ", sstfTime=" + sstfTime +
                    ", scanTime=" + scanTime +
                    ", betterAlgorithm='" + betterAlgorithm + '\'' +
                    '}';
        }
    }
}
