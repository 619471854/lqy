package com.lqy.abook;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;

import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.CallBackListener;
import com.lqy.abook.tool.CrashHandler;
import com.lqy.abook.tool.MyLog;

public class MyApp extends Application {

	private static MyApp instance;

	public static MyApp getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		instance = this;
		// 是否是debug模式
		CONSTANT.isDebug = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		MyLog.i_base("MyApplication onCreate");
		// 崩溃处理
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		// 全局设置
		globalConfig();

		super.onCreate();
	}

	/**
	 * 全局设置
	 */
	private void globalConfig() {
		// 版本号
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			CONSTANT.versionName = info.versionName;
		} catch (Exception e) {
			CONSTANT.versionName = CONSTANT.EMPTY;
		}
	}

	@Override
	// 建议在您app的退出之前调用
	public void onTerminate() {
		super.onTerminate();
	}

	/**
	 * 用于主线程执行
	 */
	public void sendMsg(int what, Object obj) {
		handler.obtainMessage(what, obj).sendToTarget();
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if (handlerCallback != null && handlerCallback.containsKey(msg.what))
					handlerCallback.get(msg.what).callBack((String[]) msg.obj);
			} catch (Exception e) {
				// TODO: handle exception
			}
			super.handleMessage(msg);
		}

	};
	private Map<Integer, CallBackListener> handlerCallback;
	private int what = 0;

	public int addHandlerCallbacka(CallBackListener listener) {
		if (listener != null) {
			if (handlerCallback == null)
				handlerCallback = new HashMap<Integer, CallBackListener>();
			what++;
			handlerCallback.put(what, listener);
		}
		return what;
	}
}
