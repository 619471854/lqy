package com.lqy.abook.tool;

import android.content.DialogInterface;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.lqy.abook.R;
import com.lqy.abook.activity.BrowserActivity;
import com.lqy.abook.widget.MyAlertDialog;

public class MyWebChromeClient extends WebChromeClient {
	private BrowserActivity activity;
	private ProgressBar progressBar;

	public MyWebChromeClient(BrowserActivity con) {
		this.activity = con;
		progressBar = (ProgressBar) con.findViewById(R.id.browser_progress);
	}

	// @Override
	// public void onExceededDatabaseQuota(String url, String
	// databaseIdentifier,
	// long quota, long estimatedDatabaseSize, long totalQuota,
	// QuotaUpdater quotaUpdater) {// localStorage扩充数据库的容量
	// quotaUpdater.updateQuota(estimatedDatabaseSize * 10);
	// super.onExceededDatabaseQuota(url, databaseIdentifier, quota,
	// estimatedDatabaseSize, totalQuota, quotaUpdater);
	// }

	@Override
	public void onProgressChanged(WebView view, int progress) {
		// 加载进度
		if (progress == 100) {
			progressBar.setVisibility(View.GONE);
		} else {
			progressBar.setProgress(progress);
			if (progressBar.getVisibility() == View.GONE)
				progressBar.setVisibility(View.VISIBLE);
		}
		// MyLog.web("onProgressChanged " + progress);
	}

	@Override
	public void onReceivedTitle(WebView view, String title) {
		MyLog.web("onReceivedTitle=" + title);
		super.onReceivedTitle(view, title);
	}

	@Override
	public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
		MyLog.web("WebChromeClient_onJsAlert=" + message);
		Util.dialog(activity, message);
		result.confirm();
		return true;
	}

	@Override
	public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
		if (activity != null && !activity.isFinishing())
			new MyAlertDialog(activity).setTitle("系统提示").setMessage(message).setCancelable(false)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							result.confirm();
						}
					}).setNeutralButton("取消", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							result.cancel();
						}
					}).show();
		return true;
	}
}
