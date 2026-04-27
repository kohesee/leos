package simulation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OutputManager {
    private static PrintStream originalOut;
    private static PrintStream originalErr;
    private static BufferedWriter logWriter;
    private static Path logPath;
    private static boolean installed = false;

    public static synchronized void install(String runName) {
        if (installed) {
            return;
        }
        try {
            originalOut = System.out;
            originalErr = System.err;

            Path logsDir = Paths.get("logs");
            Files.createDirectories(logsDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US));
            String safeRunName = runName == null || runName.isBlank() ? "simulation" : runName.replaceAll("[^a-zA-Z0-9_-]", "_");

            logPath = logsDir.resolve(safeRunName + "_" + timestamp + ".log");

            logWriter = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8);

            System.setOut(new PrintStream(new ManagedOutputStream(false), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new ManagedOutputStream(true), true, StandardCharsets.UTF_8));

            installed = true;

            Runtime.getRuntime().addShutdownHook(new Thread(OutputManager::shutdown));

            printInfo("Log file: " + logPath.toAbsolutePath());
        } catch (Exception e) {
            if (originalErr != null) {
                originalErr.println("Output manager install failed: " + e.getMessage());
            }
        }
    }

    public static synchronized void shutdown() {
        try {
            if (logWriter != null) {
                logWriter.flush();
                logWriter.close();
            }
        } catch (IOException ignored) {
        } finally {
            if (originalOut != null) {
                System.setOut(originalOut);
            }
            if (originalErr != null) {
                System.setErr(originalErr);
            }
            installed = false;
        }
    }

    private static synchronized void processLine(String line, boolean isError) {
        try {
            String normalizedLine = normalizeLine(line);
            if (isSectionHeader(normalizedLine)) {
                writeToTerminal("", false);
                writeToLog("", false);
            }

            if (logWriter != null) {
                if (isError) {
                    logWriter.write("[STDERR] " + normalizedLine);
                } else {
                    logWriter.write(normalizedLine);
                }
                logWriter.newLine();
                logWriter.flush();
            }

            writeToTerminal(normalizedLine, isError);
        } catch (IOException e) {
            if (originalErr != null) {
                originalErr.println("Output write failure: " + e.getMessage());
            }
        }
    }

    private static void printInfo(String message) {
        processLine("[OUTPUT] " + message, false);
    }

    private static void writeToTerminal(String line, boolean isError) {
        if (isError && originalErr != null) {
            originalErr.println(line);
            return;
        }
        if (originalOut != null) {
            originalOut.println(line);
        }
    }

    private static void writeToLog(String line, boolean isError) throws IOException {
        if (logWriter == null) {
            return;
        }
        if (isError) {
            logWriter.write("[STDERR] " + line);
        } else {
            logWriter.write(line);
        }
        logWriter.newLine();
        logWriter.flush();
    }

    private static String normalizeLine(String line) {
        String s = line == null ? "" : line;
        s = s.replace("PHASE 2 COMPLETE", "WAREHOUSE OS");
        return toAscii(s);
    }

    private static String toAscii(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\t' || (c >= 32 && c <= 126)) {
                out.append(c);
            } else {
                out.append(' ');
            }
        }
        return out.toString().replaceAll(" +", " ").trim();
    }

    private static boolean isSectionHeader(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        return line.startsWith("---")
            || line.startsWith("===")
            || line.contains("STATISTICS")
            || line.contains("[ORCHESTRATOR] [LOAD]")
            || line.contains("[ORCHESTRATOR] [SETUP]")
            || line.contains("[ORCHESTRATOR] [EXEC]");
    }

    private static class ManagedOutputStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();
        private final boolean error;

        ManagedOutputStream(boolean error) {
            this.error = error;
        }

        @Override
        public synchronized void write(int b) {
            char c = (char) b;
            if (c == '\n') {
                flushBuffer();
                return;
            }
            if (c != '\r') {
                buffer.append(c);
            }
        }

        @Override
        public synchronized void flush() {
            flushBuffer();
        }

        private void flushBuffer() {
            if (buffer.length() == 0) {
                return;
            }
            String line = buffer.toString();
            buffer.setLength(0);
            processLine(line, error);
        }
    }
}