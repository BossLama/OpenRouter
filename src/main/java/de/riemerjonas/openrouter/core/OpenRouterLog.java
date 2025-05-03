package de.riemerjonas.openrouter.core;

import java.text.SimpleDateFormat;

public class OpenRouterLog {

    private static LogLevel logLevel = LogLevel.INFO;

    /**
     * Set the log level for the logger.
     * @param level The log level to set
     */
    public static void setLogLevel(LogLevel level)
    {
        logLevel = level;
    }

    /**
     * Get the formatted timestamp for the log message.
     * @return The formatted timestamp
     */
    private static String getTimestamp()
    {
        return new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
    }

    /**
     * Return the prefix for the log message.
     * @param tag The tag for the log message
     * @param type The type of the log message (INFO, ERROR, etc.)
     * @return The formatted prefix for the log message
     */
    private static String getPrefix(String tag, String type)
    {
        return String.format("[%s] [%s] [%s] ", getTimestamp(), type, tag);
    }

    /**
     * Format the log message with the given tag, type, and message.
     * @param tag The tag for the log message
     * @param type The type of the log message (INFO, ERROR, etc.)
     * @param message The message to log
     * @return The formatted log message
     */
    private static String formatLogMessage(String tag, String type, String message)
    {
        return getPrefix(tag, type) + message;
    }

    /**
     * Log an info message.
     * @param tag The tag for the log message
     * @param message The message to log
     */
    public static void info(String tag, String message)
    {
        if (logLevel.getLevel() > LogLevel.INFO.getLevel()) return;
        System.out.println(formatLogMessage(tag, "INFO", message));
    }

    /**
     * Log an error message.
     * @param tag The tag for the log message
     * @param message The message to log
     */
    public static void error(String tag, String message)
    {
        if (logLevel.getLevel() > LogLevel.ERROR.getLevel()) return;
        System.err.println(formatLogMessage(tag, "ERROR", message));
    }

    /**
     * Log a warning message.
     * @param tag The tag for the log message
     * @param message The message to log
     */
    public static void warn(String tag, String message)
    {
        if (logLevel.getLevel() > LogLevel.WARN.getLevel()) return;
        System.err.println(formatLogMessage(tag, "WARN", message));
    }

    /**
     * An enum representing the log levels.
     */
    public enum LogLevel
    {
        INFO(0),
        WARN(1),
        ERROR(2);

        private final int level;
        LogLevel(int level)
        {
            this.level = level;
        }

        public int getLevel()
        {
            return level;
        }
    }
}
