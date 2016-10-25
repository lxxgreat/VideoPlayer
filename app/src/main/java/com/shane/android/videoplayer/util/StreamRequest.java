package com.shane.android.videoplayer.util;

import android.content.Context;
import android.util.Log;


import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Request for Stream data, using YellowPage Services network access If request
 * is not for YellowPage Services, call {@link Request#overwriteNetworkAccess}
 */
public class StreamRequest extends Request {
    private static final String TAG = "StreamRequest";

    private static final int BUFFER_SIZE = 1024;
    private static final String TEMP_DOWNLOADED_FILE_NAME_FORMAT = "%s_temp_downloaded";
    private static final String CONN_RANGE_VALUE_FORMAT = "bytes=%d-";
    private static final String CONN_RANGE_PROPERTY = "RANGE";

    public StreamRequest(Context context, String url) {
        super(context, url);
    }

    @Override
    protected String getRequestUrl() {
        return mRequestUrl;
    }

    public int requestStream(OutputStream outPutStream) {
        return requestStream(outPutStream, null);
    }

    public int requestStream(OutputStream outPutStream, Map<String, String> headerFields) {
        if (outPutStream == null) {
            Log.e(TAG, "requestStream: the outPutStream should no be null");
            return STATUS_CLIENT_ERROR;
        }

        if (!NetUtil.isNetConnected(mContext)) {
            Log.d(TAG, "requestStream: the net work was not connected");
            return STATUS_NETWORK_UNAVAILABLE;
        }

        int status = STATUS_CLIENT_ERROR;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;

        try {
            conn = super.getConn();
            if (conn != null) {
                if (headerFields != null) {
                    for (Map.Entry<String, String> entry : headerFields.entrySet()) {
                        String value = conn.getHeaderField(entry.getKey());
                        headerFields.put(entry.getKey(), value);
                    }
                }

                baos = getTempDownloadData();
                // 如果该文件下载过，那么进行断点下载，从已下载文件的末尾继续下载
                if (baos != null) {
                    conn.addRequestProperty(CONN_RANGE_PROPERTY,
                            String.format(CONN_RANGE_VALUE_FORMAT, baos.size()));
                } else {
                    baos = new ByteArrayOutputStream();
                }
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "requestStream:The response code is " + responseCode);
                if (responseCode == HttpStatus.SC_OK
                        || responseCode == HttpStatus.SC_PARTIAL_CONTENT) {
                    inputStream = conn.getInputStream();

                    byte[] buff = new byte[BUFFER_SIZE];
                    int len = 0;
                    while ((len = inputStream.read(buff)) != -1) {
                        baos.write(buff, 0, len);
                    }
                    outPutStream.write(baos.toByteArray());
                    status = STATUS_OK;
                } else if (isServerError(responseCode)) {
                    status = STATUS_SERVER_ERROR;
                } else {
                    status = STATUS_UNKNOWN_ERROR;
                }
            }
        } catch (SocketTimeoutException e) {
            status = STATUS_SERVICE_UNAVAILABLE;
            Log.e(TAG, "Request failed! ", e);
        } catch (UnknownHostException e) {
            status = STATUS_UNKNOWN_HOST_ERROR;
            Log.e(TAG, "Request failed! ", e);
        } catch (IOException e) {
            if (!NetUtil.isNetConnected(mContext)) {
                status = STATUS_NETWORK_UNAVAILABLE;
            }
            Log.e(TAG, "Request failed! ", e);
        } catch (Exception e) {
            if (!NetUtil.isNetConnected(mContext)) {
                status = STATUS_NETWORK_UNAVAILABLE;
            } else {
                status = STATUS_UNKNOWN_ERROR;
            }
            Log.e(TAG, "Request failed! ", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close input stream! ", e);
                }
            }

            if (status != STATUS_OK && baos != null && baos.size() > 0) {
                saveTemporaryDownloadedData(baos);
            }

            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close byte array output stream! ", e);
                }
            }
        }
        return status;
    }

    /**
     * 将已经下载的文件存储到Cache中供再次下载使用。文件名以File的SHA1命名。
     */
    private void saveTemporaryDownloadedData(ByteArrayOutputStream out) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(mContext.getCacheDir()
                    + File.separator
                    + String.format(TEMP_DOWNLOADED_FILE_NAME_FORMAT,
                    HashUtil.getSHA1(getRequestUrl())));
            outputStream.write(out.toByteArray());
            outputStream.flush();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to save data! ", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to save data! ", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close output stream! ", e);
                }
            }
        }
    }

    /**
     * 检查Cache中是否存在待下载文件的临时文件，该文件可能是某次下载已完成的部分，以文件的SHA1命名。
     * 这个文件用来支持断点续传。为了减少Cache的占用，每次读取后删除该文件。
     */
    private ByteArrayOutputStream getTempDownloadData() {
        File file = new File(mContext.getCacheDir()
                + File.separator
                + String.format(TEMP_DOWNLOADED_FILE_NAME_FORMAT,
                HashUtil.getSHA1(getRequestUrl())));

        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = null;
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
                outputStream = new ByteArrayOutputStream();
                byte[] buff = new byte[BUFFER_SIZE];
                int len = 0;
                while ((len = inputStream.read(buff)) != -1) {
                    outputStream.write(buff, 0, len);
                }
                outputStream.flush();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Failed to get temp downloaded data! ", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to get temp downloaded data! ", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close input stream! ", e);
                    }
                }
                // 删除临时文件
                if (!file.delete()) {
                    Log.e(TAG, "Failed to delete tmp download data!");
                }
            }
        }
        return outputStream;
    }
}
