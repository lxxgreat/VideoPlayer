package com.shane.android.videoplayer.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static Map<String, String[]> sDirectoryList = new HashMap<String, String[]>();


    public static String getFileString(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        return getFileString(new File(path));
    }

    public static String getFileString(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileUtil", e);
        } catch (IOException e) {
            Log.e(TAG, "FileUtil", e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }
        }
        return null;
    }

    public static String getRawFileString(Context context, int res) {
        InputStream is = context.getResources().openRawResource(res);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        try {
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "FileUtil", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "FileUtil", e);
            }
        }
        return null;
    }

    public static boolean saveFileString(String path, String data) {
        File file = new File(path);
        if (!file.exists()) {
            createFile(path);
        }

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data.getBytes());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "FileUtil", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }
        }

        return false;
    }

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
            Log.e(TAG, "FileUtil", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }
        }
        return null;
    }

    public static boolean isAssetDirectory(AssetManager assetManager, String path) {
        try {
            return getAssetList(assetManager, path).length > 0;
        } catch (IOException e) {
            Log.e(TAG, "FileUtil", e);
        }
        return false;
    }

    public static void copyAssetFile(
            AssetManager assetManager, String assetFilePath, String destFilePath) {
        byte[] buffer = new byte[1024];
        InputStream is = null;
        OutputStream os = null;
        try {
            is = assetManager.open(assetFilePath);
            File file = new File(destFilePath);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return;
                }
            }
            os = new FileOutputStream(file);
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            Log.e(TAG, "FileUtil", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }
        }
    }

    public static void copyWebAssetDirectory(
            AssetManager assetManager, String assetDirectory, String desDirectory) {
        try {
            String[] fileNames = getAssetList(assetManager, assetDirectory);
            for (String fileName : fileNames) {
                File file = new File(desDirectory);
                if (!file.exists()) {
                    if (!file.mkdir()) {
                        return;
                    }
                }
                String assetFilePath = assetDirectory + "/" + fileName;
                String destFilePath = desDirectory + "/" + fileName;
                if (isAssetDirectory(assetManager, assetFilePath)) { // 如果是目录
                    file = new File(destFilePath);
                    if (!file.exists()) {
                        if (!file.mkdir()) {
                            return;
                        }
                    }
                    copyWebAssetDirectory(assetManager, assetFilePath, destFilePath);
                } else { // 如果是文件
                    copyAssetFile(assetManager, assetFilePath, destFilePath);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "FileUtil", e);
        }
    }

    private static String[] getAssetList(AssetManager assetManager, String assetDirectory)
            throws IOException {
        String[] fileList = sDirectoryList.get(assetDirectory);
        if (fileList == null) {
            fileList = assetManager.list(assetDirectory);
            if (fileList != null) {
                sDirectoryList.put(assetDirectory, fileList);
            }
        }
        return fileList;
    }

    /**
     * downloadFile with specified network access and headers.
     *
     * @param networkAccess Set extra network access for request.{@link
     *                      Request#overwriteNetworkAccess}
     */
    public static boolean downLoadFileWithHeader(Context context, String url, String destPath, Map<String, String> headerFields, int networkAccess) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(destPath)) {
            Log.d(TAG, "The url or dest path should be null");
            return false;
        }

        boolean downloaded = false;
        StreamRequest request = new StreamRequest(context, url);
        request.overwriteNetworkAccess(networkAccess);
        FileOutputStream fos = null;
        try {
            if (createFile(destPath) == null) {
                return false;
            }
            fos = new FileOutputStream(new File(destPath));
            int statusCode = request.requestStream(fos, headerFields);
            if (statusCode == Request.STATUS_OK) {
                downloaded = true;
            } else {
            }
        } catch (IOException e) {
            Log.e(TAG, "FileUtil", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }
        }
        return downloaded;
    }

    /**
     * downloadFile with YellowPage Services' network access and headers.
     *
     * @see
     */
    public static boolean downLoadFileWithHeader(Context context, String url, String destPath, Map<String, String> headerFields) {
        return downLoadFileWithHeader(context, url, destPath, headerFields, Request.NETWORK_ACCESS_DEFAULT);
    }
    /**
     * downloadFile with specified network access.
     *
     * @param networkAccess Set extra network access for request.{@link
     *                      Request#overwriteNetworkAccess}
     */
    public static boolean downLoadFile(Context context, String url, String destPath, int networkAccess) {
        return downLoadFileWithHeader(context, url, destPath, null, networkAccess);
    }

    /**
     * downloadFile with YellowPage Services' network access.
     *
     * @see
     */
    public static boolean downLoadFile(Context context, String url, String destPath) {
        return downLoadFileWithHeader(context, url, destPath, null, Request.NETWORK_ACCESS_DEFAULT);
    }

    public static boolean copyFile(File srcFile, File destFile) {
        if (createFile(destFile.toString()) != null) {
            return doCopyFile(srcFile, destFile);
        }
        return false;
    }

    /**
     * Copy a file from srcFile to destFile.
     * 
     * @return true if succeed, false if fail
     */
    public static boolean doCopyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    /**
     * Copy data from a source stream to {@code destFile}.
     * 
     * @return true if succeed, false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        if (destFile.exists()) {
            if (!destFile.delete()) {
                return false;
            }
        }
        boolean ret = false;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
            ret = true;
        } catch (Exception e) {
            Log.e(TAG, "copyToFile", e);
        } finally {
            if (out !=null) {
                try {
                    out.flush();
                    out.getFD().sync();
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "FileUtil", e);
                }
            }
        }

        return ret;
    }

    public static boolean copyFile(String srcFilePath, String destFilePath) {
        File srcFile = new File(srcFilePath);
        File destFile = new File(destFilePath);
        return copyFile(srcFile, destFile);
    }

    public static boolean deleteFiles(File... files) {
        boolean succeeded = true;
        for (File file : files) {
            if (!deleteFile(file)) {
                succeeded = false;
                break;
            }
        }
        return succeeded;
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }

        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File toBeDeletedFile : files) {
                    boolean deleted = deleteFile(toBeDeletedFile);
                    if (!deleted) {
                        return false;
                    }
                }
                return file.delete();
            } else {
                return file.delete();
            }
        }
        return false;
    }

    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        return deleteFile(new File(path));
    }

    public static File createFile(String filePath) {
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
            LogUtil.e(TAG, "FileUtil", e);
        }
        return null;
    }

    public static String getFileSha1(String filePath) {
        byte[] bytes = getFileBytes(filePath);
        if (bytes != null) {
            return getDataSha1Digest(bytes);
        }
        return null;
    }

    public static boolean isLocalUri(String rawUrl) {
        if (TextUtils.isEmpty(rawUrl)) {
            return false;
        }

        String url = rawUrl.toLowerCase();
        if (url.startsWith("content://")) {
            return true;
        }
        if (url.startsWith("file://")) {
            return true;
        }
        if (url.startsWith("data")) {
            return true;
        }
        if (url.startsWith("about:blank")) {
            return true;
        }

        return false;
    }

    public static String getDataSha1Digest(final byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(data);
            return MD5Util.getHexString(md.digest());
        } catch (Exception e) {
            Log.e(TAG, "FileUtil", e);
        }
        return null;
    }
}
