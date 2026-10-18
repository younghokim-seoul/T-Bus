package com.windnsoft.tbus.General;

import android.content.DialogInterface;

import java.util.HashMap;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public abstract class DialogDismissListener implements DialogInterface.OnDismissListener {

    private HashMap<String, String> mStrMap;
    private HashMap<String, Integer> mIntMap;
    private HashMap<String, Boolean> mBoolMap;

    public void setValue(String key, String value) {
        if (mStrMap == null)
            mStrMap = new HashMap<String, String>();

        mStrMap.put(key, value);
    }

    public void setValue(String key, int value) {
        if (mIntMap == null)
            mIntMap = new HashMap<String, Integer>();

        mIntMap.put(key, value);
    }

    public void setValue(String key, boolean value) {
        if (mBoolMap == null)
            mBoolMap = new HashMap<String, Boolean>();

        mBoolMap.put(key, value);
    }

    public String getValueForStr(String key) {
        if (mStrMap == null)
            return null;
        else
            return mStrMap.get(key);
    }

    public int getValueForInt(String key, int defaultvalue) {
        if (mIntMap == null)
            return defaultvalue;
        else {
            if (mIntMap.get(key) == null)
                return defaultvalue;
            else
                return mIntMap.get(key);
        }
    }

    public boolean getValueForBool(String key, boolean defaultvalue) {
        if (mBoolMap == null)
            return false;
        else {
            if (mBoolMap.get(key) == null)
                return defaultvalue;
            else
                return mBoolMap.get(key);
        }
    }
}