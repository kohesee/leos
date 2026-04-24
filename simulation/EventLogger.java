package simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * EventLogger.java
 * Centralized logging for all modules in the OS simulation
 * Format: [timestamp] [MODULE] [EVENT_TYPE] message
 */
public class EventLogger {
    private static final EventLogger instance = new EventLogger();
    private final List<String> eventLog = new ArrayList<>();
    private final long startTime = System.currentTimeMillis();

    private EventLogger() {
    }

    public static EventLogger getInstance() {
        return instance;
    }

    public void log(String module, String eventType, String message) {
        long timestamp = System.currentTimeMillis() - startTime;
        String logEntry = String.format("[%dms] [%s] [%s] %s", timestamp, module, eventType, message);
        eventLog.add(logEntry);
        System.out.println(logEntry);
    }

    public void log(String module, String message) {
        log(module, "EVENT", message);
    }

    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    public void printFullLog() {
        System.out.println("\n========== COMPLETE EVENT LOG ==========");
        for (String event : eventLog) {
            System.out.println(event);
        }
        System.out.println("========================================\n");
    }

    public void clearLog() {
        eventLog.clear();
    }

    public int getEventCount() {
        return eventLog.size();
    }
}
