package com.windnsoft.tbus.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.windnsoft.tbus.Entities.ChannelsEntity;
import com.windnsoft.tbus.Entities.PreferenceEntity;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.Entities.UserTypes;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class SharedPreferencesCache {
    private final static String KeyUserInfo = "UserInfo";
    private final static String KeyUserType = "UserType";
    private final static String KeyUserName = "UserName";
    private final static String KeyDeviceUUID = "DeviceUUID";
    private final static String KeyUserIndex = "UserIndex";
    private final static String KeyUserNickname = "UserNickname";
    private final static String KeyUserPhone = "UserPhone";
    private final static String KeyUserPassword = "UserPassword";
    private final static String KeyUserEntityJson = "UserEntityJson";
    private final static String KeyOverrideRealPhone = "OverrideRealPhone";
    public final static String KeyUserAgreement = "UserAgreement";
    private final static String KeyUserPicture = "UserPicture";
    private final static String KeyIfKey = "IfKey";
    private final static String KeyUserNoChannel = "NoChannel";

    private final static String KeyEnvironments = "Environments";
    private final static String KeyChannelInfo = "ChannelInfo";
    private final static String KeyLastChannelId = "LastChannelId";
    private final static String KeyLastChannelNumber = "LastChannelNumber";
    private final static String KeyLastChannelTitle = "LastChannelTitle";
    private final static String KeyLastChannelPass = "LastChannelPass";
    private final static String KeyChannelNickname = "ChannelNickname";

    private final static String KeyPreference = "Preference";
    public final static String KeyAutoLogin = "AutoLogin";
    private final static String KeyTTS = "TTS";
    private final static String KeyPTTBtnToggle = "PTTBtnToggle";
    private final static String KeyUseVolumeBtnPTT = "UseVolumeButtonPTT";
    private final static String KeyShowMySpeed = "MySpeed";
    private final static String KeyBeepOnFinishTalk = "BeepOnFinishTalk";
    private final static String KeyJoinedChannel = "JoinedChannel";
    private final static String KeyShowPhotoAnimation = "ShowPhotoAnimation";
    private final static String KeyShowLocation = "ShowLocation";
    final static public String KeyNormVolume = "NormVolume";
    final static public String KeyTTSVolume = "TTSVolume";
    final static public String KeyBeepVolume = "BeepVolume";

    public final static String ValueTrue = "true";
    public final static String ValueFalse = "false";

    public final static int MaxVolume = 4000;
    public final static int MinVolume = -5000;
    public final static int MaxTTSVolume = 10;
    public final static int MaxBeepVolume = 10;

    private static UserEntity user = null;
    private static PreferenceEntity preference = null;
    private static ChannelsEntity lastChannel = null;

    public static long getUserIndex() {
        Context context = BaseApplication.getApplication().getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(KeyUserInfo, context.MODE_PRIVATE);
        return prefs.getLong(KeyUserIndex, 0);
    }

    public static String getDeviceUUID() {
        Context context = BaseApplication.getApplication().getApplicationContext();

        UUID uuid = null;
        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        try {
            if (androidId != null && !"9774d56d682e549c".equals(androidId)) {
                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            } else {
                uuid = UUID.randomUUID();

            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return uuid.toString();
    }

    public static String getRealPhoneNumber() {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyUserInfo, context.MODE_PRIVATE);
        String overridePhone = prefs.getString(KeyOverrideRealPhone, null);
        if (overridePhone != null && !overridePhone.isEmpty()) {
            return overridePhone;
        }

        TelephonyManager telManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String phoneNum;
        try {
            phoneNum = telManager.getLine1Number();
        } catch (SecurityException | NullPointerException ex) {
            return null;
        }

//        if (phoneNum.equals("")) {
//            phoneNum = telManager.getSubscriberId();
//        }

        if (phoneNum != null && phoneNum.startsWith("+82")) {
            phoneNum = phoneNum.replace("+82", "0");
        }
        return phoneNum;
    }

    public static UserEntity getCurrentUser() {
        user = new UserEntity();

        Context context = BaseApplication.getApplication().getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(KeyUserInfo, context.MODE_PRIVATE);
        String userJson = prefs.getString(KeyUserEntityJson, null);
        if (userJson != null && !userJson.isEmpty()) {
            try {
                UserEntity parsed = new Gson().fromJson(userJson, UserEntity.class);
                if (parsed != null) {
                    user = parsed;
                }
            } catch (Exception ignored) {
            }
        }
        user.id = prefs.getInt(KeyUserIndex, user.id);
        String phoneNum = prefs.getString(KeyUserPhone, null);
        if (phoneNum == null || phoneNum.equals("")) phoneNum = getRealPhoneNumber();
        user.tel = phoneNum;
        user.name = prefs.getString(KeyUserName, user.name);
        user.type = prefs.getInt(KeyUserType, user.type > 0 ? user.type : UserTypes.Normal);
        user.password = prefs.getString(KeyUserPassword, user.password != null ? user.password : "");

        return user;
    }

    public static void setCurrentUser(UserEntity user) {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyUserInfo, context.MODE_PRIVATE);

        // Preserve locally edited detail fields if login response only contains basic fields
        String existingJson = prefs.getString(KeyUserEntityJson, null);
        if (existingJson != null && !existingJson.isEmpty()) {
            try {
                UserEntity existing = new Gson().fromJson(existingJson, UserEntity.class);
                if (existing != null && (existing.id == user.id || user.id <= 0)) {
                    if (user.garage == null) user.garage = existing.garage;
                    if (user.ride == null) user.ride = existing.ride;
                    if (user.company_name == null) user.company_name = existing.company_name;
                    if (user.passengers == null) user.passengers = existing.passengers;
                    if (user.possible_date == null) user.possible_date = existing.possible_date;
                    if (user.car_number == null) user.car_number = existing.car_number;
                    if (user.profile == null) user.profile = existing.profile;
                    if (user.photo == null) user.photo = existing.photo;
                    if (user.photoList == null) user.photoList = existing.photoList;
                    if (user.password == null) user.password = existing.password;
                }
            } catch (Exception ignored) {
            }
        }

        SharedPreferencesCache.user = user;
        SharedPreferences.Editor editor = prefs.edit();
        if (user.id > 0)
            editor.putInt(KeyUserIndex, user.id);
        if (user.tel != null && !user.tel.equals(""))
            editor.putString(KeyUserPhone, user.tel);
        if (user.name != null && !user.name.equals(""))
            editor.putString(KeyUserName, user.name);
        editor.putInt(KeyUserType, user.type);
        if (user.password != null)
            editor.putString(KeyUserPassword, user.password);
        editor.putString(KeyUserEntityJson, new Gson().toJson(user));
//        if (user.user_picture != null && !user.user_picture.equals(""))
//            editor.putString(KeyUserPicture, user.user_picture);
        editor.commit();
    }
    public static void setData(String key, String value) {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyEnvironments, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getStringData(String key) {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyEnvironments, context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static void setData(String key, int value) {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyEnvironments, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntData(String key) {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyEnvironments, context.MODE_PRIVATE);
        return prefs.getInt(key, -1);
    }

    public static void setPreference(PreferenceEntity preference) {

        Context context = BaseApplication.getApplication().getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(KeyPreference, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KeyBeepOnFinishTalk, preference.beepOnFinishTalk);
        editor.putBoolean(KeyPTTBtnToggle, preference.pttBtnToggle);
        editor.putBoolean(KeyUseVolumeBtnPTT, preference.useVolumeBtnPtt);
        editor.putBoolean(KeyTTS, preference.tts);
        editor.putBoolean(KeyShowMySpeed, preference.showMySpeed);
        editor.putBoolean(KeyJoinedChannel, preference.joinedChannel);
        editor.putBoolean(KeyShowPhotoAnimation, preference.showPhotoAnimation);
        editor.putBoolean(KeyShowLocation, preference.showAddress);
        editor.putInt(KeyNormVolume, preference.normVolume);
        editor.putInt(KeyTTSVolume, preference.ttsVolume);
        editor.putInt(KeyBeepVolume, preference.beepVolume);

        editor.commit();
    }

    public static PreferenceEntity getPreference() {
        if (preference == null) {
            preference = new PreferenceEntity();

            Context context = BaseApplication.getApplication().getApplicationContext();

            SharedPreferences prefs = context.getSharedPreferences(KeyPreference, context.MODE_PRIVATE);
            preference.beepOnFinishTalk = prefs.getBoolean(KeyBeepOnFinishTalk, true);
            preference.showMySpeed = prefs.getBoolean(KeyShowMySpeed, false);
            preference.pttBtnToggle = prefs.getBoolean(KeyPTTBtnToggle, false);
            preference.useVolumeBtnPtt = prefs.getBoolean(KeyUseVolumeBtnPTT, false);
            preference.tts = prefs.getBoolean(KeyTTS, true);
            preference.joinedChannel = prefs.getBoolean(KeyJoinedChannel, false);
//            preference.showAddress = prefs.getBoolean(KeyShowLocation, false);
            preference.showPhotoAnimation = prefs.getBoolean(KeyShowPhotoAnimation, false);
            preference.normVolume = prefs.getInt(KeyNormVolume, 2000);
            preference.ttsVolume = prefs.getInt(KeyTTSVolume, 7); //max 10
            preference.beepVolume = prefs.getInt(KeyBeepVolume, 7); //max 10
        }

        return preference;
    }

    public static void setLastChannel(ChannelsEntity channel) {
        lastChannel = channel;
        Context context = BaseApplication.getApplication().getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(KeyChannelInfo, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KeyLastChannelId, channel.id);
        editor.putLong(KeyLastChannelNumber, channel.channel_number);
        editor.putString(KeyLastChannelTitle, channel.channel_title);
        editor.putString(KeyLastChannelPass, channel.channel_pw);

        editor.commit();
    }

    public static ChannelsEntity getLastChannel() {
        if (lastChannel == null) {
            Context context = BaseApplication.getApplication().getApplicationContext();
            SharedPreferences prefs = context.getSharedPreferences(KeyChannelInfo, context.MODE_PRIVATE);
            ChannelsEntity channel = new ChannelsEntity();
            channel.id = prefs.getLong(KeyLastChannelId, -1);
            if (channel.id <= 0) return null;
            channel.channel_number = prefs.getLong(KeyLastChannelNumber, -1);
            channel.channel_title = prefs.getString(KeyLastChannelTitle, "");
            channel.channel_pw = prefs.getString(KeyLastChannelPass, "");
            lastChannel = channel;
        }
        return lastChannel;
    }

    public static void putChannelPassword(long cid, String password) {
        Context context = BaseApplication.getApplication().getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(KeyChannelInfo, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cpw_" + cid, password);

        editor.commit();
    }

    public static @NonNull
    String getChannelPassword(long cid) {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyChannelInfo, context.MODE_PRIVATE);
        return prefs.getString("cpw_" + cid, "");

    }

    public static void setChannelNickname(String cid, String nickname) {
        Context context = BaseApplication.getApplication().getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(KeyChannelNickname, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cn_" + cid, nickname);

        editor.commit();
    }

    public static @NonNull
    String getChannelNickname(String cid) {
        Context context = BaseApplication.getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(KeyChannelNickname, context.MODE_PRIVATE);
        return prefs.getString("cn_" + cid, "");
    }
}
