package com.lqy.abook.load;

import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader {

	public static void load(Activity a, ImageView view, String url, int progressId) {
		if (a != null)
			load(a, view, url, a.findViewById(progressId));
	}

	public static void load(final Activity a, final ImageView view, final String url, final View progress) {
		if (a == null || view == null || Util.isEmpty(url))
			return;
		if (progress != null)
			progress.setVisibility(View.VISIBLE);
		view.setTag(url);
		new Thread() {
			public void run() {
				// 设置头像
				final String file = FileUtil.loadImageForUrl(url, FileUtil.getImagePath());
				if (a == null || a.isFinishing())
					return;
				a.runOnUiThread(new Runnable() {
					public void run() {
						if (progress != null)
							progress.setVisibility(View.GONE);
						Bitmap image = loadFile(file, 1);
						if (image != null) {
							view.setImageBitmap(image);
						}
					}
				});
			}
		}.start();
	}

	// 本地加载最大图片
	public static Bitmap loadFile(String file, int scale) {
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = scale;
			o.inPurgeable = true;
			o.inInputShareable = true;
			return BitmapFactory.decodeFile(file, o);
		} catch (OutOfMemoryError e) {
			MyLog.i("OutOfMemoryError:scale=" + scale);
			System.gc();// 回收内存
			try {
				Thread.sleep(300);
			} catch (Exception e2) {
			}

			return loadFile(file, scale * 2);// 如果内存不够，缩小图片

		}

	}
}
