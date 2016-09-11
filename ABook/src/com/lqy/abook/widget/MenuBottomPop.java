package com.lqy.abook.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lqy.abook.R;
import com.lqy.abook.tool.GlobalConfig;

/**
 * 从界面下部弹出的  Dialog
 */
public class MenuBottomPop extends Dialog {
	private Context activity;

	public MenuBottomPop(Context context) {
		super(context, R.style.dialog_full_screen);
		this.activity = context;
	}

	public MenuBottomPop setParams(int layoutResId, int[] btnId, View.OnClickListener listener) {
		return setParams(layoutResId, R.id.pop_layout, btnId, listener);
	}

	public MenuBottomPop setParams(int layoutResId, int contentId, int[] btnId, View.OnClickListener listener) {
		View parent = LayoutInflater.from(activity).inflate(layoutResId, null);
		View content = parent.findViewById(contentId);
		if (listener != null) {
			for (int id : btnId) {
				View button = parent.findViewById(id);
				if (button != null)
					button.setOnClickListener(listener);
			}
		}
		// 点击外围关闭
		parent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
		if (content != null)
			content.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				}
			});

		setView(parent);
		return this;
	}

	public void setView(View v) {
		this.setContentView(v);

		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.97f;
		lp.dimAmount = 0.5f;
		window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setWindowAnimations(R.style.pop_down); // 设置窗口弹出动画
		lp.width = GlobalConfig.getScreenWidth();

		window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// 点击外围关闭
		View lay = v.findViewById(R.id.pop_layout);
		if (lay != null) {
			v.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					cancel();
				}
			});
			lay.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
				}
			});
		}

	}

	public void show() {
		try {
			super.show();
		} catch (Exception e) {
		}
	}

	public void cancel() {
		try {
			if (isShowing())
				dismiss();
		} catch (Exception e) {
		}
	}

	protected View getView() {
		return null;
	}
}