package com.yongyida.robot.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Administrator on 2016/3/30 0030.
 */
public class MyCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "MyCrashHandler";
    private static MyCrashHandler sInstance;

    private MyCrashHandler() {
    }

    public static synchronized MyCrashHandler getInstance() {
        if (sInstance == null) {
            sInstance = new MyCrashHandler();
        }
        return sInstance;
    }

    public void uncaughtException(Thread arg0, Throwable arg1) {
        String errorinfo = getErrorInfo(arg1);
        Log.e(TAG, "ERROR: " + errorinfo);
    }

    private String getErrorInfo(Throwable arg) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        arg.printStackTrace(pw);
        pw.close();
        return writer.toString();
    }
}