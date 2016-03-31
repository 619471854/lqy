package com.lqy.abook;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;

public class MenuActivity extends Activity {
	protected MenuActivity _this;
	private final String className = getClass().getName();
	protected TextView view_hint;// 显示提示

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_this = this;
		MyLog.i_base(className + "  onCreate");
		GlobalConfig.setCurrentActivity(this);
		// 如果没有网络，则重新检查一下
		if (!NetworkUtils.isNetConnected(null))
			NetworkUtils.refreshNetState(_this);

		// MyLog.i("AsyncTxtLoader.instance  " + (AsyncTxtLoader.instance ==
		// null));
	}

	@Override
	protected void onResume() {
		MyLog.i_base(className + "  onResume");
		GlobalConfig.setCurrentActivity(this);
		super.onResume();
		if (!CONSTANT.isDebug)
			StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		MyLog.i_base(className + "  onPause");
		super.onPause();
		if (!CONSTANT.isDebug)
			StatService.onPause(this);
	}

	@Override
	protected void onStop() {
		MyLog.i_base(className + "  onStop");
		super.onStop();
	}

	@Override
	protected void onStart() {
		MyLog.i_base(className + "  onStart");
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
			handler = null;
		}
		MyLog.i_base(className + "  onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		MyLog.i_base(className + "  onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		MyLog.i_base(className + "  onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * 返回上个界面
	 */
	public void cancelButtonClick(View v) {
		finish();
		animationLeftToRight();
	}

	public void sendButtonClick(View v) {
	}

	// 返回键处理
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public void animationRightToLeft() {
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

	}

	public void animationLeftToRight() {
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}

	/**
	 * 显示加载提示悬浮框
	 */
	private View loadView;

	public void showProgressBar() {
		if (loadView == null)
			loadView = findViewById(R.id.loading_view);
		if (loadView != null) {
			loadView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 隐藏加载提示悬浮框
	 */
	protected boolean notHideProgress = false;

	public void hideProgressBar() {
		if (!notHideProgress) {
			if (loadView == null)
				loadView = findViewById(R.id.loading_view);
			if (loadView != null)
				loadView.setVisibility(View.GONE);
			hideLoadingDialog();
		}
	}

	/**
	 * 显示正在加载的对话框
	 */
	private ProgressDialog dialog;

	public void showLoadingDialog(String msg) {
		if (!isFinishing()) {
			dialog = new ProgressDialog(this);
			dialog.setMessage(msg);
			dialog.setCancelable(true);
			dialog.show();
		}
	}

	public void hideLoadingDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.cancel();
			dialog = null;
		}
	}

	public void sendErrorOnThread(String error) {
		sendMsgOnThread(CONSTANT.MSG_ERROR, NetworkUtils.isNetConnected(null) ? error : "找不到网络");
	}

	public void sendMsgOnThread(int what) {
		sendMsgOnThread(what, 0, null);
	}

	public void sendMsgOnThread(int what, Object obj) {
		sendMsgOnThread(what, 0, obj);
	}

	public void sendMsgOnThread(int what, int arg1, Object obj) {
		if (!isFinishing()) {
			handler.obtainMessage(what, arg1, arg1, obj).sendToTarget();
		}
	}

	protected Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			try {
				if (!isFinishing()) {
					hideProgressBar();
					if (msg.what == CONSTANT.MSG_ERROR)
						dealErrorMsg(msg.what, msg.obj);
					else
						dealMsg(msg.what, msg.arg1, msg.obj);
				}
			} catch (Exception e) {
			}
		};
	};

	protected void dealMsg(int what, int arg1, Object o) {
	}

	protected void dealErrorMsg(int what, Object o) {
		if (view_hint != null) {
			view_hint.setText(Util.toString(o));
			view_hint.setVisibility(View.VISIBLE);
		} else
			Util.dialog(_this, o.toString());
	}

	/**
	 * onActivityResult 回调
	 */
	private Map<Integer, onActivityResult> onActivityResultData;

	public void addActivityResultData(int resultCode, onActivityResult listener) {
		if (listener != null) {
			if (onActivityResultData == null)
				onActivityResultData = new HashMap<Integer, onActivityResult>();
			onActivityResultData.put(resultCode, listener);
		}
	}

	public boolean hasResultCode(int requestCode) {
		return onActivityResultData != null && onActivityResultData.get(requestCode) != null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (onActivityResultData != null && onActivityResultData.containsKey(requestCode))
			onActivityResultData.get(requestCode).callBack(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static interface onActivityResult {
		public void callBack(int requestCode, int resultCode, Intent data);
	}
}
