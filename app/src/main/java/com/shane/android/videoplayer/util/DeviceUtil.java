package com.shane.android.videoplayer.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


public class DeviceUtil {
    private static final String TAG = DeviceUtil.class.getSimpleName();
    // Model
    private static final String KEY_MOD = "mod";
    // Build Version
    private static final String KEY_BVS = "bvs";
    // MNC
    private static final String KEY_MNC = "mnc";
    // MCC
    private static final String KEY_MCC = "mcc";
    // PowerKeeper Version
    private static final String KEY_PVS = "pvs";
    // world split
    private static final String WORD_SPLIT = "-";
    // group split
    private static final String GROUP_SPLIT = "__";

    private String sType = null;

    public static class Holder {
        private final static DeviceUtil sInstance = new DeviceUtil();
    }

    public static DeviceUtil getInstance() {
        return Holder.sInstance;
    }

    public String getType(final Context context) {
        if (sType == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(KEY_MOD+WORD_SPLIT+getModel()+GROUP_SPLIT);
            sb.append(KEY_BVS+WORD_SPLIT+getVersion()+GROUP_SPLIT);
            sb.append(KEY_MNC+WORD_SPLIT+getMNC()+GROUP_SPLIT);
            sb.append(KEY_MCC+WORD_SPLIT+getMCC()+GROUP_SPLIT);
            sb.append(KEY_PVS+WORD_SPLIT+getPowerKeeperVersion(context));
            sType = sb.toString();
        }

        Log.i(TAG, "type:" + sType);
        return sType;
    }
    /**
     * @return country code, e.g. China is 86.
     * */
    public static String getMCC() {
//        String ret = CountryCode.getNetworkCountryCode();
        String ret = "TODO";
        if (TextUtils.isEmpty(ret)) {
            ret = "-1";
        }
        return ret;
    }

    public static String getMNC() {
//        String ret = CountryCode.getIddCode();
        String ret = "TODO";
        if (TextUtils.isEmpty(ret)) {
            ret = "-1";
        }
        return ret;
    }

    public static String getIMEI() {
        String imei = "TODO:";
        if (TextUtils.isEmpty(imei)) {
            return "";
        }
        return imei;
    }

    public static String getUTCTimeStr() {
        return String.valueOf(System.currentTimeMillis()/1000);
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getVersion() {
        return Build.VERSION.INCREMENTAL;
    }

    public static String getPowerKeeperVersion(Context context) {
        String versionCode = "0000";
        Context appContext = context.getApplicationContext();
        try {
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            versionCode = String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getPowerKeeperVersion", e);
        } catch (Exception e) {
            Log.e(TAG, "getPowerKeeperVersion", e);
        }

        return versionCode;
    }

    public static String getUserAgent(Context context) {
        return "com.shane.android.videoplayer";
    }
}
