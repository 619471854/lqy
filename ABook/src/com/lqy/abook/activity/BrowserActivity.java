package com.lqy.abook.activity;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.db.FavoriteDao;
import com.lqy.abook.db.HistoryDao;
import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.MyWebChromeClient;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.MyAlertDialog;

public class BrowserActivity extends MenuActivity {
	private static final int WHAT_INIT = 0;
	private static final int WHAT_HISTORY = 1;
	private static final int WHAT_OVERRIDEURL = 2;
	private static final int WHAT_SAVEBOOK1 = 3;
	private static final int WHAT_SAVEBOOK2 = 4;
	private static final int WHAT_SAVEBOOK3 = 5;

	private int historyLength = 0;// 历史网页数量
	private int backClickCount = 0;// 点击后退按钮次数
	private HistoryDao dao = new HistoryDao();
	public static boolean changeInterceptPic = false;// 改变了是否禁止加载图片的设置
	public static boolean interceptAdvert = false;// 是否禁止过滤广告
	private String host;// 域名

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);

		setWebView();
		init();
	}

	private ImageView btn_more;
	private View lay_top;
	private View lay_bottom;
	private View lay_more;
	private View btn_last;
	private View btn_next;
	private View btn_refresh;
	private WebView webView;
	private EditText view_url;

	private void init() {
		view_url = (EditText) findViewById(R.id.browser_url);
		btn_more = (ImageView) findViewById(R.id.browser_more);
		lay_more = findViewById(R.id.browser_more_lay);
		lay_top = findViewById(R.id.browser_top);
		lay_bottom = findViewById(R.id.browser_bottom);
		btn_last = findViewById(R.id.browser_last);
		btn_next = findViewById(R.id.browser_next);
		btn_refresh = findViewById(R.id.browser_refresh);
		btn_last.setEnabled(false);
		btn_next.setEnabled(false);

		view_url.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				showTitle(null, null);
			}
		});
		view_url.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					sendButtonClick(btn_refresh);
					return true;
				}
				return false;
			}
		});

		// if (true) {
		// loadUrl("http://m.shenmaxiaoshuo.com/ml-45980/");
		// return;
		// }

		Intent intent = getIntent();
		// ReadActivity的查看原网页等
		String title = intent.getStringExtra("title");
		String url = intent.getStringExtra("url");
		// 其它应用的分享操作(android.intent.action.SEND)跳转过来
		String sendText = intent.getStringExtra(Intent.EXTRA_TEXT);
		// 浏览器(android.intent.category.BROWSABLE)跳转过来
		String browserUrl = intent.getDataString();
		if (!Util.isEmpty(url)) {
			loadUrl(title, url);
		} else if (browserUrl != null || sendText != null) {
			// 从外部进来
			url = browserUrl == null ? sendText : browserUrl;
			int index = url.indexOf("http");
			if (index == -1) {
				Util.dialog(_this, "未找到网址");
			} else {
				loadUrl(url.substring(0, index).trim(), url.substring(index).trim());
			}
		} else {
			SharedPreferences sp = getSharedPreferences(CONSTANT.SP_BROWSER, 0);
			boolean useDefaultUrl = sp.getBoolean("useDefaultUrl", false);
			if (useDefaultUrl)
				loadUrl(CONSTANT.defaultTitle, CONSTANT.defaultUrl);
			else
				dao.getLastestHistory(_this, WHAT_INIT);
		}
	}

	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	private void setWebView() {
		webView = (WebView) findViewById(R.id.browser_webview);
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
		webSettings.setUserAgentString(CONSTANT.CHROME_USER_AGENT);
		// 禁止加载图片
		SharedPreferences sp = getSharedPreferences(CONSTANT.SP_BROWSER, 0);
		boolean interceptPic = sp.getBoolean("interceptPic", false);
		webSettings.setBlockNetworkImage(interceptPic);
		changeInterceptPic = false;

		String cacheDirPath = FileUtil.getCachePath();
		// 设置数据库缓存路径
		webSettings.setDatabasePath(cacheDirPath);
		// 设置 Application Caches 缓存目录
		webSettings.setAppCachePath(cacheDirPath);
		// 开启 Application Caches 功能
		webSettings.setAppCacheEnabled(true);

		webView.setWebViewClient(client);
		webView.setWebChromeClient(new MyWebChromeClient(this));
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		switch (what) {
		case WHAT_INIT:
			FavoriteEntity history = (FavoriteEntity) o;
			if (history != null && !Util.isEmpty(history.getUrl()))
				loadUrl(history.getTitle(), history.getUrl());
			else
				loadUrl(CONSTANT.defaultTitle, CONSTANT.defaultUrl);
			break;
		case WHAT_HISTORY:// 加载了页面,获取历史纪录数量,刷新前进后退按钮
			historyLength = arg1;
			int current = historyLength - 1 - backClickCount;// 当前页处于历史纪录的位置
			MyLog.web("setHistoryLength " + historyLength + " current=" + current);
			if (current < historyLength - 1)
				btn_next.setEnabled(true);
			else
				btn_next.setEnabled(false);
			if (current > 0)
				btn_last.setEnabled(true);
			else
				btn_last.setEnabled(false);
			break;
		case WHAT_OVERRIDEURL:// 加载了新的页面,刷新前进后退按钮
			btn_last.setEnabled(true);
			btn_next.setEnabled(false);
			backClickCount = 0;
			break;
		case WHAT_SAVEBOOK1:// 添加到书架
			MyLog.i("savebook1");
			showLoadingDialog("正在获取小说目录");
			String[] params = (String[]) o;
			ParserManager.parserBrowser(_this, webView.getUrl(), params[1], params[0], WHAT_SAVEBOOK2);
			break;
		case WHAT_SAVEBOOK2:// 添加到书架
			MyLog.i("savebook2 ", o);
			saveBook((BookAndChapters) o);
			break;
		case WHAT_SAVEBOOK3:// 添加到书架成功，启动首页
			MyLog.i("savebook3 ", o);
			if (MainActivity.getInstance() == null) {
				startActivity(new Intent(_this, LoadingActivity.class));
				finish();
			} else {
				MainActivity.isAddBook = true;
				Intent intent = new Intent(_this, MainActivity.class);
				intent.putExtra("book", (BookEntity) o);
				startActivity(intent);
				animationRightToLeft();
				finish();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 添加到书架
	 */
	private void saveBook(final BookAndChapters result) {
		if (result == null || result.getResult() == BookAndChapters.SearchResult.Failed) {
			Util.dialog(_this, "获取目录失败");
		} else if (result.getResult() == BookAndChapters.SearchResult.Search) {
			String msg = "不能获取到目录，是否去搜索小说《" + result.getBook().getName() + "》";
			Util.dialog(_this, msg, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(_this, SearchActivity.class);
					intent.putExtra("search", result.getBook().getName() + " " + result.getBook().getAuthor());
					startActivity(intent);
				}
			});
		} else if (result.getResult() == BookAndChapters.SearchResult.InputName) {
			final EditText et = new EditText(_this);
			et.setBackgroundColor(Color.WHITE);
			new MyAlertDialog(_this).setTitle("请输入要添加的书籍名字").setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = et.getText().toString().trim();
					if (!Util.isEmpty(name)) {
						result.getBook().setName(name);
						saveBook(result.getBook(), result.getChapters());
					}
				}
			}).setNegativeButton("取消", null).show();
		} else {
			saveBook(result.getBook(), result.getChapters());
		}
	}

	/**
	 * 添加到书架
	 */
	private void saveBook(final BookEntity book, final List<ChapterEntity> chapters) {
		showLoadingDialog("正在保存小说");
		new Thread() {
			public void run() {
				if (new BookDao().addBook(book)) {
					LoadManager.saveDirectory(book.getId(), chapters);
					book.setUnReadCount(chapters.size());
					sendMsgOnThread(WHAT_SAVEBOOK3, book);
				} else {
					sendErrorOnThread("保存小说失败");
				}
			};
		}.start();
	}

	private WebViewClient client = new WebViewClient() {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (interceptAdvert)
				host = null;
			else
				try {
					host = new URI(url).getHost();
					String[] hosts = host.split("\\.");
					if (host != null && hosts.length == 3)// 只取域名的中间部分
						host = hosts[1];
					if (host.length() == 0)
						host = null;
					else
						MyLog.web("当前网页的域名中间部分为：" + host);
				} catch (URISyntaxException e) {
				}

			MyLog.web("onPageStarted " + url);
			showTitle(url, url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			sendMsgOnThread(WHAT_OVERRIDEURL);
			host = null;
			view.loadUrl(url);
			MyLog.web("shouldOverrideUrlLoading " + url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			String title = view.getTitle();
			MyLog.web("onPageFinished " + title + " " + url);
			showTitle(title, url);
			// 保存历史纪录
			dao.saveHistory(title, url);
			// 获取历史纪录数量
			loadUrl("javascript:window.local_obj.setHistoryLength(history.length);");
		}

		public void onLoadResource(WebView view, String url) {
			// MyLog.web("onLoadResource " + url);
		};

		@SuppressLint("NewApi")
		public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
			// api 11-21
			try {
				if (!interceptAdvert && host != null && !url.contains(host)) {
					MyLog.web("shouldInterceptRequest1 forbid " + url);
					return new WebResourceResponse("image/png", "UTF-8", null);
				}
			} catch (Exception e) {
			}
			return super.shouldInterceptRequest(view, url);
		}

		@SuppressLint("NewApi")
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

			try {
				String url = request.getUrl().toString();
				if (!interceptAdvert && host != null && !url.contains(host)) {
					MyLog.web("shouldInterceptRequest2 forbid " + url);
					return new WebResourceResponse("image/png", "UTF-8", null);
				}
			} catch (Exception e) {
			}
			return super.shouldInterceptRequest(view, request);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			MyLog.web("onReceivedError");
			// 获取历史纪录数量
			loadUrl("javascript:window.local_obj.setHistoryLength(history.length);");
		}
	};

	class InJavaScriptLocalObj {
		@JavascriptInterface
		public void showSource(String html) {
			MyLog.web("showSource " + html);
			// MyLog.save(html);
		}

		@JavascriptInterface
		public void saveBook(String cookie, String html) {
			MyLog.save(html);
			sendMsgOnThread(WHAT_SAVEBOOK1, new String[] { cookie, html });
		}

		@JavascriptInterface
		public void setHistoryLength(int length) {
			sendMsgOnThread(WHAT_HISTORY, length, null);
		}
	}

	private void loadUrl(String title, String url) {
		if (Util.isEmpty(url))
			return;
		if (!view_url.isFocused()) {
			if (Util.isEmpty(title))
				title = "无标题";
			view_url.setText(title);
		}
		loadUrl(url);
	}

	private void loadUrl(String url) {
		MyLog.web("loadUrl " + url);
		try {
			if (url.startsWith("http")) {
				backClickCount = 0;
				// 加载了新的网页,关闭前进按钮
				if (historyLength != 0)// 0表示第一次加载
					btn_last.setEnabled(true);
				btn_next.setEnabled(false);

				host = null;
			}
			webView.loadUrl(url);
		} catch (Exception e) {
		}
	}

	private void showTitle(String title, String url) {
		if (view_url.isFocused()) {
			if (url == null)
				url = webView.getUrl();
			if (Util.isEmpty(url))
				url = CONSTANT.defaultUrl;
			view_url.setText(url);
			view_url.setSelection(view_url.getText().toString().length());
		} else {
			if (title == null)
				title = webView.getTitle();
			if (Util.isEmpty(title))
				title = "无标题";
			view_url.setText(title);
		}
	}

	private void getHtml(String cb) {
		if (Util.isEmpty(cb))
			loadUrl("javascript:window.local_obj.showSource(document.getElementsByTagName('html')[0].innerHTML);");
		else
			loadUrl("javascript:window.local_obj." + cb + "(document.getElementsByTagName('html')[0].innerHTML);");
	}

	public void sendButtonClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.browser_full:// 全屏
			lay_top.setVisibility(View.GONE);
			lay_bottom.setVisibility(View.GONE);
			hideMoreLay();
			Util.toast(_this, "按“Home”除外的任何实体按键可以显示菜单项");
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			break;
		case R.id.browser_refresh:// 刷新
			if (view_url.isFocused()) {
				String value = view_url.getText().toString();
				if (Util.matchWebSite(value)) {
					if (!value.startsWith("http://") && !value.startsWith("https://"))
						value = "http://" + value;
					loadUrl(value);
				} else {
					loadUrl("https://www.baidu.com/s?wd=" + URLEncoder.encode(value));
				}
			} else {
				loadUrl(webView.getUrl());
			}
			break;
		case R.id.browser_last:// 后退
			backClickCount++;
			btn_next.setEnabled(true);
			loadUrl("javascript:history.back();");
			break;
		case R.id.browser_next:// 前进
			if (backClickCount > 0)
				backClickCount--;
			btn_last.setEnabled(true);
			loadUrl("javascript:history.forward();");
			break;
		case R.id.browser_save:// 加入书架
			Util.dialog(_this, "请确认目前显示的是小说目录页。\n(部分网页不能获取到小说目录，只能在线看小说)", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Intent intent = new Intent(_this, LoadingActivity.class);
					// intent.putExtra("title", webView.getTitle());
					// intent.putExtra("url", webView.getUrl());
					// startActivity(intent);
					// saveBook()
					// getHtml("saveBook");
					loadUrl("javascript:window.local_obj.saveBook(document.cookie,document.getElementsByTagName('html')[0].innerHTML);");
				}
			});
			break;
		case R.id.browser_favorite_add:// 加入收藏夹
			boolean re = new FavoriteDao().saveFavorite(webView.getTitle(), webView.getUrl());
			Util.dialog(_this, re ? "收藏成功" : "收藏失败");
			break;
		case R.id.browser_more:// 更多
			if (lay_more.getVisibility() == View.VISIBLE) {
				hideMoreLay();
			} else {
				lay_more.setVisibility(View.VISIBLE);
				btn_more.setImageResource(R.drawable.more_clicked);
			}
			break;
		case R.id.browser_recommendation:// 推荐
			hideMoreLay();
			intent = new Intent(_this, BrowserFavoriteActivity.class);
			intent.putExtra("index", 0);
			startActivityForResult(intent, R.id.browser_history);
			animationRightToLeft();
			break;
		case R.id.browser_favorite:// 收藏夹
			hideMoreLay();
			intent = new Intent(_this, BrowserFavoriteActivity.class);
			intent.putExtra("index", 1);
			startActivityForResult(intent, R.id.browser_favorite);
			animationRightToLeft();
			break;
		case R.id.browser_history:// 历史纪录
			hideMoreLay();
			startActivityForResult(new Intent(_this, BrowserHistoryActivity.class), R.id.browser_history);
			animationRightToLeft();
			break;
		case R.id.browser_share:// 分享
			hideMoreLay();
			String url = webView.getUrl();
			if (Util.isEmpty(url)) {
				Util.dialog(_this, "未找到网址");
				return;
			}
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
			intent.putExtra(Intent.EXTRA_TEXT, url);
			startActivity(Intent.createChooser(intent, "分享到"));
			break;
		case R.id.browser_set:// 设置
			startActivity(new Intent(_this, BrowserSetActivity.class));
			animationRightToLeft();
			hideMoreLay();
			break;
		case R.id.browser_exit:// 退出
			hideMoreLay();
			cancelButtonClick(null);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		if (changeInterceptPic) {
			try {
				SharedPreferences sp = getSharedPreferences(CONSTANT.SP_BROWSER, 0);
				boolean interceptPic = sp.getBoolean("interceptPic", false);
				webView.getSettings().setBlockNetworkImage(interceptPic);
			} catch (Exception e) {
			}
			changeInterceptPic = false;
		}
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case R.id.browser_favorite:
		case R.id.browser_history:
			if (resultCode == RESULT_OK) {
				loadUrl(data.getStringExtra("title"), data.getStringExtra("url"));
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 更多菜单
	 */
	private void hideMoreLay() {
		lay_more.setVisibility(View.GONE);
		btn_more.setImageResource(R.drawable.more_nor);
	}

	/**
	 * 返回键处理
	 */
	private long clickBackTime = 0;


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (lay_top.getVisibility() == View.GONE) {
			MyLog.i("onKeyUp 显示菜单");
			// 按任何键可以显示菜单
			lay_top.setVisibility(View.VISIBLE);
			lay_bottom.setVisibility(View.VISIBLE);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (lay_more.getVisibility() == View.VISIBLE) {
				hideMoreLay();
			} else {
				long current = System.currentTimeMillis();
				if (current - clickBackTime > 2000) {
					clickBackTime = current;
					handler.postDelayed(back, 200);
				} else {
					handler.removeCallbacks(back);
					cancelButtonClick(null);
				}
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	Runnable back = new Runnable() {

		@Override
		public void run() {
			Util.toast(_this, "再点击一次退出此界面");
		}
	};
}
