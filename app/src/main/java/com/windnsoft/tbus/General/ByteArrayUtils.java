package com.windnsoft.tbus.General;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class ByteArrayUtils {
    public static List<byte[]> split(byte[] array, byte[] delimiter) {
        List<byte[]> byteArrays = new LinkedList<>();
        if (delimiter.length == 0) {
            return byteArrays;
        }
        int begin = 0;

        outer:
        for (int i = 0; i < array.length - delimiter.length + 1; i++) {
            for (int j = 0; j < delimiter.length; j++) {
                if (array[i + j] != delimiter[j]) {
                    continue outer;
                }
            }
            byteArrays.add(Arrays.copyOfRange(array, begin, i));
            begin = i + delimiter.length;
        }
        byteArrays.add(Arrays.copyOfRange(array, begin, array.length));
        return byteArrays;
    }

    public static byte[] combine(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];

        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);
        return combined;
    }

    public static byte[] appendData(byte firstObject, byte[] secondObject) {
        byte[] byteArray = {firstObject};
        return appendData(byteArray, secondObject);
    }

    public static byte[] appendData(byte[] firstObject, byte secondByte) {
        byte[] byteArray = {secondByte};
        return appendData(firstObject, byteArray);
    }

    public static byte[] appendData(byte[] firstObject, byte[] secondObject) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (firstObject != null && firstObject.length != 0)
                outputStream.write(firstObject);
            if (secondObject != null && secondObject.length != 0)
                outputStream.write(secondObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}
