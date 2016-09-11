package com.lqy.abook.tool;

import android.util.Log;

import com.google.gson.Gson;
import com.lqy.abook.load.FileUtil;

public class MyLog {

	public static final String TAG_BOOK = "zztx";
	public static final String TAG_BASE = "BaseActivity";
	public static final String TAG_WEB = "web";

	public static final String SEPARATOR = "----------------------------------";

	public static boolean isShowLog() {
		return true;// CONSTANT.isDebug;// || CONSTANT.CurrentRunTime ==
					// CONSTANT.RunTimeType.LOCATION;
	}

	public static void i(Object msg) {
		if (isShowLog() && msg != null) {
			Log.i(TAG_BOOK, new Gson().toJson(msg));
		}
	}

	public static void i(String title, Object msg) {
		if (isShowLog() && msg != null) {
			Log.i(TAG_BOOK, title + "  " + new Gson().toJson(msg));
		}
	}

	public static void i(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			Log.i(TAG_BOOK, msg);
		}
	}

	public static void save(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			FileUtil.write(msg, FileUtil.getErrorPath(), "MyLog.txt");
		}
	}

	public static void web(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			Log.i(TAG_WEB, msg);
		}
	}

	public static void i_base(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			Log.i(TAG_BASE, msg);
		}
	}

	public static void e(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			Log.e(TAG_BOOK, msg);
		}
	}

	public static void e(Exception e) {
		if (isShowLog() && e != null) {
			Log.e(TAG_BOOK, e.toString() + "  " + e.getMessage());
		}
	}

}
