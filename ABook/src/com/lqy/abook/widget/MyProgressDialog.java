package com.lqy.abook.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.tool.Util;

public class MyProgressDialog {
	private Dialog builder;
	private Context context;
	private String text;// 显示的文本
	private ProgressBar progressBar;
	private boolean isCanCancel = false;

	public MyProgressDialog(Context context) {
		this.context = context;
	}

	public boolean isShowing() {
		return builder != null && builder.isShowing();
	}

	public MyProgressDialog(Context context, int id) {
		this.context = context;
		this.text = context.getString(id);
	}

	public MyProgressDialog(Context context, String text) {
		this.context = context;
		this.text = text;
	}

	public void setProgress(int progress) {
		// 未处理
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setCanCancel(boolean isCanCancel) {
		this.isCanCancel = isCanCancel;
	}

	public void show() {
		try {
			builder = new Dialog(context, R.style.my_progress_dialog);
			builder.setContentView(getView());
			builder.setCancelable(isCanCancel);
			// WindowManager.LayoutParams lp =
			// builder.getWindow().getAttributes();
			// 模糊度
			// builder.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
			// WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			// lp.alpha = 0.5f;// 透明度，
			// lp.dimAmount=1.0f;//黑暗度
			// builder.getWindow().setAttributes(lp);
			builder.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private View getView() {
		View view = LayoutInflater.from(context).inflate(R.layout.my_progress_dialog, null);
		TextView textView = (TextView) view.findViewById(R.id.my_progress_dialog_text);
		progressBar = (ProgressBar) view.findViewById(R.id.my_progress_dialog);
		if (Util.isEmpty(text)) {
			textView.setText(text);
		} else
			textView.setVisibility(View.GONE);
		return view;
	}

	public void cancel() {
		try {
			if (builder != null && builder.isShowing())
				builder.dismiss();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

}
