package com.windnsoft.tbus.Common;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;


public class MyApplication extends Application {
    Handler hTopUi;
    String uniqueNo = "";
    boolean bForeground = false;
    public boolean bEmergency;
    public String hostAddress = "1.234.83.190";        // IP 변경함 고정 IP
    public int hostPort = 20021;        // 포트 변경함 20020 -> 20021
    //public int hostPort = 20022;		// 포트 변경함 Speex 20022

    //final public static String DEF_IMAGE_UPLOAD_URL = "http://smartel.ppsys.co.kr:8080/Smartel/ImageServlet";
    //final public static String DEF_IMAGE_DOWNLOAD_URL = "http://smartel.ppsys.co.kr:8080/Smartel/ImageServlet?picture=";
    final public static String DEF_IMAGE_UPLOAD_URL = "http://1.234.83.190:8080/Smartel/ImageServlet";
    final public static String DEF_IMAGE_DOWNLOAD_URL = "http://1.234.83.190:8080/Smartel/ImageServlet?picture=";
    final public static String DEF_IMAGE_FOLDER = "/SMARTEL/IMAGE/";
    final public static String DEF_GOOGLE_STORE_DOWNLOAD_URL = "https://play.google.com/store/apps/details?id=ppsys.smartel";

    int notificationID = 150820; // 임시로 설정함

    final public static int DEF_MAX_CHANNEL = 9999;
    final int DEF_ONE_MIN = 60 * 1000;

    int channelUserCount = 0;

    /**
     * 콜 종료시 나오는 비프음
     */
    boolean bBeepCallEnd = false;

    /**
     * 스피커 모드
     */
    boolean bSpeakMode = false;


    /**
     * DEV1 전화 수신/발신 상태
     * Idle = true 전화사용중이 아님
     * Idle = false 전화사용중
     */
    boolean bPhoneCallIdle = true;

    /**
     * DEV1 20150831
     * 새 버전이 있음을 알릴 필요가 있으면 true
     * 서버로부터 받은 Packet 의 version 이 나의 Packet 버전보다 높으면 true 로 셋팅
     */
    boolean bUpdatedAnnounceNeed = false;
    /**
     * DEV1 20150831
     * bUpdatedAnnounceNeed = true 일때 새 버전이 있음을 공지하였는가?
     * 1 번만 공지하기 위한 값
     * 최초의 값은 false / 한번 알림하고 나면 앱을 종료할때까지 true
     */
    boolean bUpdatedAnnounced = false;


    /**
     * 블루투스 마이크 연결되었는지
     */
    boolean bBluetoothScoConnected = false;

    /**
     * 앱이 처음 실행될때 딱 1번만 Bluetooth Sco 를 On 하도록.
     * PhoneCallState 가 Idle 일 때 한번 동작
     */
    boolean b1timeBluetoothScoOn = true;

    SoundPool spChannel, spVoiceChannel;
    int spKeyOnId;
    int spKeyOffId;
    int spVoiceKeyOnId;
    int spVoiceKeyOffId;

    int headsetState = 0;
    int loginState = 0;
    final public static int DEF_LOGINING = 0;
    final public static int DEF_LOGIN_SECCESS = 1;
    final public static int DEF_LOGIN_FAIL = 2;





    /**
     * 연결 가능 여부 확인.
     *
     * @return
     */
    public boolean isConnectivity() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mWifi.isConnected()) {
            return true;
        }

        if (mMobile.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * TCP 연결 여부 확인.
     *
     * @return
     */

    public Handler TopUiHandler() {
        return hTopUi;
    }

    public void TopUiHandler(Handler h) {
        this.hTopUi = h;
    }

    public void SendMessageSafe(Message msg) {
        if (TopUiHandler() != null)
            TopUiHandler().sendMessage(msg);
    }

    public void SendMessageSafeDelayed(Message msg, int ms) {
        if (TopUiHandler() != null)
            TopUiHandler().sendMessageDelayed(msg, ms);
    }

    public void SendEmptyMessageSafe(int what) {
        if (TopUiHandler() != null)
            TopUiHandler().sendEmptyMessage(what);
    }

    public void SendEmptyMessafeSafeDelayed(int what, int ms) {
        if (TopUiHandler() != null)
            TopUiHandler().sendEmptyMessageDelayed(what, ms);
    }

    private String getMyPhoneNumber() {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    public String UniqueNo() {
        return uniqueNo;
    }

    public void HeadsetState(int headsetState) {
        this.headsetState = headsetState;
    }

    public int HeadsetState() {
        return headsetState;
    }

    public void LoginState(int loginState) {
        this.loginState = loginState;
    }

    public int LoginState() {
        return loginState;
    }

    public void Foreground(boolean bForeground) {
        this.bForeground = bForeground;
    }

    public boolean isForeground() {
        return bForeground;
    }

    public int ChannelUserCount() {
        return channelUserCount;
    }

    public void BeepCallEnd(boolean bBeepCallEnd) {
        this.bBeepCallEnd = bBeepCallEnd;
    }

    public boolean isBeepCallEnd() {
        return bBeepCallEnd;
    }

    public void SpeakMode(boolean bSpeakMode) {
        this.bSpeakMode = bSpeakMode;
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(bSpeakMode);
    }

    public boolean isSpeakMode() {
        return bSpeakMode;
    }


    PowerManager.WakeLock cpuLock;

    @SuppressLint("Wakelock")
    public void AcquireCpuLock() {
        if (cpuLock != null) {
            if (cpuLock.isHeld())
                cpuLock.release();
        }
        cpuLock = null;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        cpuLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rPartitialWakeLock");
        cpuLock.acquire();
//		ALogger.i("DEV1", "CPU Lock Aquired");
    }

    public void ReleaseCpuLock() {
        if (cpuLock != null) {
            if (cpuLock.isHeld())
                cpuLock.release();
        }
        cpuLock = null;

//		ALogger.i("DEV1", "CPU Lock Released");
    }


    /**
     * 알림 클리어
     */
    public void SendNotificationCancel() {
        NotificationManager aNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        aNotificationManager.cancel(notificationID);
    }



    public void BluetoothScoStop() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setBluetoothScoOn(false);
        am.stopBluetoothSco();
        am.setMode(AudioManager.MODE_NORMAL);
    }

    public void BluetoothScoConnected(boolean bConnected) {
        this.bBluetoothScoConnected = bConnected;
        if (bConnected) {
        } else {
            BluetoothScoStop();
        }
    }

    public boolean BluetoothScoConnected() {
        return bBluetoothScoConnected;
    }

    public void UiUpdateMyInfo() {
    }

}
