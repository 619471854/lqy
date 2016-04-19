package com.lqy.abook.tool;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.lqy.abook.MyApp;

public class WebViewParser {
	/**
	 * 以下是通过创建webview来获取html
	 * 
	 * @param url网页的url
	 * @param searchJs用于执行搜索操作的js
	 * @return 如果searchJs为空则直接返回当前的html，否则先执行searchJs再返回执行后的html
	 */
	public static String getSearchResult(String url, String searchJs) {
		return new WebViewParser().getHtml(url, searchJs);
	}

	private boolean isParsering = false;

	private String getHtml(String url, String searchJs) {
		if (Util.isEmpty(url)) {
			return null;
		}
		isParsering = true;
		result = null;
		// 转到ui线程里加载webview获取html
		MyApp app = MyApp.getInstance();
		int what = app.addHandlerCallbacka(listener);
		app.sendMsg(what, new String[] { url, searchJs });
		// 等待获取成功
		while (isParsering) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				isParsering = false;
			}
		}
		String re = result;
		result = null;
		MyLog.i("getHtml result");
		return re;
	}

	private final CallBackListener listener = new CallBackListener() {

		@Override
		public void callBack(String... params) {
			// 在ui线程里执行
			if (params != null && params.length == 2) {
				try {
					WebView webView = createFloatView();
					setWebView(webView, params[0], params[1]);
				} catch (Exception e) {
					isParsering = false;
					result = null;
				}
			}
		}
	};
	private String result = null;
	private boolean isLoadingHtml = false;

	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void setWebView(WebView webView, String url, final String searchJs) {
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new InJavaScriptLocalObj(), "local_obj");
		webSettings.setSupportZoom(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);

		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webView.loadUrl(url);
		webView.setWebViewClient(new WebViewClient() {
			private boolean isfirst = true;

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				// MyLog.i("onPageStarted " + url);
				isLoadingHtml = true;
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				// MyLog.i("shouldOverrideUrlLoading " + url);
				return true;
			}

			@Override
			public void onPageFinished(final WebView view, String url) {
				super.onPageFinished(view, url);
				isLoadingHtml = false;
				// MyLog.i("onPageFinished " + url + "  " + isfirst);
				if (Util.isEmpty(searchJs) || !isfirst) {
					// 执行js,获取当前的html
					view.loadUrl("javascript:window.local_obj.showSource('" + url + "',document.getElementsByTagName('html')[0].innerHTML);");
				} else if (isfirst) {
					isfirst = false;
					// 执行js,执行搜索
					view.loadUrl(searchJs);
				}
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				// MyLog.i("onPageFinished");
				isParsering = false;
				result = null;
				remove();
			}

		});
	}

	class InJavaScriptLocalObj {
		@JavascriptInterface
		public void showSource(String url, String html) {
			// MyLog.i("showSource " + isLoading + " " + url);
			if (!isLoadingHtml) {
				isParsering = false;
				result = url + "===" + html;
				remove();
			}
		}
	}

	// 定义浮动窗口布局
	FrameLayout parent;
	// 创建浮动窗口设置布局参数的对象
	WindowManager mWindowManager;

	private void remove() {
		if (parent != null) {
			try {
				mWindowManager.removeView(parent);
			} catch (Exception e) {
				MyLog.e(e);
			}
			parent = null;
		}
		mWindowManager = null;
	}

	private WebView createFloatView() {
		MyApp myApp = MyApp.getInstance();
		WebView webView = new WebView(myApp);
		if (mWindowManager != null || parent == null) {
			// 获取的是WindowManagerImpl.CompatModeWrapper
			parent = new FrameLayout(myApp);
			mWindowManager = (WindowManager) myApp.getSystemService(myApp.WINDOW_SERVICE);
			LayoutParams wmParams = new WindowManager.LayoutParams();
			// 设置window type
			wmParams.type = LayoutParams.TYPE_PHONE;
			// 设置图片格式，效果为背景透明
			wmParams.format = PixelFormat.RGBA_8888;
			// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
			wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
			// 调整悬浮窗显示的停靠位置为左侧置顶
			wmParams.gravity = Gravity.LEFT | Gravity.TOP;
			// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
			wmParams.x = 0;
			wmParams.y = 0;

			// 设置悬浮窗口长宽数据
			wmParams.width = 0;
			wmParams.height = 0;

			// 获取浮动窗口视图所在布局
			mWindowManager.addView(parent, wmParams);
		}
		parent.addView(webView);
		return webView;
	}
}
