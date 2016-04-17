package com.lqy.abook.tool;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String toString(Object o) {
		if (o != null)
			return o.toString();
		return CONSTANT.EMPTY;
	}

	public static String formatDate(long time, int end) {
		if (time != CONSTANT._1)
			try {
				Date d = new Date();
				d.setTime(time);
				return sdf.format(d).substring(0, end);
			} catch (Exception e) {
			}
		return CONSTANT.EMPTY;
	}
	public static int toInt(Object o) {
		int result = 0;
		try {
			if (o != null) {
				String str = o.toString().trim();
				if (str.length() != 0) {
					result = Integer.parseInt(str);
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static int toIntOr_1(Object o) {
		int result = 1;
		try {
			if (o != null) {
				String str = o.toString().trim();
				if (str.length() != 0) {
					result = Integer.parseInt(str);
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static long toLongOr_1(Object o) {
		long result = -1;
		try {
			if (o != null) {
				String str = o.toString().trim();
				if (str.length() != 0) {
					result = Long.parseLong(str);
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isJsEmpty(String s) {
		return s == null || s.length() == 0 || "undefined".equals(s);
	}


}
