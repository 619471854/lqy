package com.lqy.abook.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MyImageButton extends ImageView {
	public MyImageButton(Context context) {
		super(context);
		setClickable(true);
	}

	public MyImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
	}

	@SuppressLint("NewApi")
	public MyImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setClickable(true);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// MyLog.i( "dispatchTouchEvent:"+event.getAction());
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// MyLog.i( "onTouchEvent:" + event.getAction() + "  " + getLeft() + " "
		// + getTop() + " " + getWidth());
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			int left = getLeft();
			int top = getTop();
			int width = getWidth();
			int height = getHeight();
			// 缩小5
			this.layout(left + 8, top + 8, left + width - 8, top + height - 8);
		} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL
				|| event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			int left = getLeft();
			int top = getTop();
			int width = getWidth();
			int height = getHeight();
			// 放大5
			this.layout(left - 8, top - 8, left + width + 8, top + height + 8);
		}
		return super.onTouchEvent(event);
	}

}
