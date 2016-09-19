package com.lqy.abook.img;

import java.io.File;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.ImageLoader;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.MyAlertDialog;

public class SelectImageDialog {

	private MenuActivity activity;
	private String cameraPath = "";// 照相缓存图片路径
	private int sheepTime = 100;// 等待回收内存的时间

	private int requestCode_camera = 10001;
	private int requestCode_image = 10002;

	/**
	 * 照片选择对话框
	 */
	public SelectImageDialog(MenuActivity context) {
		this.activity = context;
	}

	/**
	 * 显示照片选择对话框
	 */
	public void show() {
		String array[] = { "拍照", "从相册中选择", "恢复默认" };
		new AlertDialog.Builder(activity).setItems(array, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {// 使用照相机
					try {
						File file = new File(FileUtil.getCachePath(), "camera" + System.currentTimeMillis() + ".jpg");
						cameraPath = file.toString();
						Uri u = Uri.fromFile(file);
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
						activity.startActivityForResult(intent, requestCode_camera);
						activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					} catch (Exception e) {
						Util.toast(activity, "抱歉，您的手机不支持打开照相机");
					}
				} else if (which == 1) {// 使用本地图片
					try {
						Intent intent;
						if (Build.VERSION.SDK_INT < 19) {
							intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
						} else {
							intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						}
						activity.startActivityForResult(intent, requestCode_image);
						activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

					} catch (Exception e) {
						Util.toast(activity, "抱歉，您的手机不支持打开相册");
					}
				} else if (which == 2) {
					// 重命名以前的图片
					File file = new File(FileUtil.getAppPath(), FileUtil.LOADING_NAME);
					if (file.exists())
						file.renameTo(new File(FileUtil.getAppPath(), System.currentTimeMillis() + "_" + FileUtil.LOADING_NAME));
				}
			}
		}).show();
	}

	/**
	 * 返回 界面 回调
	 */
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == activity.RESULT_OK) {
			if (requestCode == requestCode_camera) {
				dealCameraResult();
				return true;
			}
			if (data != null && requestCode == requestCode_image) {
				dealImageResult(data);
				return true;
			}
		}
		return false;
	}

	/**
	 * 保存图片
	 */
	private void saveImage(final String path) {
		// 重命名以前的图片
		File file = new File(FileUtil.getAppPath(), FileUtil.LOADING_NAME);
		if (file.exists())
			file.renameTo(new File(FileUtil.getAppPath(), System.currentTimeMillis() + "_" + FileUtil.LOADING_NAME));
		file = new File(path);
		if (file.exists())
			file.renameTo(new File(FileUtil.getAppPath(), FileUtil.LOADING_NAME));
		Util.toast(activity, "设置成功");
	}

	private void dealImageResult(Intent data) {
		Bitmap bm = null;

		try {
			Uri originalUri = data.getData(); // 获得图片的uri
			// 显得到bitmap图片获取图片的路径：
			String path = null;
			String name = null;
			try {
				String[] proj = { "_data", "_display_name" };// 路径 名字
				Cursor cursor = activity.managedQuery(originalUri, proj, null, null, null);
				if (cursor == null) {
					path = originalUri.getPath();
				} else {
					cursor.moveToFirst();
					path = cursor.getString(0);
					name = cursor.getString(1);
					if ("null".equals(path))
						path = null;
				}
				if (path != null) {
					File file = new File(path);
					if (!file.exists()) {
						path = null;
					}
				}

			} catch (Exception e) {// 对某些手机的路径获取不到
			}
			// 获取名字
			if (Util.isEmpty(name)) {
				String url = path == null ? originalUri.toString() : path;
				if (url != null) {
					int index = url.lastIndexOf("/");
					if (index != -1)
						name = url.substring(index + 1, url.length());
				}
			}
			if (Util.isEmpty(name))
				name = "cache" + System.currentTimeMillis();
			if (path == null) {// 对某些手机的路径获取不到
				InputStream is = activity.getContentResolver().openInputStream(originalUri);
				path = ImageLoader.saveBitmap(is, FileUtil.getCachePath(), name);
			}
			if (path == null) {
				Util.toast(activity, "未找到图片路径");
				return;
			}
			saveImage(path);
		} catch (Exception e) {
			Util.toast(activity, "获取图片失败");
		} catch (OutOfMemoryError e) {
			try {
				System.gc();
				Thread.sleep(sheepTime);
				sheepTime += 100;
			} catch (Exception e2) {
			}
			if (sheepTime > 2000)
				Util.toast(activity, "内存不足");
			else
				dealImageResult(data);

		} finally {
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
				System.gc();
				bm = null;
			}
		}
	}

	/**
	 * 处理照相图片
	 */
	public void dealCameraResult() {
		if (Util.isEmpty(cameraPath)) {
			Util.toast(activity, "未找到照片");
			return;
		}
		saveImage(cameraPath);
	}

}
