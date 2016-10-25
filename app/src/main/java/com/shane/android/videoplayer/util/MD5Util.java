package com.shane.android.videoplayer.util;

import android.util.Log;

import java.security.MessageDigest;

public class MD5Util {

    private static final String TAG = MD5Util.class.getSimpleName();

    private MD5Util() { /* empty */ }

    public static String getMd5DigestUpperCase(String input) {
        if (input == null) {
            return null;
        }
        String lowerCaseMd5 = getDataMd5Digest(input.getBytes());
        if (lowerCaseMd5 == null) {
            return null;
        }
        return lowerCaseMd5.toUpperCase();
    }


    public static String getDataMd5Digest(final byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return getHexString(md.digest());
        } catch (Exception e) {
            Log.e(TAG, "getDataMd5Digest", e);
            return null;
        }
    }

    public static String getHexString(byte[] b) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int c = (b[i] & 0xf0) >> 4;
            builder.append((char) ((c >= 0 && c <= 9) ? '0' + c : 'a' + c - 10));
            c = (b[i] & 0xf);
            builder.append((char) ((c >= 0 && c <= 9) ? '0' + c : 'a' + c - 10));
        }
        return builder.toString();
    }
}
