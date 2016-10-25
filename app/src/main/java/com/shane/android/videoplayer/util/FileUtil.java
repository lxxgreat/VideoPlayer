package com.shane.android.videoplayer.util;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 16-10-25
 */

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    public static byte[] getFileBytes(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        if (!file.exists() || file.length() <= 0) {
            return null;
        }

        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            is = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                if (baos == null) {
                    baos = new ByteArrayOutputStream();
                }
                baos.write(buffer, 0, len);
            }
            if (baos != null) {
                return baos.toByteArray();
            } else {
                return null;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "getFileBytes", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, "getFileBytes", e);
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, "getFileBytes", e);
                }
            }
        }
        return null;
    }

    public static boolean saveFileString(String path, byte[] data) {
        if (TextUtils.isEmpty(path)) return false;
        if (data == null || data.length == 0) return false;

        File file = new File(path);
        if (!file.exists()) {
            createFile(path);
        }

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data);
            return true;
        } catch (IOException e) {
            LogUtil.e(TAG, "saveFileString", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, "saveFileString", e);
                }
            }
        }

        return false;
    }

    public static File createFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;

        File file = new File(filePath);
        if (file.exists()) {
            return file;
        }

        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                return parent;
            }
        }

        try {
            if (file.createNewFile()) {
                return file;
            }
        } catch (IOException e) {
            LogUtil.e(TAG, "createFile", e);
        }
        return null;
    }

    public static boolean fileExists(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;

        File file = new File(filePath);
        return file.exists();

    }
}
