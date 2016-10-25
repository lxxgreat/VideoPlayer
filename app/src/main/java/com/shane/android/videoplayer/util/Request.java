package com.shane.android.videoplayer.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Request {
    private static final String TAG = "Request";

    private static final int DEFAULT_HTTP_REQUEST_READ_TIMEOUT_MS_WIFI = 10 * 1000;
    private static final int DEFAULT_HTTP_REQUEST_READ_TIMEOUT_MS_MOBILE = 30 * 1000;
    private static final int DEFAULT_HTTP_REQUEST_CONNECT_TIMEOUT_MS = 10 * 1000;

    // Secret key
    private static final String APP_KEY = "yellowpage";
    private static final String APP_SECRET = "77eb2e8a5755abd016c0d69ba74b219c";
    private static final String DECODE_KEY = "d101b17c77ff93cs";

    private HashMap<String, String> mParamsMap;
    private Map<String, String> mHeadersMap;
    protected String mRequestUrl;
    protected boolean mDecryptDownloadData;
    protected boolean mRequireLogin;

    protected int mConnectTimeout;
    protected int mReadTimeout;

    protected Context mContext;
    protected String mRequestMethod;
    protected int mNetworkAccess = NETWORK_ACCESS_DEFAULT;

    public static final int STATUS_OK = 0;
    public static final int STATUS_NETWORK_UNAVAILABLE = 1;
    public static final int STATUS_SERVICE_UNAVAILABLE = 2;
    public static final int STATUS_CLIENT_ERROR = 3;
    public static final int STATUS_SERVER_ERROR = 4;
    public static final int STATUS_UNKNOWN_ERROR = 5;
    public static final int STATUS_NETWORK_ACCESS_DENIED = 6;
    public static final int STATUS_NOT_MODIFIED = 7;
    public static final int STATUS_UNKNOWN_HOST_ERROR = 8;

    //Network access status
    public static final int NETWORK_ACCESS_DEFAULT = 0;
    public static final int NETWORK_ACCESS_ALLOWED = 1;
    public static final int NETWORK_ACCESS_DENIED = -1;

    public Request(Context context, String url) {
        mContext = context;
        mRequestMethod = HttpGet.METHOD_NAME;
        mRequestUrl = url;
        mDecryptDownloadData = true;
        mRequireLogin = false;
    }

    public Request setDecryptDownloadData(boolean decrypt) {
        mDecryptDownloadData = decrypt;
        return this;
    }

    public Request setRequireLogin(boolean require) {
        mRequireLogin = require;
        return this;
    }

    public void setReadTimeout(int t) {
        mReadTimeout = t;
    }

    public void setConnectTimeout(int t) {
        mConnectTimeout = t;
    }

    public int getReadTimeout() {
        return mReadTimeout;
    }

    public int getConnectTimeout() {
        return mConnectTimeout;
    }

    public void setEtag(String etag) {
        addRequestProperty("If-None-Match", etag);
    }

    private String getLoginCookies() {
        return null;
    }

    private String getCookies() {
        return null;
    }

    protected HttpURLConnection getConn() {
        final String url = getRequestUrl();

        if (TextUtils.isEmpty(url)) {
            return null;
        }

        HttpURLConnection conn = null;
        try {
            final URL req = new URL(url);
            conn = (HttpURLConnection) req.openConnection();

            if (mReadTimeout > 0) {
                conn.setReadTimeout(mReadTimeout);
            } else if (NetUtil.isWifiConnected(mContext)) {
                conn.setReadTimeout(DEFAULT_HTTP_REQUEST_READ_TIMEOUT_MS_WIFI);
            } else {
                conn.setReadTimeout(DEFAULT_HTTP_REQUEST_READ_TIMEOUT_MS_MOBILE);
            }

            conn.setConnectTimeout(mConnectTimeout > 0 ? mConnectTimeout
                    : DEFAULT_HTTP_REQUEST_CONNECT_TIMEOUT_MS);
            conn.setRequestMethod(mRequestMethod);

            if (TextUtils.equals(mRequestMethod, HttpPost.METHOD_NAME)) {
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            String cookie = getCookies();
            if (!TextUtils.isEmpty(cookie)) {
                conn.setRequestProperty("Cookie", cookie);
            }
            conn.setRequestProperty("User-Agent", DeviceUtil.getUserAgent(mContext));
            addHeaders(conn);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Failed to get connection! ", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to get connection! ", e);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get connection! ", e);
        }
        return conn;
    }

    private void addHeaders(HttpURLConnection conn) {
        if (mHeadersMap == null || mHeadersMap.size() == 0) {
            return;
        }
        Iterator<Entry<String ,String>> iterator = mHeadersMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            conn.addRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    // The constructed url to requested
    protected String getRequestUrl() {
        if (TextUtils.equals(mRequestMethod, HttpPost.METHOD_NAME)) {
            return mRequestUrl;
        }
        final String params = getParams();
        return TextUtils.isEmpty(params) ? mRequestUrl : String.format("%s?%s", mRequestUrl,
                params);
    }

    protected String getParams() {
        HashMap<String, String> paramsMap;
        if (mParamsMap == null) {
            paramsMap = new HashMap<String, String>();
        } else {
            paramsMap = mParamsMap;
        }
        return signUrlParams(mContext, paramsMap);
    }

    protected boolean isServerError(int statusCode) {
        return statusCode == HttpStatus.SC_BAD_REQUEST
                || statusCode == HttpStatus.SC_UNAUTHORIZED
                || statusCode == HttpStatus.SC_FORBIDDEN
                || statusCode == HttpStatus.SC_NOT_ACCEPTABLE
                || statusCode / 100 == 5;
    }

    public Request setHttpMethod(String method) {
        mRequestMethod = method;
        return this;
    }

    public void addRequestProperty(String key, String value) {
        if (mHeadersMap == null) {
            mHeadersMap = new HashMap<String, String>();
        }

        mHeadersMap.put(key, value);
    }

    private Map<String, String> getRequestProperties() {
        return Collections.unmodifiableMap(mHeadersMap);
    }

    public void addParam(String key, String value) {
        if (mParamsMap == null) {
            mParamsMap = new HashMap<String, String>();
        }

        if (!mParamsMap.containsKey(key)) {
            mParamsMap.put(key, value);
        }
    }

    public void clearParams() {
        if (mParamsMap != null) {
            mParamsMap.clear();
        }
    }

    /**
     * If request for antispam or fraud, need overwrite network access.
     *
     * @param networkAccess should be {@link #NETWORK_ACCESS_DEFAULT}, {@link
     *                      #NETWORK_ACCESS_DEFAULT}, {@link #NETWORK_ACCESS_DEFAULT}
     */
    public void overwriteNetworkAccess(int networkAccess) {
        mNetworkAccess = networkAccess;
    }

    public static String signUrlParams(Context context, Map<String, String> paramsMap) {
        return getSignedUri(getEncryptedParam(paramsMap), APP_KEY, APP_SECRET);
    }

    private static HashMap<String, String> getEncryptedParam(Map<String, String> paramsMap) {
        HashMap<String, String> map = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : paramsMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(
                    URLEncoder.encode(TextUtils.isEmpty(entry.getValue()) ? "" : entry.getValue()));
        }
        map.put("_encparam", encryptData(sb.toString()));
        return map;
    }

    private static String getSignedUri(Map<String, String> paramsMap, final String appKey, final String secret) {
        String sign = genUrlSign(paramsMap, appKey, secret);
        if (sign.length() == 0)
            return sign;
        // 使用签名生成访问URL
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("appkey=").append(appKey).append("&sign=").append(sign);
        for (Entry<String, String> entry : paramsMap.entrySet()) {
            stringBuilder.append('&').append(entry.getKey()).append('=').append(
                    URLEncoder.encode(entry.getValue()));
        }
        // String requestUrl = apiUrl + "?" + stringBuilder.toString();
        return stringBuilder.toString();
    }

    private static String genUrlSign(Map<String, String> paramMap, final String appkey, final String secret) {
        if (paramMap.isEmpty())
            return "";
        String[] keyArray = paramMap.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);

        // 拼接有序的参数名-值串
        // 格式： appkey+{key+value}n+secret
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appkey);
        for (String key : keyArray) {
            stringBuilder.append(key).append(paramMap.get(key));
        }
        stringBuilder.append(secret);
        String codes = stringBuilder.toString();
        // SHA-1编码， 这里使用的是Apache
        // codec，即可获得签名(shaHex()会首先将中文转换为UTF8编码然后进行sha1计算，使用其他的工具包请注意UTF8编码转换)
        return CoderUtil.encodeSHA(codes).toUpperCase(Locale.US);
    }

    public static String encryptData(String data) {
       return CoderUtil.base64AesEncode(data, DECODE_KEY);
    }

    public static String decryptData(String data) {
        return CoderUtil.base6AesDecode(data, DECODE_KEY);
    }
}
