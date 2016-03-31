package com.lqy.abook.tool;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Base64;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.lqy.abook.MyApp;
import com.lqy.abook.widget.MyAlertDialog;

public class Util {

	public static void notCompleted(Context c) {
		dialog(c, "该功能未实现，请等待下个版本");
	}

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String toString(Object o) {
		if (o != null)
			return o.toString();
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

	private static final Object _o = new Object();

	public static void dialog(Context c, String msg, DialogInterface.OnClickListener positivelistener) {
		try {
			new MyAlertDialog(c).setTitle("系统提示").setMessage(msg).setPositiveButton("确定", positivelistener).setNegativeButton("取消", null).show();
		} catch (Exception e) {
			MyLog.e(e);
		}
	}

	public static void dialog(Context c, String msg) {
		try {
			new MyAlertDialog(c).setTitle("系统提示").setMessage(msg).setPositiveButton("确定", null).show();
		} catch (Exception e) {
			MyLog.e(e);
		}
	}

	public static void toast(Context c, String msg) {
		try {
			Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			MyLog.e(e);
		}
	}

	public static void toast(Context c, int msgResId) {
		try {
			Toast.makeText(c, c.getString(msgResId), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			MyLog.e(e);
		}
	}

	public static String base64Encode(String s) {
		try {
			return base64Encode(s.getBytes("utf-8"));
		} catch (Exception e) {
			return CONSTANT.EMPTY;
		} catch (OutOfMemoryError e) {
			try {
				Thread.sleep(1000);
				System.gc();
				return base64Encode(s.getBytes("utf-8"));
			} catch (Exception e1) {
				return CONSTANT.EMPTY;
			} catch (OutOfMemoryError e1) {
				return CONSTANT.EMPTY;
			}
		}
	}

	public static String base64Encode(byte[] b) {
		if (b == null)
			return CONSTANT.EMPTY;
		try {
			return Base64.encodeToString(b, 2);
		} catch (Exception e) {
			return CONSTANT.EMPTY;
		}
	}

	public static String base64Decode(String s) {

		if (s == null)
			return CONSTANT.EMPTY;

		try {
			return new String(Base64.decode(s, 2));
		} catch (Exception ex) {
			return CONSTANT.EMPTY;
		}

	}

	/**
	 * 关闭键盘
	 */
	public static void hideKeyboard(Activity activity, EditText v) {
		try {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
				// activity.getCurrentFocus().getWindowToken()
				imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		} catch (Exception e) {
		}
	}

	public static String getString(int id) {
		try {
			return MyApp.getInstance().getString(id);
		} catch (Exception e) {
			return CONSTANT.EMPTY;
		}
	}

	/**
	 * 验证网址
	 */
	public static boolean matchWebSite(String url) {
		String reg = "(http(s)?://)?(www\\.)?[\\w]+\\.\\w{2,4}(/)?";
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(url);
		return m.find();
	}
}
