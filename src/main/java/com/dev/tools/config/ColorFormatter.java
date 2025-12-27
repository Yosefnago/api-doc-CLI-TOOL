package com.dev.tools.config;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @Since 1.1.0
 */
public class ColorFormatter extends Formatter {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GRAY = "\u001B[90m";
    private static final String BLUE = "\u001B[34m";

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @Override
    public String format(LogRecord r) {

        String color = switch (r.getLevel().getName()) {
            case "INFO" -> GREEN;
            case "WARNING" -> YELLOW;
            case "SEVERE" -> RED;
            default -> RESET;
        };

        String time = TIME.format(Instant.ofEpochMilli(r.getMillis()));
        String msg = formatMessage(r);

        if (msg.startsWith("-> Generated ")) {
            String file = msg.substring("-> Generated ".length());
            msg = BLUE + "-> Generated " + RESET + file;
        }

        return GRAY + "[" + time + "]" + RESET + " " +
                color + r.getLevel().getName() + RESET + ": " +
                msg + "\n";
    }
}
