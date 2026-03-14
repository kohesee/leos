package io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// PathOptimizer.java
// SSTF and SCAN algorithms
// [cite: 49, 50]
public class PathOptimizer {

    public List<Integer> sstfOrder(int startShelf, List<Integer> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> pending = new ArrayList<>(requests);
        List<Integer> order = new ArrayList<>();
        int current = startShelf;

        while (!pending.isEmpty()) {
            int bestIndex = 0;
            int bestDistance = Math.abs(pending.get(0) - current);

            for (int i = 1; i < pending.size(); i++) {
                int distance = Math.abs(pending.get(i) - current);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }

            int next = pending.remove(bestIndex);
            order.add(next);
            current = next;
        }

        return order;
    }

    public List<Integer> scanOrder(int startShelf, List<Integer> requests, int maxShelf, boolean moveUpFirst) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        if (maxShelf < startShelf) {
            throw new IllegalArgumentException("maxShelf must be greater than or equal to startShelf");
        }

        if (startShelf < 0) {
            throw new IllegalArgumentException("startShelf cannot be negative");
        }

        List<Integer> lower = new ArrayList<>();
        List<Integer> higher = new ArrayList<>();

        for (int request : requests) {
            if (request < 0 || request > maxShelf) {
                throw new IllegalArgumentException("request out of range: " + request);
            }

            if (request <= startShelf) {
                lower.add(request);
            } else {
                higher.add(request);
            }
        }

        Collections.sort(lower);
        Collections.sort(higher);

        List<Integer> order = new ArrayList<>();
        if (moveUpFirst) {
            order.addAll(higher);
            for (int i = lower.size() - 1; i >= 0; i--) {
                order.add(lower.get(i));
            }
        } else {
            for (int i = lower.size() - 1; i >= 0; i--) {
                order.add(lower.get(i));
            }
            order.addAll(higher);
        }

        return order;
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

    public String formatMovementTrace(int startShelf, List<Integer> order) {
        StringBuilder builder = new StringBuilder();
        builder.append("Shelf ").append(startShelf);
        for (int shelf : order) {
            builder.append(" -> ").append(shelf);
        }
        return builder.toString();
    }
}
