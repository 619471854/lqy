package com.lqy.abook.img;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class ErrorPageListener implements View.OnTouchListener {
	private static final int INVALID_POINTER = -1;
	private float mLastMotionX = -1;
	private int mActivePointerId = INVALID_POINTER;
	private boolean mIsDragging;// 是否拖曳

	private int mTouchSlop;
	private int mCurrentPage;
	private ImageViewPager viewPager;
	private View view;

	public ErrorPageListener(ImageViewPager mViewPager) {
		this.viewPager = mViewPager;
	}

	public void setView(View view) {
		this.view = view;

		ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		if ((viewPager == null) || (viewPager.getAdapter().getCount() == 0)) {
			return false;
		}
		try {
			final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
				mLastMotionX = ev.getX();
				break;
			case MotionEvent.ACTION_MOVE: {
				final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
				final float x = MotionEventCompat.getX(ev, activePointerIndex);
				final float deltaX = x - mLastMotionX;

				if (!mIsDragging) {
					if (Math.abs(deltaX) > mTouchSlop) {
						mIsDragging = true;
					}
				}

				if (mIsDragging) {
					mLastMotionX = x;
					if (viewPager.isFakeDragging() || viewPager.beginFakeDrag()) {
						viewPager.fakeDragBy(deltaX);
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (!mIsDragging) {
					final int count = viewPager.getAdapter().getCount();
					final int width = view.getWidth();
					final float halfWidth = width / 2f;
					final float sixthWidth = width / 6f;

					if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
						if (action != MotionEvent.ACTION_CANCEL) {
							viewPager.setCurrentItem(mCurrentPage - 1);
						}
						return true;
					} else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
						if (action != MotionEvent.ACTION_CANCEL) {
							viewPager.setCurrentItem(mCurrentPage + 1);
						}
						return true;
					}
				}

				mIsDragging = false;
				mActivePointerId = INVALID_POINTER;
				if (viewPager.isFakeDragging())
					viewPager.endFakeDrag();
				break;

			case MotionEventCompat.ACTION_POINTER_DOWN: {
				final int index = MotionEventCompat.getActionIndex(ev);
				mLastMotionX = MotionEventCompat.getX(ev, index);
				mActivePointerId = MotionEventCompat.getPointerId(ev, index);
				break;
			}

			case MotionEventCompat.ACTION_POINTER_UP:
				final int pointerIndex = MotionEventCompat.getActionIndex(ev);
				final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
				if (pointerId == mActivePointerId) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
				}
				mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, mActivePointerId));
				break;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
