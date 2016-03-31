package com.lqy.abook.tool;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Looper;

import com.lqy.abook.activity.LoadingActivity;

public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;

	// 用于格式化日期,作为日志文件名的一部分

	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// MyService.instance.close();
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// Log.e(TAG, "error : ", e);
			}
			// 退出程序
			// android.os.Process.killProcess(android.os.Process.myPid());
			// System.exit(-1);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		// 使用Toast来显示异常信息
		new Thread() {
			@Override
			public void run() {
				try {
					Looper.prepare();
					Util.toast(mContext, "抱歉,程序出现异常即将退出");
					Looper.loop();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}.start();
		// 保存日志文件
		String error = new ErrorLog().saveCrashInfoFile(ex, mContext);

		System.out.println("uncaughtException");
		// System.exit(0);
		Intent intent = new Intent(mContext, LoadingActivity.class);
		if (!Util.isEmpty(error))
			intent.putExtra("error", error);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		CONSTANT.isDebug = (mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		if (!CONSTANT.isDebug)
			mContext.startActivity(intent);
		System.exit(0);
		return true;
	}

}
