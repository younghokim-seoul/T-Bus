package com.windnsoft.tbus.General;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class WsStringUtil {
    public static String RmoveSpecialCharacters(String str) {
        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        str = str.replaceAll(match, "");
        return str;
    }

    public static String RemoveNotWordCharacters(String str) {
        return str.replaceAll("\\W", "");
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public static long value(String string) {
        string = string.trim();
        if (string.contains(".")) {
            final int index = string.lastIndexOf(".");
            return value(string.substring(0, index)) * 1000 + value(string.substring(index + 1));
        } else {
            return Long.valueOf(string);
        }
    }
}
