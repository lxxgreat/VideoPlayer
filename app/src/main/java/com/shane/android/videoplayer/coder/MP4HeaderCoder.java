package com.shane.android.videoplayer.coder;

import android.os.SystemClock;

import com.shane.android.videoplayer.interf.IFileCoder;
import com.shane.android.videoplayer.util.CoderUtil;
import com.shane.android.videoplayer.util.FileUtil;
import com.shane.android.videoplayer.util.HexUtil;
import com.shane.android.videoplayer.util.LogUtil;

import java.io.ByteArrayOutputStream;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 16-10-25
 */

public class MP4HeaderCoder implements IFileCoder {
    private static final String TAG = MP4HeaderCoder.class.getSimpleName();

    private static final String DECODE_KEY = "d101b17c77ff93cs";
    private static final int ENCODER_BYTES = 16; // AES128
    int mDecodeBytes = 44; // AES128

    String mSouceFile;
    byte[] mRawBytes;

    public MP4HeaderCoder(String src) {
        mSouceFile = src;
        mRawBytes = FileUtil.getFileBytes(src);
    }

    @Override
    public byte[] getEncodeBytes(byte[] data) {
        if (data == null) return null;
        if (data.length < ENCODER_BYTES) return data;
        byte[] ret = null;
        long start = SystemClock.elapsedRealtime();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(data, 0, ENCODER_BYTES);
        byte[] temp = baos.toByteArray();
        LogUtil.d(TAG, "HexString========:" + HexUtil.toHexString(temp));
        byte[] res = CoderUtil.base64AesEncode(temp, DECODE_KEY);
        LogUtil.d(TAG, "HexString2========:" + HexUtil.toHexString(res));
        mDecodeBytes = res.length;
        LogUtil.i(TAG, "mDecodeBytes========:" + mDecodeBytes);
        baos = null;
        baos = new ByteArrayOutputStream();
        int remain = data.length-ENCODER_BYTES;
        try {
            baos.write(res);
            baos.write(data, ENCODER_BYTES, remain);
            ret = baos.toByteArray();
        } catch (Exception e) {
            LogUtil.e(TAG, "getEncodeBytes", e);
        }

        long duration = SystemClock.elapsedRealtime() - start;
        LogUtil.d(TAG, "getEncodeBytes=====duration: " + duration + "ms");
        return ret;
    }

    @Override
    public byte[] getDecodeBytes(byte[] data) {
        if (data == null) return null;
        if (data.length < mDecodeBytes) return data;
        byte[] ret = null;
        long start = SystemClock.elapsedRealtime();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(data, 0, mDecodeBytes);
        byte[] temp = CoderUtil.base64AesDecode(baos.toByteArray(), DECODE_KEY);
        String decoder = HexUtil.toHexString(temp);
        LogUtil.d(TAG, "decoder========:" + decoder);
        baos = null;
        baos = new ByteArrayOutputStream();
        int remain = data.length - mDecodeBytes;
        try {
            baos.write(temp);
            baos.write(data, mDecodeBytes, remain);
            ret = baos.toByteArray();
        } catch (Exception e) {
            LogUtil.e(TAG, "getDecodeBytes", e);
        }

        long duration = SystemClock.elapsedRealtime() - start;
        LogUtil.d(TAG, "getDecodeBytes=====duration: " + duration + "ms");
        return ret;
    }

    public void setSouceFile(String src) {
        mSouceFile = src;
    }

    public void setRawBytes(byte[] raw) {
        mRawBytes = raw;
    }

    public byte[] getRawBytes() {
        return mRawBytes;
    }

    public String getSouceFile() {
        return mSouceFile;
    }

    public void test() {
        byte[] ret = getEncodeBytes(mRawBytes);
        getDecodeBytes(ret);
    }
}
