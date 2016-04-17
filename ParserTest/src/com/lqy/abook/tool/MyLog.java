package com.lqy.abook.tool;


public class MyLog {

	public static final String TAG_BOOK = "zztx";
	public static final String TAG_BASE = "BaseActivity";

	public static final String SEPARATOR = "----------------------------------";

	public static boolean isShowLog() {
		return true;// CONSTANT.isDebug;// || CONSTANT.CurrentRunTime ==
					// CONSTANT.RunTimeType.LOCATION;
	}

	public static void i(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			System.out.println(msg);
		}
	}

	public static void i_base(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			System.out.println( msg);
		}
	}

	public static void e(String msg) {
		if (isShowLog() && !Util.isEmpty(msg)) {
			System.out.println( msg);
		}
	}

	public static void e(Exception e) {
		if (isShowLog() && e != null) {
			System.out.println( e.toString() + "  " + e.getMessage());
		}
	}

}
