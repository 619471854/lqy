package com.lqy.abook.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;

public class ImageCompress {
	// ThumbnailUtils
	private static int maxSize = 100 * 1024;// 压缩后file的最大 大小
	private int sheep = 100;// 等待回收内存的时间

	/**
	 * 压缩图片精度,保存到file里
	 */
	public String saveBmpToFile(Bitmap bmp, String path, String name) throws Exception{
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();// 创建文件夹

		path = path + File.separator + name;
		file = new File(path);
		if (file.exists())
			file.delete();
		FileOutputStream bitmapWtriter = null;
		try {
			bitmapWtriter = new FileOutputStream(file);

			int quality = getCompressBmpSize(bmp, maxSize);
			bmp.compress(Bitmap.CompressFormat.JPEG, quality, bitmapWtriter);
		} finally {
			if (bitmapWtriter != null) {
				try {
					bitmapWtriter.close();
					bitmapWtriter = null;
				} catch (IOException e) {
				}
			}
		}
		return path;
	}

	/**
	 * 压缩图片精度,保存到file里，maxSize表示file的最大 大小,options:压缩程度，100表示不压缩
	 */
	public String compressBmpToFile(Bitmap bmp, String path, String name) {
		if (bmp == null)
			return path + File.separator + name;
		sheep = 100;
		ByteArrayOutputStream baos = null;
		try {

			baos = compressBmp(bmp, maxSize);

			File file = new File(path);
			if (!file.exists())
				file.mkdirs();// 创建文件夹

			path = path + File.separator + name;
			file = new File(path);
			if (file.exists())
				file.delete();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				fos.write(baos.toByteArray());
				// fos.flush();
				// fos.close();
			} finally {
				if (fos != null) {
					fos.flush();
					fos.close();
					fos = null;
				}
			}

		} catch (Exception e) {
		} finally {

			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
				}
				baos = null;
			}

		}
		return path;
	}

	/**
	 * 压缩图片精度
	 */
	public ByteArrayOutputStream compressBmp(Bitmap bmp, int maxSize) {

		ByteArrayOutputStream baos = null;
		try {
			//
			// baos = new ByteArrayOutputStream();
			// bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			// int length = baos.toByteArray().length;
			// int options = (maxSize * 100) / length + 1;// 压缩程度,100表示不压缩
			// if (options < 100) {
			// bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
			// }
			// return baos;
			baos = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 90;
			int length = baos.toByteArray().length;
			while (length > maxSize && options > 1) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();// 重置baos即清空baos
				bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
				if (options > 10)
					options -= 10;// 每次都减少10
				else
					options = options / 2;
				length = baos.toByteArray().length;
			}
			MyLog.i("compressBmp size=" + options);
			return baos;
		} catch (OutOfMemoryError e) {

			try {
				System.gc();
				Thread.sleep(sheep);
				sheep += 100;
			} catch (Exception e2) {
			}

			return compressBmp(bmp, maxSize);

		}
	}

	/**
	 * 压缩图片精度
	 */
	public int getCompressBmpSize(Bitmap bmp, int maxSize) {

		int options = 100;
		try {
			//
			// baos = new ByteArrayOutputStream();
			// bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			// int length = baos.toByteArray().length;
			// int options = (maxSize * 100) / length + 1;// 压缩程度,100表示不压缩
			// if (options < 100) {
			// bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
			// }
			// return baos;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int length = baos.toByteArray().length;
			while (length > maxSize && options > 1) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();// 重置baos即清空baos
				bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
				if (options > 10)
					options -= 10;// 每次都减少10
				else
					options = options / 2;
				length = baos.toByteArray().length;
			}
			MyLog.i("compressBmp size=" + options);
			return options;
		} catch (OutOfMemoryError e) {

			try {
				System.gc();
				Thread.sleep(sheep);
				sheep += 100;
			} catch (Exception e2) {
			}

			return getCompressBmpSize(bmp, maxSize);

		}
	}

	/**
	 * 如果图片较大，缩小图片,获取大于imgWidth的最小图片
	 */
	public Bitmap getSmallBitmap(Bitmap bm, int imgWidth, int imgHeight) {
		float width = bm.getWidth();
		float height = bm.getHeight();
		// 我们想要的新的图片大小
		if (height > imgHeight + 1 && width > imgWidth + 1) {// +1除去float造成的误差
			// 长和宽放大缩小的比例
			float scaleX = imgWidth / width;
			float scaleY = imgHeight / height;
			float scale = scaleX > scaleY ? scaleX : scaleY;
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap resizeBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			return resizeBmp;
		} else
			return bm;
	}

	/**
	 * 如果图片较大，缩小图片,获取小于height的最大图片
	 */
	public Bitmap getMaxBitmap(Bitmap bm, int maxWidth, int maxHeight) {
		float width = bm.getWidth();
		float height = bm.getHeight();
		// 我们想要的新的图片大小
		if (height > maxWidth || width > maxHeight) {
			// 长和宽放大缩小的比例
			float scaleX = maxWidth / width;
			float scaleY = maxHeight / height;
			float scale = scaleX < scaleY ? scaleX : scaleY;
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap resizeBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			return resizeBmp;
		} else
			return bm;
	}

	/**
	 * 缩放图片大小,获取等于width的图片
	 */
	public Bitmap getSmallBitmapFill(Bitmap bm, int imgWidth, int imgHeight) {
		float width = bm.getWidth();
		float height = bm.getHeight();
		// 我们想要的新的图片大小
		if (height > imgHeight && width > imgWidth) {
			float scaleX = imgWidth / width;
			float scaleY = imgHeight / height;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleX, scaleY); // 长和宽放大缩小的比例
			Bitmap resizeBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
			return resizeBmp;
		} else
			return bm;
	}

	/**
	 * 计算图片的缩放值,获取大于imgWidth的最小图片
	 */
	public int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
		int height = options.outHeight;
		int width = options.outWidth;
		// 只用于压缩到 大于手机大小的图片
		// 如果 reqWidth大于reqHeight，width小于height，就把reqWidth与reqHeight对调
		if ((height > width && reqWidth > reqHeight) || (height < width && reqWidth < reqHeight)) {
			int swap = reqWidth;
			reqWidth = reqHeight;
			reqHeight = swap;
		}
		int inSampleSize = 1;

		if (height > reqHeight && width > reqWidth) {
			int heightRatio = Math.round((float) height / (float) reqHeight);
			int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		// 获取2的倍数的近似值,inSampleSize只能处理2的倍数
		int scale = 2;
		while (scale < inSampleSize)
			scale *= 2;
		if (scale * 0.75 <= inSampleSize)
			inSampleSize = scale;
		else {
			inSampleSize = scale / 2;
		}
		return inSampleSize;
	}

	/**
	 * 计算图片的缩放值,获取小于height的最大图片
	 */
	public int calculateInSampleSizeInside(Options options, int reqWidth, int reqHeight) {
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			int heightRatio = Math.round((float) height / (float) reqHeight);
			int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}
		// 获取2的倍数的近似值,inSampleSize只能处理2的倍数
		int scale = 2;
		while (scale < inSampleSize)
			scale *= 2;
		if (scale * 0.75 <= inSampleSize)
			inSampleSize = scale;
		else {
			inSampleSize = scale / 2;
		}
		return inSampleSize;
	}

	/**
	 * 如果图片较大，缩小图片,获取大于imgWidth的最小图片
	 */
	public Bitmap getSmallBitmap(String filePath, int width, int height) {
		try {
			Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);
			options.inSampleSize = calculateInSampleSize(options, width, height);
			options.inJustDecodeBounds = false;
			options.inPurgeable = true;
			options.inInputShareable = true;
			return BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError e) {// 如果内存溢出，重新加载
			System.gc();
			try {
				Thread.sleep(100);
			} catch (Exception e2) {
				// TODO: handle exception
			}
			return getSmallBitmap(filePath, width * 8 / 10, height * 8 / 10);
		}
	}

	/**
	 * 如果图片较大，缩小图片,获取小于imgWidth的最大图片
	 */
	public Bitmap getMaxBitmap(String filePath, int width, int height) {
		try {
			if (Util.isEmpty(filePath))
				return null;
			Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);
			options.inSampleSize = calculateInSampleSizeInside(options, width, height);
			options.inJustDecodeBounds = false;
			options.inPurgeable = true;
			options.inInputShareable = true;
			return BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError e) {
			System.gc();
			try {
				Thread.sleep(100);
			} catch (Exception e2) {
				// TODO: handle exception
			}
			return getMaxBitmap(filePath, width * 8 / 10, height * 8 / 10);
		}
	}

	//
	// /**
	// * 是否需要缩放
	// */
	// public boolean isSmallBitmap(String filePath) {
	// Options options = new Options();
	// options.inJustDecodeBounds = true;
	// BitmapFactory.decodeFile(filePath, options);
	// return calculateInSampleSize(options, img_w, img_h) > 1;
	// }

	/**
	 * 将彩色图转换为灰度图
	 * 
	 * @param img
	 *            位图
	 * @return 返回转换好的位图
	 */
	public Bitmap convertGreyImg(Bitmap img) {
		if (img == null || img.isRecycled())
			return null;
		try {

			int width = img.getWidth(); // 获取位图的宽
			int height = img.getHeight(); // 获取位图的高

			int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组

			img.getPixels(pixels, 0, width, 0, 0, width, height);
			int alpha = 0xFF << 24;
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int grey = pixels[width * i + j];

					int red = ((grey & 0x00FF0000) >> 16);
					int green = ((grey & 0x0000FF00) >> 8);
					int blue = (grey & 0x000000FF);

					grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
					grey = alpha | (grey << 16) | (grey << 8) | grey;
					pixels[width * i + j] = grey;
				}
			}
			Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
			result.setPixels(pixels, 0, width, 0, 0, width, height);
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 图片上添加文字
	 */
	public Bitmap drawText(Bitmap bm, String text) {
		Bitmap newb = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Config.ARGB_8888);
		// 创建一个新的和SRC长度宽度一样的位图
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(bm, 0, 0, null); // 在 0，0坐标载入图片
		// 添加文字
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		float dentity = GlobalConfig.getDensity();
		paint.setStrokeWidth(dentity);
		paint.setTextSize(bm.getWidth() / 15);// 设置字体大小
		// String text="2014-11-24 11:20:20";
		float textWidth = paint.measureText(text);// 文字宽度
		canvas.drawText(text, bm.getWidth() - textWidth - dentity * 5, bm.getHeight() - dentity * 5, paint);
		// 保存
		canvas.save(Canvas.ALL_SAVE_FLAG);// 保存
		canvas.restore();// 存储

		return newb;
	}

	public int getOrientation(String jpegFile) {
		int rotation = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(jpegFile);
			int result = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
			switch (result) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotation = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotation = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotation = 270;
				break;
			}
			MyLog.i("getOrientation " + rotation);
		} catch (Exception e) {
			MyLog.i("getOrientation " + e.toString());
		}
		return rotation;
	}

	/**
	 * 旋转图片
	 */
	public Bitmap rotateImg(Bitmap bitmap, int rotation) {
		if (bitmap == null || rotation == 0)
			return bitmap;
		Matrix matrix = new Matrix();
		matrix.setRotate(rotation); // 设置旋转

		// 按照matrix的旋转构建新的Bitmap
		try {
			Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			if (result != null) {
				bitmap.recycle();
				bitmap = result;
			}
		} catch (OutOfMemoryError e) {
			try {
				System.gc();
				Thread.sleep(200);
			} catch (Exception e2) {
			}
			try {
				matrix.setScale(0.5f, 0.5f);
				Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				if (result != null) {
					bitmap.recycle();
					bitmap = result;
				}
			} catch (OutOfMemoryError e1) {
			}
		}
		return bitmap;
	}
}
