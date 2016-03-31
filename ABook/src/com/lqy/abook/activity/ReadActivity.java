package com.lqy.abook.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sf.hmg.turntest.PageWidget;
import sf.hmg.turntest.TurnUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.FontMode;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.load.AsyncTxtLoader;
import com.lqy.abook.load.Cache;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.CallBackListener;
import com.lqy.abook.tool.LightUtils;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;

public class ReadActivity extends MenuActivity {
	private TurnUtil turnUtil;
	private ChapterEntity chapter;
	private BookEntity book;
	private int textSize = 16;// 字体
	private FontMode mode;
	private int light;// 设置的亮度
	private boolean isSystemLight = true;// 是当前手机亮度还是设置的亮度
	private SharedPreferences sp;
	private static ReadActivity instance;
	private BookDao dao = new BookDao();

	public static ReadActivity getInstance() {
		if (instance == null || instance.isFinishing())
			return null;
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
		instance = this;
		init();
		showInfo();
		asynGetData();
	}

	private void init() {
		book = Cache.getBook();
		if (book == null) {
			finish();
			return;
		}
		chapter = Cache.getCurrentChapter();

		// 设置字体等
		sp = getSharedPreferences(CONSTANT.SP_READ, 0);
		textSize = sp.getInt("textSize", textSize);
		mode = FontMode.valueOf(sp);
		isSystemLight = sp.getBoolean("isSystemLight", true);
		light = sp.getInt("light", light);
		light = Math.min(light, CONSTANT.screen_light_max);
		if (light < CONSTANT.screen_light_min) {
			isSystemLight = true;
			light = CONSTANT.screen_light_min;
		}
		if (!isSystemLight) {
			LightUtils.setScreenBrightness(_this, light);
		}

		PageWidget mPageWidget = (PageWidget) findViewById(R.id.read_content);
		turnUtil = new TurnUtil(this, mPageWidget, mode, textSize);

		turnUtil.setMenuListener(new CallBackListener() {
			@Override
			public void callBack(String... params) {
				MyLog.i("menu click");
				Intent intent = new Intent(_this, ReadMenuActivity.class);
				intent.putExtra("textSize", textSize);
				intent.putExtra("fontMode", mode);
				intent.putExtra("isSystemLight", isSystemLight);
				intent.putExtra("light", light);
				startActivity(intent);
				// overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
			}
		});
	}

	public void updateReadLoation(int readBegin) {
		book.setReadBegin(readBegin);
		dao.updateReadLoation(book.getId(), book.getCurrentChapterId(), readBegin);
	}

	private void asynGetData() {
		if (!NetworkUtils.isNetConnectedRefreshWhereNot()) {
			Util.toast(_this, R.string.net_not_connected);
		}
		updateReadLoation(book.getReadBegin());
		if (chapter != null && chapter.isVip()) {
			// 此章是VIP章节，请换源下载
			turnUtil.showChapterText(chapter, book.getId(), 0);
			showInfo();
			return;
		}
		new Thread() {
			public void run() {
				if (chapter == null) {// 获取章节
					List<ChapterEntity> chapters = LoadManager.getDirectory(book.getId());
					if (chapters == null || chapters.size() == 0) {
						chapters = ParserManager.getDict(book);
					}
					if (chapters == null || chapters.size() == 0) {
						// 获取目录失败，请换源下载
						if (NetworkUtils.isNetConnected(null))
							book.setLoadStatus(LoadStatus.failed);
						sendMsgOnThread(4, null);
					} else {
						if (book.getLoadStatus() == LoadStatus.failed) {
							book.setLoadStatus(LoadStatus.notLoaded);
						}
						sendMsgOnThread(0, chapters);
					}
				} else {// 从本地获取
					String text = LoadManager.getChapterContent(book.getId(), chapter.getName());
					if (Util.isEmpty(text)) {
						chapter.setLoadStatus(LoadStatus.failed);
						sendMsgOnThread(2, null);
					} else {
						chapter.setLoadStatus(LoadStatus.completed);
						sendMsgOnThread(1, text);
					}
				}
			};
		}.start();
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		switch (what) {
		case 0:// 获取章节成功
			Cache.setChapters((ArrayList<ChapterEntity>) o);
			LoadManager.asynSaveDirectory(book.getId(), Cache.getChapters());
			Cache.setCurrentChapterAfterChangeSite();
			chapter = Cache.getCurrentChapter();

			asynGetData();
			break;
		case 1:// 获取章节内容成功
			updateText();
			break;
		case 2:// 下载
				// chapter = Cache.getCurrentChapter();
			List<ChapterEntity> chapters = Cache.getChapters();
			if (chapters != null) {// 不会为空，否则chapter不存在，不会进入这里
				// 下载本章
				AsyncTxtLoader.getInstance().loadCurrentChapter(_this, book, Cache.getCurrentChapter(), 1, false);
				// 下载后续章节
				if (!AsyncTxtLoader.isRunning(book.getId())) {
					int current = Cache.getCurrentChapterIndex();
					int limit = current + CONSTANT.auto_load_size;
					AsyncTxtLoader.getInstance().load(this, book, chapters, 5, current + 1, limit);
				}
			}
			break;
		case 3:// 切换网站
			hideLoadingDialog();
			asynGetData();
		case 4: // 获取目录失败，请换源下载
			turnUtil.showChapterText(null, book.getId(), 0);
		default:

			break;
		}
	}

	/**
	 * 更新内容
	 */
	public void updateText() {
		turnUtil.showChapterText(chapter, book.getId(), book.getReadBegin());
		showInfo();
	}

	@Override
	protected void dealErrorMsg(int what, Object o) {
		// TODO Auto-generated method stub
		super.dealErrorMsg(what, o);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// 目录或者切换下载地址
		String formClass = intent.getStringExtra("class");
		if (DirectoryActivity.class.getName().equals(formClass)) {
			if (chapter == null || chapter.getId() != Cache.getCurrentChapterIndex()) {
				changeChapter();// 切换章节
			}
		} else if (SiteSwitchActivity.class.getName().equals(formClass)) {
			// 切换网站
			BookEntity otherSiteBook = (BookEntity) intent.getSerializableExtra("book");
			if (otherSiteBook != null && book.getId() != CONSTANT.ID_DEFAULT) {
				book.setCover(otherSiteBook.getCover());
				book.setType(otherSiteBook.getType());
				book.setSite(otherSiteBook.getSite());
				book.setDetailUrl(otherSiteBook.getDetailUrl());
				book.setDirectoryUrl(otherSiteBook.getDirectoryUrl());
				book.setNewChapter(otherSiteBook.getNewChapter());
				book.setUpdateTime(otherSiteBook.getUpdateTime());

				ChapterEntity c = Cache.getCurrentChapter();
				final ArrayList<ChapterEntity> chapters = Cache.getChapters();
				Cache.setBook(otherSiteBook);
				chapter = null;
				showLoadingDialog(null);
				new Thread() {
					public void run() {
						dao.updateBook(book);
						// 删除目录
						String bookPath = FileUtil.getBooksPath(book.getId());
						File file = new File(bookPath, FileUtil.BOOK_INDEX_NAME);
						if (file.exists()) {
							file.delete();
						}
						// 删除所有章节的缓存
						if (chapters != null)
							try {
								for (ChapterEntity _chapter : chapters) {
									file = new File(bookPath, FileUtil.getChapterName(_chapter.getName()));
									if (file.exists()) {
										file.delete();
									}
								}
							} catch (Exception e) {
							}
						sendMsgOnThread(3, null);
					}
				}.start();
			}
		}
		super.onNewIntent(intent);
	}

	/**
	 * 显示电量,时间章节信息等
	 */
	private TextView view_time;
	private TextView view_battery;
	private BatteryReceiver batteryReceiver;
	private TextView view_chapterTitle;
	private TextView view_chapterNum;

	// private ProgressBar view_progress;

	class BatteryReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				try {
					int level = intent.getIntExtra("level", 0);// 获取当前电量
					int scale = intent.getIntExtra("scale", 100); // 电量的总刻度
					int status = intent.getIntExtra("status", -1); // 状态
					if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
						view_battery.setText("充电中" + ((level * 100) / scale) + "%");
					} else {
						view_battery.setText("电量" + ((level * 100) / scale) + "%");
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

	}

	private void showInfo() {
		if (view_time == null) {
			view_time = (TextView) findViewById(R.id.read_time);
			view_battery = (TextView) findViewById(R.id.read_battery);
			view_chapterTitle = (TextView) findViewById(R.id.read_title);
			view_chapterNum = (TextView) findViewById(R.id.read_num);
			// view_progress = (ProgressBar) findViewById(R.id.read_progress);
			// 监听电量
			IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			batteryReceiver = new BatteryReceiver();
			registerReceiver(batteryReceiver, intentFilter);
		}
		Calendar cal = Calendar.getInstance();
		view_time.setText(String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
		if (chapter != null) {
			view_chapterTitle.setText(chapter.getName());
			view_chapterNum.setText(Cache.getCurrentChapterIndexString());
		}
	}

	@Override
	protected void onDestroy() {
		instance = null;
		if (batteryReceiver != null)
			unregisterReceiver(batteryReceiver);
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("book", Cache.getBook());
		if (Cache.exitChapters())
			outState.putSerializable("chapters", Cache.getChapters());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		book = (BookEntity) savedInstanceState.get("book");
		ArrayList<ChapterEntity> chapters = (ArrayList<ChapterEntity>) savedInstanceState.get("chapters");
		if (book == null) {
			finish();
			return;
		}
		Cache.setBook(book);
		Cache.setChapters(chapters);
		asynGetData();
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * 切换章节后
	 */
	public void changeChapter() {
		chapter = Cache.getCurrentChapter();
		book.setReadBegin(0);
		asynGetData();
	}

	/**
	 * 更新目录后
	 */
	public void refreshChapter() {
		chapter = Cache.getCurrentChapter();
	}

	public void setTextSize(boolean isAdd) {
		if (isAdd) {
			textSize = Math.min(textSize + 2, 72);
		} else {
			textSize = Math.max(textSize - 2, 10);
		}
		sp.edit().putInt("textSize", textSize).commit();
		turnUtil.setFontSize(textSize);
	}

	public void setFontMode(FontMode mode) {
		if (mode != null && mode.type == FontMode.Type.custom || mode.type != this.mode.type) {
			this.mode = mode;
			mode.save(sp);
			turnUtil.setFontMode(mode);
		}
	}

	public void setLight(int _light) {
		setLightNotSave(_light, false);
		sp.edit().putBoolean("isSystemLight", isSystemLight).putInt("light", light).commit();
	}

	public void setLightNotSave(int _light, boolean _isSystemLight) {
		light = _light;
		isSystemLight = _isSystemLight;
		if (!isSystemLight) {
			LightUtils.setScreenBrightness(_this, light);
		}
	}

	public void setLight(boolean _isSystemLight) {
		isSystemLight = _isSystemLight;
		sp.edit().putBoolean("isSystemLight", isSystemLight).putInt("light", light).commit();
		if (isSystemLight) {
			LightUtils.setScreenBrightness(_this, -1);
		} else {
			LightUtils.setScreenBrightness(_this, light);
		}
	}
}
