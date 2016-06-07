package com.lqy.abook.img;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ImageViewPager extends ViewPager {

	public ImageViewPager(Context context) {
		super(context);
	}

	public ImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			// 不理会
			e.printStackTrace();
			return false;
		} catch (ArrayIndexOutOfBoundsException e) {
			// 不理会
			e.printStackTrace();
			return false;
		}
	}

}
