package com.windnsoft.tbus.General;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class WsLog {

    static final String TAG = "WINDNSOFT";
    static boolean DEBUG = false;

    public static final void e(String message) {
        if (DEBUG) Log.e(TAG, buildLogMsg(message));
    }

    public static final void w(String message) {
        if (DEBUG) Log.w(TAG, buildLogMsg(message));
    }

    public static final void i(String message) {
        if (DEBUG) Log.i(TAG, buildLogMsg(message));
    }

    public static final void d(String message) {
        if (DEBUG) Log.d(TAG, buildLogMsg(message));
    }

    public static final void v(String message) {
        if (DEBUG) Log.v(TAG, buildLogMsg(message));
    }

    public static String buildLogMsg(String message) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);

        return sb.toString();
    }

    public static boolean isDebuggable(Context context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            debuggable = (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
        } catch (Exception e) {
        }

        return debuggable;
    }

    public static void init(Context context){
        DEBUG = isDebuggable(context);
    }
}
