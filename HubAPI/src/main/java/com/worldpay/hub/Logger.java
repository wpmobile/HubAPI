package com.worldpay.hub;

import android.util.Log;

/**
 * Created by SixthSense on 24/02/16.
 */
public class Logger {

    private static LogLevel debugLogLevel = LogLevel.DEBUG_LOGS;

    public static LogLevel getDebugLogLevel() {
        return debugLogLevel;
    }

    public static void setDebugLogLevel(LogLevel logLevel) {
        debugLogLevel = logLevel;
    }

    public enum LogLevel {
        NO_LOGS(0),
        DEBUG_LOGS(1),
        DEBUG_LOGS_EXTENDED(2);

        private final int value;

        LogLevel(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }

        public LogLevel getLogLevelFromValue(int value) {
            switch (value) {
                case 0:
                    return NO_LOGS;
                case 1:
                    return DEBUG_LOGS;
                case 2:
                    return DEBUG_LOGS_EXTENDED;
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
    }

    public static void d(String tag, String msg) {
        if (debugLogLevel != LogLevel.NO_LOGS && debugLogLevel == LogLevel.DEBUG_LOGS_EXTENDED) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (debugLogLevel != LogLevel.NO_LOGS && debugLogLevel == LogLevel.DEBUG_LOGS) {
            Log.e(tag, msg);
        }
    }
}
