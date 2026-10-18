package com.windnsoft.tbus.Common;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.MainActivity;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class BaseApplication extends Application {

    private static Application instance;

    static Activity UiActivity;
    static boolean activityVisible;
    static AlarmManager mgr;

    final public static int MainHandler_Destroy = 404;
    final public static int MainHandler_Restart = 400;
    final public static int MainHandler_Dialog_Restart = 101;
    private static Handler MainHandler;

    private static PendingIntent mPendingIntent;

    public static void setMainHandler(Handler handler) {
        MainHandler = handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (instance == null) instance = this;
        WsLog.init(this);

        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024)
                .build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP


    }

    public static Application getApplication() {
        return instance;
    }

    public static Activity getUiActivity() {
        return UiActivity;
    }

    public static void setUiActivity(Activity uiActivity) {
        UiActivity = uiActivity;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static void restartApp() {
        WsLog.i("restartApp");

        cancelIntent();
        if (MainHandler != null) {
            MainHandler.sendEmptyMessage(MainHandler_Restart);
//            MainHandler.removeCallbacksAndMessages(null);
//            MainHandler = null;
            return;
        } else {
            Intent mStartActivity = new Intent(instance, MainActivity.class);
            int mPendingIntentId = 123456;
            mPendingIntent = PendingIntent.getActivity(instance, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            mgr = (AlarmManager) instance.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
            if (UiActivity != null) {
//                UiActivity.moveTaskToBack(true);
                UiActivity.finish();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(0);
        }
//        UiActivity = null;
    }

    public static void destroy() {
//        System.exit(0);
//        if (UiActivity != null)
//            UiActivity.finish();
        if (MainHandler != null) {
            cancelIntent();
            MainHandler.sendEmptyMessage(MainHandler_Destroy);
//            MainHandler.removeCallbacksAndMessages(null);
//            MainHandler = null;
            return;

        } else {
            if (UiActivity != null) {
//                UiActivity.moveTaskToBack(true);
                UiActivity.finish();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        }
//        UiActivity = null;
    }

    public static void showRestartDialog(String text) {
        if (MainHandler != null) {
            Message msg = Message.obtain();
            msg.what = MainHandler_Dialog_Restart;
            msg.obj = text;
            MainHandler.sendMessage(msg);
        }
    }

    static public void cancelIntent() {
        if (mgr != null && mPendingIntent != null)
            mgr.cancel(mPendingIntent);
    }

}