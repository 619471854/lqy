package com.lqy.abook.tool;

import android.view.Window;
import android.view.WindowManager;

import com.lqy.abook.MenuActivity;

public class LightUtils {


	/**
	 * 保存当前的屏幕亮度值，并使之生效,-1表示系统亮度
	 */
	public static void setScreenBrightness(MenuActivity activity, int paramInt) {
		Window localWindow = activity.getWindow();
		WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
		float f = paramInt / 255.0F;
		localLayoutParams.screenBrightness = f;
		localWindow.setAttributes(localLayoutParams);
	}

//	/**
//	 * 获得当前屏幕亮度值 0--255
//	 */
//	public static int getScreenBrightness(MenuActivity activity) {
//		int screenBrightness = 255;
//		try {
//			screenBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
//		} catch (Exception localException) {
//
//		}
//		return screenBrightness;
//	}
//	/**
//	 * 获得当前屏幕亮度的模式 SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
//	 * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
//	 */
//	public static int getScreenMode(MenuActivity activity) {
//		int screenMode = 0;
//		try {
//			screenMode = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
//		} catch (Exception localException) {
//
//		}
//		return screenMode;
//	}
//
//	/**
//	 * 设置当前屏幕亮度的模式为自动调节屏幕亮度,好像没用？
//	 */
//	public static void setScreenModeAutoMatic(MenuActivity activity) {
//		try {
//			Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
//		} catch (Exception localException) {
//			localException.printStackTrace();
//		}
//	}
//
//	/**
//	 * 设置当前屏幕亮度的模式为手动调节屏幕亮度
//	 */
//	public static void setScreenModeManual(MenuActivity activity) {
//		try {
//			Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//		} catch (Exception localException) {
//			localException.printStackTrace();
//		}
//	}
//
//	/**
//	 * 设置当前屏幕亮度值 0--255
//	 */
//	public static void saveScreenBrightness(MenuActivity activity, int paramInt) {
//		try {
//			Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);
//		} catch (Exception localException) {
//			localException.printStackTrace();
//		}
//	}
}
