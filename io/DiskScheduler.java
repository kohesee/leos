package io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiskScheduler {
    public static final int MIN_TRACK = 0;
    public static final int MAX_TRACK = 100;
    public static final int SHELF_STEP = 10;

    public List<Integer> scheduleSSTF(List<Integer> shelves, int currentPos) {
        validatePosition(currentPos);
        if (shelves == null || shelves.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> pending = validateShelves(shelves);
        List<Integer> order = new ArrayList<>();
        int head = currentPos;

        while (!pending.isEmpty()) {
            int bestIndex = 0;
            int bestDistance = Math.abs(pending.get(0) - head);

            for (int i = 1; i < pending.size(); i++) {
                int distance = Math.abs(pending.get(i) - head);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }

            int next = pending.remove(bestIndex);
            order.add(next);
            head = next;
        }

        return order;
    }

    // SCAN direction is fixed to upward sweep first for this project.
    public List<Integer> scheduleSCAN(List<Integer> shelves, int currentPos) {
        validatePosition(currentPos);
        if (shelves == null || shelves.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> validated = validateShelves(shelves);
        List<Integer> lower = new ArrayList<>();
        List<Integer> higher = new ArrayList<>();

        for (int shelf : validated) {
            if (shelf < currentPos) {
                lower.add(shelf);
            } else {
                higher.add(shelf);
            }
        }

        Collections.sort(lower);
        Collections.sort(higher);

        List<Integer> order = new ArrayList<>();
        order.addAll(higher);
        for (int i = lower.size() - 1; i >= 0; i--) {
            order.add(lower.get(i));
        }

        return order;
    }

    private List<Integer> validateShelves(List<Integer> shelves) {
        List<Integer> validated = new ArrayList<>();
        for (int shelf : shelves) {
            validatePosition(shelf);
            if (shelf % SHELF_STEP != 0) {
                throw new IllegalArgumentException("Shelf position must be in steps of 10: " + shelf);
            }
            validated.add(shelf);
        }
        return validated;
    }

    private void validatePosition(int position) {
        if (position < MIN_TRACK || position > MAX_TRACK) {
            throw new IllegalArgumentException("Track out of range 0-100: " + position);
        }
    }
}
