package com.windnsoft.tbus.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.windnsoft.tbus.Common.BaseApplication;
import com.windnsoft.tbus.R;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";

    static boolean isNetworkOfflined;

    private static NetworkStateReceiver instance;

    public static NetworkStateReceiver getInstance() {
        if (instance == null) instance = new NetworkStateReceiver();
        return instance;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.d(TAG, "Network connectivity change");

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting()) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");

/*
                final Activity uiActivity = BaseApplication.getUiActivity();
                uiActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alert = new AlertDialog.Builder(uiActivity);
                        alert.setPositiveButton(uiActivity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alert.setMessage("인터넷 연결이 끊어졌습니다. 5초후 재시도합니다");
                        alert.show();
                    }
                });
*/
                try {
                    BaseApplication.getApplication().unregisterReceiver(getInstance());
                } catch (Exception e) {
                }

                if (!BaseApplication.isActivityVisible()) {
                    setNetworkOfflined(true);
                    BaseApplication.destroy();
                    return;
                } else {
                    setNetworkOfflined(true);
                    BaseApplication.showRestartDialog(BaseApplication.getApplication().getString(R.string.restart_cause_network));
                }

//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                BaseApplication.restartApp();
            }
        }
    }

    public static boolean isNetworkOfflined() {
        return isNetworkOfflined;
    }

    public static void setNetworkOfflined(boolean networkOfflined) {
        isNetworkOfflined = networkOfflined;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BaseApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}