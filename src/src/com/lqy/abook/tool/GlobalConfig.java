package com.lqy.abook.tool;

import java.lang.reflect.Field;

import android.util.DisplayMetrics;

import com.lqy.abook.MenuActivity;

public class GlobalConfig {

	private static MenuActivity currentActivity;// 当前显示的界面
	public static boolean activityIsBack = false;// 当前界面是否处于后台

	public static void setCurrentActivityBackground(MenuActivity a, boolean isBackground) {
		if (currentActivity == a)
			activityIsBack = isBackground;
	}

	public static void setCurrentActivity(MenuActivity a) {
		currentActivity = a;
		activityIsBack = false;
	}

	public static MenuActivity getCurrentActivity() {
		return currentActivity;
	}

	public static boolean exitCurrentActivity() {
		return currentActivity != null && !currentActivity.isFinishing();
	}

	// 屏幕的宽和高
	private static int screenWidth = -1;
	private static int screenHeight = -1;
	private static float density = -1;
	private static int statusBarHeight = -1;

	private static void initScreenInfo() {
		if (!exitCurrentActivity()) {
			screenWidth = 720; // 屏幕宽度（像素）
			screenHeight = 1280; // 屏幕高度（像素）
			density = 2; // 屏幕密度（0.75 / 1.0 / 1.5）
		} else {
			DisplayMetrics metric = new DisplayMetrics();
			getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
			screenWidth = metric.widthPixels; // 屏幕宽度（像素）
			screenHeight = metric.heightPixels; // 屏幕高度（像素）
			density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		}
		// 获取状态栏高度
		try {
			Class<?> c = null;
			Object obj = null;
			Field field = null;
			int x = 0;
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = getCurrentActivity().getResources().getDimensionPixelSize(x);
			// 在oncreate里无效
			// Rect outRect = new Rect();
			// currentActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
			// float statusBarHeight = outRect.top;
			screenHeight -= statusBarHeight;
		} catch (Exception e) {
			MyLog.e(e);
		}

	}

	public static int getScreenWidth() {

		if (screenWidth == -1) {
			initScreenInfo();
		}

		return screenWidth;

	}

	public static int getStatusBarHeight() {

		if (statusBarHeight == -1) {
			initScreenInfo();
		}

		return statusBarHeight;

	}

	public static int getScreenHeight() {

		if (screenHeight == -1) {
			initScreenInfo();
		}

		return screenHeight;

	}

	public static float getDensity() {

		if (density == -1) {
			initScreenInfo();
		}

		return density;

	}

}
