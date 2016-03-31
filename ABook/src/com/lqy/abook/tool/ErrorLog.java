package com.lqy.abook.tool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.baidu.mobstat.StatService;
import com.lqy.abook.MyApp;
import com.lqy.abook.load.FileUtil;

public class ErrorLog {

	/**
	 * 上传json解析错误到百度app分析
	 */
	public void uploadJsonErrorToBaidu(final String url, final String msg) {
		new Thread() {
			public void run() {
				new ErrorLog().uploadToBaidu(MyApp.getInstance(), "json_error", url + "*****" + msg);
			};
		}.start();
	}

	/**
	 * 上传到百度app分析
	 */
	public void uploadToBaidu(Context context, String id, String info) {
		try {
			StatService.onEvent(context, id, info);
		} catch (Exception e) {
		}
	}

	/**
	 * 保存错误信息到文件中
	 */
	public String saveCrashInfoFile(Throwable ex, Context ctx) {
		try {
			long timestamp = System.currentTimeMillis();
			String fileName = "crash-" + Util.sdf.format(new Date()) + "-" + timestamp + ".txt";
			String path = FileUtil.getErrorPath();

			String error = collectInfo(ex, ctx);

			// 保存到本地
			FileUtil.write(error, path, fileName);
			// 显示到日志里
			MyLog.e("error:" + error);
			return error;
		} catch (Exception e) {
			// Log.e(TAG, "an error occured while writing file...", e);
		}
		return null;
	}

	/**
	 * 收集信息
	 */
	private String collectInfo(Throwable ex, Context ctx) {
		StringBuffer sb = new StringBuffer();
		try {
			// 时间
			String time = Util.sdf.format(new Date());
			sb.append("时间" + "=" + time + "\n");
			if (GlobalConfig.exitCurrentActivity())
				sb.append("当前的界面" + "=" + GlobalConfig.getCurrentActivity().getClass().getName() + "\n");
			// 收集设备参数信息

			if (ctx != null) {
				sb.append("设备参数信息\n");
				try {
					PackageManager pm = ctx.getPackageManager();
					PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
					if (pi != null) {
						String versionName = pi.versionName == null ? "null" : pi.versionName;
						String versionCode = pi.versionCode + CONSTANT.EMPTY;
						sb.append("versionName=" + versionName + "\n");
						sb.append("versionCode=" + versionCode + "\n");
					}
				} catch (NameNotFoundException ne) {
				}
			}
			Field[] fields = Build.class.getDeclaredFields();
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					sb.append(field.getName() + "=" + field.get(null).toString() + "\n");
				} catch (Exception e1) {
				}
			}
			if (ex != null) {
				sb.append("错误信息\n");
				PrintWriter printWriter = null;
				try {
					Writer writer = new StringWriter();
					printWriter = new PrintWriter(writer);
					ex.printStackTrace(printWriter);
					Throwable cause = ex.getCause();
					while (cause != null) {
						cause.printStackTrace(printWriter);
						cause = cause.getCause();
					}
					String result = writer.toString();
					sb.append(result);
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					try {
						if (printWriter != null)
							printWriter.close();
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return sb.toString();
	}

}
