package com.lqy.abook.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.ImageCompress;
import com.lqy.abook.tool.Util;

public class CutView extends View {
	private Bitmap bm;
	private boolean canOverFlow = false;// 是否能超出边界
	private boolean isFillEmpty = true;// 是否填充空白
	static final int NONE = 0;
	static final int DRAG = 1; // 拖动中
	static final int ZOOM = 2; // 缩放中
	private int mode = NONE; // 当前的事件

	private float beforeLenght; // 两触点距离
	private float afterLenght; // 两触点距离

	private int screenWidth = GlobalConfig.getScreenWidth();
	private int screenHeight = GlobalConfig.getScreenHeight();

	/* 处理拖动 变量 */
	private float start_x;
	private float start_y;
	private float move_x;
	private float move_y;
	private float x;// 图片的位置
	private float y;
	private int showWidth = screenWidth;// 显示的大小
	private int showHeight = screenHeight;
	private int imgWidth = screenWidth;// 图片的大小
	private int imgHeight = screenHeight;
	private static final float scale = 0.02f; // 缩放的比例 X Y方向都是这个值 越大缩放的越快

	private int cutWidth = screenWidth;// 剪切宽度
	private int cutHeight = screenHeight;// 剪切高度
	private boolean isCut = true;// 是否剪切
	private float cutSize = 1;// 剪切宽度的缩放
	private float cutX;
	private float cutY;
	private Matrix matrix;

	private final Paint mOutlinePaint = new Paint();// 边框
	private final Paint mNoFocusPaint = new Paint();
	public static final int DEFAULT = 0;

	public CutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mOutlinePaint.setStrokeWidth(2F);
		mOutlinePaint.setStyle(Paint.Style.STROKE);
		mOutlinePaint.setAntiAlias(true);
		mOutlinePaint.setColor(0xFFFFffff);
		mNoFocusPaint.setARGB(125, 50, 50, 50);
		matrix = new Matrix();
	}

	public void setParams(int screenW, int screenH, int cutW, int cutH) {
		if (screenW != DEFAULT)
			this.screenWidth = screenW;
		if (screenH != DEFAULT)
			this.screenHeight = screenH;

		int maxW = screenWidth < screenHeight ? screenWidth : screenHeight;
		maxW = (int) (maxW * 0.9);
		if (cutW == DEFAULT)
			cutW = maxW;
		if (cutH == DEFAULT)
			cutH = maxW;

		float scaleX = (float) maxW / cutW;
		float scaleY = (float) maxW / cutH;
		cutSize = scaleX < scaleY ? scaleX : scaleY;
		cutWidth = (int) (cutW * cutSize);
		cutHeight = (int) (cutH * cutSize);

		// 居中
		x = (screenWidth - showWidth) / 2;
		y = (screenHeight - showHeight) / 2;

		cutY = (screenHeight - cutHeight) / 2;
		cutX = (screenWidth - cutWidth) / 2;
	}

	/**
	 * 就算两点间的距离
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 处理触碰..
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mode = DRAG;
			start_x = event.getRawX();
			start_y = event.getRawY();
			if (event.getPointerCount() == 2) {
				beforeLenght = spacing(event);
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (spacing(event) > 10f) {
				mode = ZOOM;
				beforeLenght = spacing(event);
			}
			break;
		case MotionEvent.ACTION_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			/* 处理拖动 */
			if (mode == DRAG) {
				move_x = event.getRawX() - start_x;
				move_y = event.getRawY() - start_y;
				if (Math.abs(move_x) > 10 || Math.abs(move_y) > 10) {
					x += move_x;
					y += move_y;
					if (isOverFlow()) {
						x -= move_x;
						y -= move_y;
					} else {
						start_x = event.getRawX();
						start_y = event.getRawY();
						invalidate();
					}
				}
			}
			/* 处理缩放 */
			else if (mode == ZOOM) {
				afterLenght = spacing(event);
				if (afterLenght > 10f) {
					float gapLength = afterLenght - beforeLenght;
					if (Math.abs(gapLength) > 5f) {
						setScale(gapLength > 0);
					}
				}
			}
			break;
		}
		return true;
	}

	/**
	 * 实现处理缩放
	 */
	private void setScale(boolean bigger) {
		if (bigger) {
			x -= scale * showWidth;
			y -= scale * showHeight;
			showWidth += scale * showWidth * 2;
			showHeight += scale * showHeight * 2;
		} else {
			x += scale * showWidth;
			y += scale * showHeight;
			showWidth -= scale * showWidth * 2;
			showHeight -= scale * showHeight * 2;
		}
		if (isOverFlow()) {
			x -= move_x;
			y -= move_y;
		} else {
			invalidate();
			beforeLenght = afterLenght;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!isCut || bm == null || bm.isRecycled()) {
			return;
		}
		// 画灰色图片
		matrix.reset();
		float scaleX = (float) showWidth / imgWidth;
		float scaleY = (float) showHeight / imgHeight;
		matrix.postScale(scaleX, scaleY);
		matrix.postTranslate(x, y);
		canvas.drawBitmap(bm, matrix, mNoFocusPaint);
		// 获取位置
		RectF mDrawRect = new RectF(cutX, cutY, cutX + cutWidth, cutY + cutHeight);
		canvas.save();
		// 画裁切的高亮图片,canvas.clipPath选择相反，有些不支持？..
		canvas.clipRect(mDrawRect);
		canvas.drawBitmap(bm, matrix, null);
		canvas.restore();
		// 画边框
		canvas.drawRect(mDrawRect, mOutlinePaint);
	}

	public void setImageBitmap(Bitmap image) {
		this.bm = image;
		if (bm != null) {
			imgWidth = bm.getWidth();
			imgHeight = bm.getHeight();
			float scaleX = (float) screenWidth / imgWidth;
			float scaleY = (float) screenHeight / imgHeight;
			float minScale = scaleX < scaleY ? scaleX : scaleY;
			showWidth = (int) (imgWidth * minScale);
			showHeight = (int) (imgHeight * minScale);
			// 居中
			x = (screenWidth - showWidth) / 2;
			y = (screenHeight - showHeight) / 2;
		}
	}

	/**
	 * 选择的区域是否超出
	 */
	private boolean isOverFlow() {
		return false;
//		float scale = (float) showWidth / imgWidth;// 图片放大倍数
//		// 获取剪切在view的位置
//		float top = (screenHeight - cutHeight) / 2 - y;
//		float left = (screenWidth - cutWidth) / 2 - x;
//		// 获取剪切在bm的位置
//		top = top / scale;
//		left = left / scale;
//		// 获取剪切的bm的大小
//		float width = cutWidth / scale;
//		float height = cutHeight / scale;
//		// 处理边界
//		boolean isOverFlow = false;
//		if (top < 0) {
//			isOverFlow = true;
//		}
//		if (left < 0) {
//			isOverFlow = true;
//		}
//		if (top + height > imgHeight) {
//			isOverFlow = true;
//		}
//		if (left + width > imgWidth) {
//			isOverFlow = true;
//		}
//		return isOverFlow;
	}

	/**
	 * 获取结果
	 */
	public Bitmap getResult() {
		try {

			float scale = (float) showWidth / imgWidth;// 图片放大倍数
			// 获取剪切在view的位置
			float top = (screenHeight - cutHeight) / 2 - y;
			float left = (screenWidth - cutWidth) / 2 - x;
			// 获取剪切在bm的位置
			top = top / scale;
			left = left / scale;
			// 获取剪切的bm的大小
			float width = cutWidth / scale;
			float height = cutHeight / scale;
			// 获取 剪切的bm的要求宽度
			float resultWidth = cutWidth / cutSize;
			// 获取 剪切的bm的缩放
			scale = resultWidth / width;
			matrix.reset();
			matrix.postScale(scale, scale);
			// 消除误差
			int topInt = (int) top;
			int leftInt = (int) left;
			int widthInt = (int) width;
			int heightInt = (int) height;
			// 处理边界
			boolean isdeal = false;
			if (topInt < 0) {
				heightInt = heightInt + topInt;
				topInt = 0;
				isdeal = true;
			}
			if (leftInt < 0) {
				widthInt = widthInt + leftInt;
				leftInt = 0;
				isdeal = true;
			}
			if (topInt + heightInt > imgHeight) {
				heightInt = imgHeight - topInt;
				isdeal = true;
			}
			if (leftInt + widthInt > imgWidth) {
				widthInt = imgWidth - leftInt;
				isdeal = true;
			}
			if (isdeal && !canOverFlow) {
				// 不允许超出边界
				Util.toast(getContext(), "选择的区域不能超出图片边界");
				return null;
			}

			// 剪切图片
			Bitmap result = Bitmap.createBitmap(bm, leftInt, topInt, widthInt, heightInt, matrix, true);
			// 填充空白区域
			if (canOverFlow && isFillEmpty && isdeal) {
				width = width * scale;
				height = height * scale;
				widthInt = result.getWidth();
				heightInt = result.getHeight();
				Bitmap croppedImage = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.RGB_565);
				{
					Canvas canvas = new Canvas(croppedImage);
					leftInt = (int) ((leftInt - left) * scale);
					topInt = (int) ((topInt - top) * scale);
					Rect dstRect = new Rect(leftInt, topInt, leftInt + widthInt, topInt + heightInt);
					Rect srcRect = new Rect(0, 0, widthInt, heightInt);
					canvas.drawBitmap(result, srcRect, dstRect, null);
					result.recycle();
					result = croppedImage;
				}
			}
			return result;

		} catch (Exception e) {
			Util.toast(getContext(), e.getMessage());
			return null;
		} catch (OutOfMemoryError e) {
			Util.toast(getContext(), e.getMessage());
			return null;
		}
	}

	public void setFillEmpty(boolean isFillEmpty) {
		this.isFillEmpty = isFillEmpty;
	}

	/**
	 * 旋转
	 */
	public void rotate() {
		try {
			bm = new ImageCompress().rotateImg(bm, 90);
			// 设置位置
			float centerX = x + showWidth / 2;
			float centerY = y + showHeight / 2;
			x = centerX - showHeight / 2;
			y = centerY - showWidth / 2;
			// 交换宽高
			int swap = imgWidth;
			imgWidth = imgHeight;
			imgHeight = swap;
			swap = showWidth;
			showWidth = showHeight;
			showHeight = swap;
			invalidate();
		} catch (OutOfMemoryError e) {
			System.gc();
			SystemClock.sleep(200);
			Util.toast(getContext(), "操作失败，请稍后再试");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
