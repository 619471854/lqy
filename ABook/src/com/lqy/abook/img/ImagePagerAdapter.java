package com.lqy.abook.img;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lqy.abook.R;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.tool.Util;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class ImagePagerAdapter extends PagerAdapter {

	private List<String> images;
	private LayoutInflater inflater;
	private Activity activity;
	protected ImageLoader imageLoader;
	private DisplayImageOptions options;
	private File cacheDir;// 缓存路径

	private static enum LoadState {
		LOADING, // 下载中
		FAILED, // 失败
		COMPLETED// 完成
	}

	private LoadState loadstate[];// 下载状态

	public ImagePagerAdapter(Activity activity, List<String> urls, long bookId,String chapterName) {
		this.images = urls;
		this.activity = activity;
		inflater = activity.getLayoutInflater();
		this.loadstate = new LoadState[urls.size()];
		for (int i = 0; i < urls.size(); i++) {
			loadstate[i] = LoadState.LOADING;
		}
		init(bookId,chapterName);
	}

	private void init(long bookId,String chapterName) {
		options = new DisplayImageOptions.Builder()
				// .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.ARGB_8888)
				.displayer(new FadeInBitmapDisplayer(300)).build();

		imageLoader = ImageLoader.getInstance();
		cacheDir = new File(FileUtil.getBooksPath(bookId),FileUtil.getChapterName(chapterName) );
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.discCache(new UnlimitedDiscCache(cacheDir)).build();
		imageLoader.init(config);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public Object instantiateItem(ViewGroup view, final int position) {
		View imageLayout = inflater.inflate(R.layout.image_show_more_list, view, false);

		final PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image_show_more_list_image);
		final View view_lay = imageLayout.findViewById(R.id.image_show_more_list_lay);
		errorPageListener.setView(view_lay);
		view_lay.setOnTouchListener(errorPageListener);

		imageLoader.displayImage(images.get(position), imageView, options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				view_lay.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				View errorText = view_lay.findViewById(R.id.image_show_more_list_error);
				View errorProgress = view_lay.findViewById(R.id.image_show_more_list_progress);
				errorText.setVisibility(View.VISIBLE);
				errorProgress.setVisibility(View.GONE);

				loadstate[position] = LoadState.FAILED;
				// switch (failReason.getType())
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				view_lay.setVisibility(View.GONE);
				loadstate[position] = LoadState.COMPLETED;
				// 不放大图片
				//imageView.setScaleType(ScaleType.CENTER_INSIDE);
			}
		});

		((ViewPager) view).addView(imageLayout, 0);
		return imageLayout;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View container) {
	}

	/**
	 * 获取当前图片的缓存路径
	 */
	public String getCurrentImgPath(int index) {
		try {
			if (loadstate[index] == LoadState.LOADING) {
				Util.toast(activity, "图片下载中，请稍后再试");
				return null;
			} else if (loadstate[index] == LoadState.FAILED) {
				Util.toast(activity, "图片下载失败");
				return null;
			}
			int name = images.get(index).hashCode();
			return cacheDir.toString() + File.separator + name;
		} catch (Exception e) {
			Util.toast(activity, "图片下载失败");
			return null;
		}

	}

	private ErrorPageListener errorPageListener;

	public void setErrorPageListener(ErrorPageListener errorPageListener) {
		this.errorPageListener = errorPageListener;
	}
}