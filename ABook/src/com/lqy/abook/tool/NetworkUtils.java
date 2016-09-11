package com.lqy.abook.tool;

import com.lqy.abook.MyApp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络相关
 */
public class NetworkUtils {
	private static boolean _isNetConnected = false;// 网络是否正常
	private static Object _isNetInit = null;// 网络是否初始化

	public static boolean isNetConnected(Context context) {
		if (context != null)
			refreshNetState(context);
		else if (_isNetInit == null)
			refreshNetState(MyApp.getInstance());
		return _isNetConnected;
	}

	public static boolean isNetConnectedRefreshWhereNot() {
		if (!_isNetConnected) {
			refreshNetState(MyApp.getInstance());
		}
		return _isNetConnected;
	}

	@SuppressWarnings("deprecation")
	public static boolean refreshNetState(Context context) {
		MyLog.i("refreshNetState");
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivityManager != null) {
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				if (info == null || info.getState() != NetworkInfo.State.CONNECTED || !connectivityManager.getBackgroundDataSetting())
					_isNetConnected = false;
				else {
					_isNetConnected = true;
				}
			}
			_isNetInit = CONSTANT.EMPTY;
		} catch (Exception e) {
			MyLog.i(e.getMessage());
		}
		return _isNetConnected;
	}

}
