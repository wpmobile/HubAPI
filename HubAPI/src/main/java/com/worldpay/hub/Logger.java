package com.worldpay.hub;

import android.util.Log;

/**
 * Created by SixthSense on 24/02/16.
 */
public class Logger {

    private static int debugLogLevel = LogLevel.NO_LOGS;

    public static int getDebugLogLevel() {
        return debugLogLevel;
    }

    public static void setDebugLogLevel(int logLevel) {
        debugLogLevel = logLevel;
    }

    public static class LogLevel {
        public static final int NO_LOGS = 0;
        public static final int DEBUG_LOGS = 1;
        public static final int DEBUG_LOGS_EXTENDED = 2;
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
