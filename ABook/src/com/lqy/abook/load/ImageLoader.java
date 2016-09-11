package com.lqy.abook.load;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
	

	/**
	 * 保存图片
	 */
	public static String saveBitmap(InputStream is, String path, String name) {

		FileOutputStream fos = null;
		// 创建文件夹
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();
		file = new File(path, name);
		try {
			fos = new FileOutputStream(file);
			byte buffer[] = new byte[4 * 1024];
			int numread = 0;
			while ((numread = is.read(buffer)) != -1) {
				fos.write(buffer, 0, numread);
			}
			fos.close();
			is.close();
			fos = null;
			is = null;
		} catch (Exception e) {
			return null;
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
				fos = null;
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
				is = null;
			}
		}
		return file.toString();
	}
}
