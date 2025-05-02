package de.riemerjonas.openrouter.core;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenRouterLog
{
    private static File LOG_FILE = new File("openrouter.log");
    private static boolean ENABLE_FILE_LOGGING = false;
    private static boolean ENABLE_CONSOLE_LOGGING = true;

    /**
     * Enables or disables file logging.
     * @param enableFileLogging true to enable file logging, false to disable it.
     */
    public static void setEnableFileLogging(boolean enableFileLogging)
    {
        ENABLE_FILE_LOGGING = enableFileLogging;
    }

    /**
     * Enables or disables console logging.
     * @param enableConsoleLogging true to enable console logging, false to disable it.
     */
    public static void setEnableConsoleLogging(boolean enableConsoleLogging)
    {
        ENABLE_CONSOLE_LOGGING = enableConsoleLogging;
    }

    /**
     * Sets the log file to the given file.
     * @param logFile The file to set as the log file.
     */
    public static void setLogFile(File logFile)
    {
        LOG_FILE = logFile;
    }

    /**
     * Gets the log file.
     * @return The log file.
     */
    public static File getLogFile()
    {
        return LOG_FILE;
    }

    /**
     * Logs a message with the given tag and level.
     * @param tag The tag to log with.
     * @param message The message to log.
     */
    public static void i(String tag, String message)
    {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String prefix = String.format("%s [INFO] [%s] ", time, tag);
        String logMessage = prefix + message;
        if (ENABLE_CONSOLE_LOGGING) System.out.println(logMessage);
        if (ENABLE_FILE_LOGGING)
        {
            try
            {
                java.nio.file.Files.writeString(LOG_FILE.toPath(), logMessage + "\n", java.nio.file.StandardOpenOption.APPEND);
            }
            catch (Exception e)
            {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        }
    }

    /**
     * Logs a message with the given tag and level.
     * @param tag The tag to log with.
     * @param message The message to log.
     */
    public static void e(String tag, String message)
    {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String prefix = String.format("%s [ERRO] [%s] ", time, tag);
        String logMessage = prefix + message;
        if (ENABLE_CONSOLE_LOGGING) System.err.println(logMessage);
        if (ENABLE_FILE_LOGGING)
        {
            try
            {
                java.nio.file.Files.writeString(LOG_FILE.toPath(), logMessage + "\n", java.nio.file.StandardOpenOption.APPEND);
            }
            catch (Exception e)
            {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        }
    }




}
