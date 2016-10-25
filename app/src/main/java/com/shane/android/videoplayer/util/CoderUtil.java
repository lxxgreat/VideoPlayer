package com.shane.android.videoplayer.util;


import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import org.apache.commons.codec.binary.Base64;

public class CoderUtil {
    private static final String TAG = "CoderUtils";

    public static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    private final static String[] hexDigits = {
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"
    };

    public static final String encodeMD5(String string) {
        if (null == string || string.length() == 0) {
            return null;
        }
        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        digester.update(string.getBytes());
        byte[] digest = digester.digest();
        return byteArrayToString(digest);
    }

    public static final String encodeMD5(File file) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                Log.e(TAG, " IOException:" + e);
            }
        }
        if(md5!=null){
            return byteArrayToString(md5.digest());
        }else{
            return "";
        }
    }

    private static String byteArrayToString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static final String encodeSHA(String string) {
        if (null == string || string.length() == 0) {
            return null;
        }
        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        digester.update(string.getBytes());
        byte[] digest = digester.digest();
        return byteArrayToString(digest);
    }

    public static final byte[] encodeSHABytes(String string) {
        if (null == string || string.length() == 0) {
            return null;
        }
        MessageDigest digester = null;
        try {
            digester = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        digester.update(string.getBytes());
        return digester.digest();
    }

    public static final byte[] encodeBase64(String string) {
        return Base64.encode(string.getBytes(), Base64.NO_PADDING);// Base64.encodeBase64(string.getBytes());
    }

    public static final byte[] encodeBase64(byte[] bytes) {
        return Base64.encode(bytes, Base64.NO_PADDING);
    }

    public static final byte[] encodeBase64Bytes(String string) {
        return Base64.encode(string.getBytes(), Base64.NO_PADDING);// Base64.encodeBase64(string.getBytes());
    }

    public static final String decodeBase64(String string) {
        return new String(Base64.decode(string.getBytes(), Base64.NO_PADDING));// Base64.decodeBase64(string.getBytes()));
    }

    public static final byte[] decodeBase64Bytes(String string) {
        return Base64.decode(string, Base64.NO_PADDING);// Base64.decodeBase64(string.getBytes());
    }

    /**
     * 返回经过AES加密和base64编码后的数据 base64AesEncode
     */
    public static final String base64AesEncode(String data, String key) {
        if (null == data || data.length() == 0) {
            return null;
        }
        // byte[] raw = decodeBase64Bytes(key);
        byte[] raw = key.getBytes();
        /*
         * security limits,the key must 16 byte for common user
         */
        if (raw == null || raw.length != 16) {
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");

        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            return new String(encodeBase64(cipher.doFinal(data.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (NoSuchPaddingException e) {
            return null;
        } catch (InvalidKeyException e) {
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            return null;
        } catch (IllegalBlockSizeException e) {
            return null;
        } catch (BadPaddingException e) {
            return null;
        }
    }

    /**
     * 返回经过base64解码和AES解密后的数据
     */
    public static final String base6AesDecode(String data, String key) {
        if (null == data || data.length() == 0) {
            return null;
        }
        // byte[] raw = decodeBase64Bytes(key);
        byte[] raw = key.getBytes();
        if (raw == null || raw.length != 16) {
            return null;
        }
        SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec("0102030405060708".getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] encryptedByte = decodeBase64Bytes(data);
            if (null == encryptedByte) {
                return null;
            }
            byte[] decryptedByte = cipher.doFinal(encryptedByte);
            return new String(decryptedByte);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (NoSuchPaddingException e) {
            return null;
        } catch (InvalidKeyException e) {
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            return null;
        } catch (IllegalBlockSizeException e) {
            return null;
        } catch (BadPaddingException e) {
            return null;
        }
    }

}
