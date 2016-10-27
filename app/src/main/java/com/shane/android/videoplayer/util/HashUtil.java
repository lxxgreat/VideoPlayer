package com.shane.android.videoplayer.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    private static final String TAG = HashUtil.class.getSimpleName();

    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA1";

    private HashUtil() { /* empty */ }

    public static String getMD5(String content) {
        return getHash(content, MD5);
    }

    public static String getMD5(InputStream is) {
        return getHash(is, MD5);
    }

    public static String getMD5(File file) {
        return getHash(file, MD5);
    }

    public static String getSHA1(String content) {
        return getHash(content, SHA1);
    }

    public static String getSHA1(InputStream is) {
        return getHash(is, SHA1);
    }

    public static String getSHA1(File file) {
        return getHash(file, SHA1);
    }

    public static String getHash(String content, String method) {
        String hash = null;
        try {
            MessageDigest digester = MessageDigest.getInstance(method);
            digester.update(content.getBytes());
            hash = toHexString(digester.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    public static String getHash(InputStream is, String method) {
        String hash = null;
        try {
            MessageDigest digester = MessageDigest.getInstance(method);
            byte[] bytes = new byte[8192];
            int byteCount;
            while ((byteCount = is.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }
            byte[] digest = digester.digest();
            hash = toHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return hash;
    }

    public static String getHash(File file, String method) {
        String hash = null;
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            hash = getHash(is, method);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return hash;
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            hexString.append(String.format("%02x", 0xFF & bytes[i]));
        }
        return hexString.toString();
    }
}
