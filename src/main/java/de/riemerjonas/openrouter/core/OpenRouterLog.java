package de.riemerjonas.openrouter.core;

import java.text.SimpleDateFormat;

public class OpenRouterLog
{
    private static LOG_LEVEL log_Level = LOG_LEVEL.INFO;

    public static enum LOG_LEVEL
    {
        DEBUG(0),
        INFO(1),
        WARNING(2),
        ERROR(3);
        private int level;
        private LOG_LEVEL(int level)
        {
            this.level = level;
        }
        public int getLevel()
        {
            return level;
        }
    }


    public static void setLogLevel(LOG_LEVEL log_Level)
    {
        OpenRouterLog.log_Level = log_Level;
    }

    /**
     * Returns the current timestamp in the format hh:mm:ss
     * @return the current timestamp
     */
    public static String getTimestamp()
    {
        return new SimpleDateFormat("hh:mm:ss").format(System.currentTimeMillis());
    }

    /**
     * Formats the log message with the given timestamp, tag and type.
     * @param tag the tag of the log message
     * @param type the type of the log message
     * @return the formatted log message
     */
    public static String formatLog(String tag, String type)
    {
        return String.format("[%s] [%s] [%s] ", getTimestamp(), type, tag);
    }

    /**
     * Logs a info message with the given tag and message.
     * @param tag the tag of the log message
     * @param message the message to log
     */
    public static void i(String tag, String message)
    {
        if(log_Level.getLevel() <= LOG_LEVEL.INFO.getLevel())
        {
            System.out.println(formatLog(tag, "INFO ") + message);
        }
    }

    /**
     * Logs a debug message with the given tag and message.
     * @param tag the tag of the log message
     * @param message the message to log
     */
    public static void d(String tag, String message)
    {
        if(log_Level.getLevel() <= LOG_LEVEL.DEBUG.getLevel())
        {
            System.out.println(formatLog(tag, "DEBUG") + message);
        }
    }

    /**
     * Logs a warning message with the given tag and message.
     * @param tag the tag of the log message
     * @param message the message to log
     */
    public static void w(String tag, String message)
    {
        if(log_Level.getLevel() <= LOG_LEVEL.WARNING.getLevel())
        {
            System.out.println(formatLog(tag, "WARN ") + message);
        }
    }

    /**
     * Logs an error message with the given tag and message.
     * @param tag the tag of the log message
     * @param message the message to log
     */
    public static void e(String tag, String message)
    {
        if(log_Level.getLevel() <= LOG_LEVEL.ERROR.getLevel())
        {
            System.err.println(formatLog(tag, "ERROR") + message);
        }
    }

    /**
     * Logs an error message with the given tag and message and prints the stack trace.
     * @param tag the tag of the log message
     * @param message the message to log
     * @param e the exception to log
     */
    public static void e(String tag, String message, Exception e) {
        if (log_Level.getLevel() <= LOG_LEVEL.ERROR.getLevel()) {

            String line1 = formatLog(tag, "ERROR") + message;
            String line2 = formatLog(tag, "ERROR") + "Error at class: " + e.getStackTrace()[0].getClassName();
            String line3 = formatLog(tag, "ERROR") + "Error at line: " + e.getStackTrace()[0].getLineNumber();
            String line4 = formatLog(tag, "ERROR") + "Error at method: " + e.getStackTrace()[0].getMethodName();
            String line5 = formatLog(tag, "ERROR") + "Error message: " + e.getMessage();

            int length = Math.max(line1.length(), Math.max(line2.length(),
                            Math.max(line3.length(), Math.max(line4.length(), line5.length()))));
            StringBuilder separator = new StringBuilder();
            separator.append("-".repeat(length));

            System.err.println(line1);
            System.err.println(separator);
            System.err.println(line2);
            System.err.println(line3);
            System.err.println(line4);
            System.err.println(line5);
            System.err.println(separator);
        }
    }

}
