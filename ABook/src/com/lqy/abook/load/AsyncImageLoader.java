package com.lqy.abook.load;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class AsyncImageLoader {
	private HashMap<String, SoftReference<Drawable>> imageCache;
	private ThreadPoolExecutor executor;
	private String filePath;

	public AsyncImageLoader() {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
		// 线程池：最大50条，每次执行：10条，空闲线程结束的超时时间：30秒
		executor = new ThreadPoolExecutor(10, 50, 30, TimeUnit.SECONDS, new LinkedBlockingQueue());
		filePath = FileUtil.getImagePath();
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void loadDrawable(ImageView view, String imageUrl, int defaultImg) {
		loadDrawable(view, imageUrl, defaultImg, false);
	}

	public void loadDrawable(final ImageView view, String imageUrl, final int defaultImg, final boolean notSetTag) {
		if (view != null) {
			view.setImageResource(defaultImg);
			loadDrawable(view, imageUrl, null, notSetTag);
		}
	}

	public Drawable loadDrawable(ImageView view, String imageUrl, ImageCallback listener) {
		return loadDrawable(view, imageUrl, listener, false);
	}

	public Drawable loadDrawable(final ImageView view, final String imageUrl, final ImageCallback listener, final boolean notSetTag) {
		if (view == null) {
			return null;
		}
		if (Util.isEmpty(imageUrl)) {
			if (!notSetTag)
				view.setTag(CONSTANT.EMPTY);
			return null;
		}

		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				if (!notSetTag)
					view.setTag(imageUrl);
				view.setImageDrawable(drawable);
				return drawable;
			}
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				Drawable drawable = (Drawable) message.obj;
				if (drawable != null) {
					if (notSetTag || imageUrl.equals(view.getTag())) {
						view.setImageDrawable(drawable);
						if (listener != null)
							listener.asynLoadSuccess();
					}
				}
			}
		};

		// 用线程池来做下载图片的任务
		if (!notSetTag)
			view.setTag(imageUrl);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				String file = FileUtil.loadImageForUrl(imageUrl, filePath);
				try {
					if (file != null) {
						Drawable drawable = Drawable.createFromPath(file);
						if (drawable == null) {
							File f = new File(file);
							if (f.exists())
								f.delete();
							file = FileUtil.loadImageForUrl(imageUrl, filePath);
							if (file != null) {
								drawable = Drawable.createFromPath(file);
							}
						}
						if (drawable != null) {
							imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
							Message message = handler.obtainMessage(0, drawable);
							handler.sendMessage(message);
						}
					}
				} catch (OutOfMemoryError e) {
					// TODO: handle exception
				}
				handler.sendEmptyMessage(0);
			}
		});
		return null;
	}

	public interface ImageCallback {
		public void asynLoadSuccess();
	}

}
