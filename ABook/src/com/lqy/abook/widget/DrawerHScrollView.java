package com.lqy.abook.widget;

import java.util.Hashtable;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import com.lqy.abook.tool.MyLog;

public class DrawerHScrollView extends HorizontalScrollView {

	private IDrawerPresenter drawerPresenter = null;
	private int currentPage = 0;
	private int totalPages = 1;
	private Hashtable<Integer, Integer> positionLeftTopOfPages = new Hashtable();
	private float touchX;
	private int postionTo = -1;
	private Handler handler = new Handler();

	public DrawerHScrollView(Context context) {
		super(context);
	}

	public void setDrawerPresenter(IDrawerPresenter drawerPresenter) {
		this.drawerPresenter = drawerPresenter;
	}

	public DrawerHScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawerHScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void cleanup() {
		currentPage = 0;
		totalPages = 1;
		drawerPresenter = null;
		if (positionLeftTopOfPages != null) {
			positionLeftTopOfPages.clear();
		}
	}

	public void setParameters(int totalPages, int currentPage, int scrollDisX) {
		MyLog.i("~~~~~setParameters totalPages:" + totalPages + ",currentPage:" + currentPage + ",scrollDisX:" + scrollDisX);
		this.totalPages = totalPages;
		this.currentPage = currentPage;
		positionLeftTopOfPages.clear();
		for (int i = 0; i < totalPages; i++) {
			int posx = (scrollDisX) * i;
			positionLeftTopOfPages.put(i, posx);
			MyLog.i("~~~~~setParameters i:" + i + ",posx:" + posx);
		}
		smoothScrollTo(0, 0);
	}

	public void setPresenter(IDrawerPresenter drawerPresenter) {
		this.drawerPresenter = drawerPresenter;
	}

	private Runnable scrollRunnable = new Runnable() {
		@Override
		public void run() {
			if (postionTo != -1) {
				MyLog.i("scrollRunnable " + postionTo);
				drawerPresenter.dispatchEvent(totalPages, currentPage);
				smoothScrollTo(postionTo, 0);
				postionTo = -1;
			}

		}
	};

	// 如果慢慢拖动fling不会执行
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchX = ev.getRawX();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			boolean change_flag = false;
			if (ev.getRawX() < touchX && (currentPage < totalPages - 1)) {
				currentPage++;
				change_flag = true;
			} else if (ev.getRawX() > touchX && (currentPage > 0)) {
				currentPage--;
				change_flag = true;
			}
			if (change_flag) {
				postionTo = positionLeftTopOfPages.get(new Integer(currentPage)).intValue();
				handler.postDelayed(scrollRunnable, 1);
			}
			break;

		default:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	//
	// // 滚动视图的fling手势
	// @Override
	// public void fling(int velocityX) {
	// boolean change_flag = false;
	// if (velocityX > 0 && (currentPage < totalPages - 1)) {
	// currentPage++;
	// change_flag = true;
	// } else if (velocityX < 0 && (currentPage > 0)) {
	// currentPage--;
	// change_flag = true;
	// }
	// if (change_flag) {
	// int postionTo = (Integer) positionLeftTopOfPages.get(new
	// Integer(currentPage)).intValue();
	// smoothScrollTo(postionTo, 0);
	// drawerPresenter.dispatchEvent(totalPages, currentPage);
	// Log.i(TAG, "------fling scrollTo " + postionTo);
	// postionTo = -1;
	// }
	// super.fling(velocityX);
	// }

	public interface IDrawerPresenter {
		void dispatchEvent(int totalPages, int currentPage);
	}

}