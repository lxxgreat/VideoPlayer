package com.shane.android.videoplayer.util;

import android.util.Log;

/**
 * @author shane（https://github.com/lxxgreat）
 * @version 1.0
 * @created 2016-10-23
 */
public class LogUtil {

	private static final String TAG = "VideoPlayer";

	private static final int LOG_LEVEL = 6;

	private static final int VERBOSE = 5;
	private static final int DEBUG = 4;
	private static final int INFO = 3;
	private static final int WARN = 2;
	private static final int ERROR = 1;
	private static final int WTF = 0;

	private LogUtil() { /* empty */ }

	public static void v(String tag, String msg) {
		if (LOG_LEVEL > VERBOSE) {
			Log.v(TAG, tag + ":" + msg);
		}
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (LOG_LEVEL > VERBOSE) {
			Log.v(TAG, tag + ":" + msg, tr);
		}
	}

	public static void d(String tag, String msg) {
		if (LOG_LEVEL > DEBUG) {
			Log.d(TAG, tag + ":" + msg);
		}
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (LOG_LEVEL > DEBUG) {
			Log.d(TAG, tag + ":" + msg, tr);
		}
	}

	public static void i(String tag, String msg) {
		if (LOG_LEVEL > INFO) {
			Log.i(TAG, tag + ":" + msg);
		}
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (LOG_LEVEL > INFO) {
			Log.i(TAG, tag + ":" + msg, tr);
		}
	}

	public static void w(String tag, String msg) {
		if (LOG_LEVEL > WARN) {
			Log.w(TAG, tag + ":" + msg);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (LOG_LEVEL > WARN) {
			Log.w(TAG, tag + ":" + msg, tr);
		}
	}

	public static void e(String tag, String msg) {
		if (LOG_LEVEL > ERROR) {
			Log.e(TAG, tag + ":" + msg);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (LOG_LEVEL > ERROR) {
			Log.e(TAG, tag + ":" + msg, tr);
		}
	}

	public static void wtf(String tag, String msg) {
		if (LOG_LEVEL > WTF) {
			Log.wtf(TAG, tag + ":" + msg);
		}
	}

	public static void wtf(String tag, String msg, Throwable tr) {
		if (LOG_LEVEL > WTF) {
			Log.wtf(TAG, tag + ":" + msg, tr);
		}
	}
}
