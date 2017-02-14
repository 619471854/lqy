package com.lqy.abook;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.SpeechUtility;
import com.lqy.abook.entity.SiteEnum;
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
		
		// 科大讯飞语音
		SpeechUtility.createUtility(this, "appid=" + CONSTANT.speech_appid);
		// 搜索的网站
		getSearchSite();

		super.onCreate();
	}

	private void getSearchSite() {
		SharedPreferences sp = getSharedPreferences(CONSTANT.SP_CENTER, 0);
		try {
			String text = sp.getString("site", null);
			Type type = new TypeToken<List<SiteEnum>>() {
			}.getType();
			List<SiteEnum> sites = new Gson().fromJson(text, type);
			SiteEnum.searchSite = sites.toArray(new SiteEnum[sites.size()]);
		} catch (Exception e) {
			MyLog.i(e);
		}
		if (SiteEnum.searchSite == null)
			SiteEnum.searchSite = SiteEnum.allSearchSite;
	}

	public void saveSearchSite(List<SiteEnum> sites) {
		if (sites == null || sites.size() == 0)
			return;
		SiteEnum.searchSite = sites.toArray(new SiteEnum[sites.size()]);

		SharedPreferences sp = getSharedPreferences(CONSTANT.SP_CENTER, 0);
		sp.edit().putString("site", new Gson().toJson(sites)).commit();
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
