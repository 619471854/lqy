package com.lqy.abook.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ToggleButton;

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
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;
import com.lqy.abook.tool.VoiceUtils;
import com.lqy.abook.widget.FontRadioButton;
import com.lqy.abook.widget.MySwitch;

public class ReadMenuActivity extends MenuActivity {

	private VoiceUtils voiceUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_menu);

		findView();
		init();
		voiceUtils = new VoiceUtils(this);
	}

	private LinearLayout menuLay_update;
	private LinearLayout menuLay_bg;
	private LinearLayout menuLay_font;
	private LinearLayout menuLay_more;
	private LinearLayout menuLay_voice;
	private LinearLayout menuLay_bottom;
	private LinearLayout menuLay_top;
	private View menuLay;
	private CheckBox menu_update;
	private ImageView menu_stop;
	private CheckBox menu_bg;
	private CheckBox menu_font;
	private MySwitch mySwitch;// 是否系统亮度
	private SeekBar seekbar_font;
	private ToggleButton btn_voicePause;
	private SeekBar seekbar_voice;
	private View btn_voice;
	private View btn_last;
	private View btn_next;
	private View btn_cancel;
	private View btn_dir;

	private void findView() {
		btn_dir = findViewById(R.id.read_menu_directory);
		btn_cancel = findViewById(R.id.read_menu_cancel);
		btn_last = findViewById(R.id.read_menu_last);
		btn_next = findViewById(R.id.read_menu_next);
		btn_last.setEnabled(Cache.hasLastChapter());
		btn_next.setEnabled(Cache.hasNextChapter());

		menuLay_update = (LinearLayout) findViewById(R.id.read_menu_update_lay);
		menuLay_bg = (LinearLayout) findViewById(R.id.read_menu_light_lay);
		menuLay_font = (LinearLayout) findViewById(R.id.read_menu_font_lay);
		menuLay_more = (LinearLayout) findViewById(R.id.read_menu_more_lay);
		menuLay_voice = (LinearLayout) findViewById(R.id.read_menu_voice_lay);
		menuLay_bottom = (LinearLayout) findViewById(R.id.read_menu_bottom_lay);
		menuLay_top = (LinearLayout) findViewById(R.id.read_menu_top_lay);

		menuLay_update.setVisibility(View.GONE);
		menuLay_bg.setVisibility(View.GONE);
		menuLay_font.setVisibility(View.GONE);
		menuLay_more.setVisibility(View.GONE);
		menuLay_voice.setVisibility(View.GONE);

		menu_update = (CheckBox) findViewById(R.id.read_menu_update);
		menu_stop = (ImageView) findViewById(R.id.read_menu_stop);
		menu_bg = (CheckBox) findViewById(R.id.read_menu_bg);
		menu_font = (CheckBox) findViewById(R.id.read_menu_font);
		mySwitch = (MySwitch) findViewById(R.id.read_menu_light_check);
		seekbar_font = (SeekBar) findViewById(R.id.read_menu_light_seek);
		btn_voice = findViewById(R.id.read_menu_voice);
		btn_voicePause = (ToggleButton) findViewById(R.id.read_voice_pause);
		seekbar_voice = (SeekBar) findViewById(R.id.read_voice_speed);
	}

	private void init() {
		// 获取初始值
		FontMode mode = null;
		int light = 0;// 亮度，默认系统亮度
		boolean isSystemLight = true;// 是否是系统亮度
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mode = (FontMode) bundle.get("fontMode");
			isSystemLight = bundle.getBoolean("isSystemLight");
			light = bundle.getInt("light");
		}
		if (mode == null)
			mode = FontMode.getDefault();

		// 选择当前的背景颜色设置按钮
		RadioGroup fontModeRg = (RadioGroup) findViewById(R.id.read_menu_mode_rg);
		fontModeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				ReadActivity a = ReadActivity.getInstance();
				if (a != null)
					switch (checkedId) {
					case R.id.read_menu_mode_white:
						a.setFontMode(FontMode.valueOf(FontMode.Type.white, 0, 0));
						break;
					case R.id.read_menu_mode_brown:
						a.setFontMode(FontMode.valueOf(FontMode.Type.brown, 0, 0));
						break;
					case R.id.read_menu_mode_green:
						a.setFontMode(FontMode.valueOf(FontMode.Type.green, 0, 0));
						break;
					case R.id.read_menu_mode_custom:
						FontRadioButton btn = (FontRadioButton) findViewById(checkedId);
						if (btn != null)
							a.setFontMode(btn.getMode());
						break;
					}
			}
		});
		switch (mode.type) {
		case white:
			fontModeRg.check(R.id.read_menu_mode_white);
			break;
		case brown:
			fontModeRg.check(R.id.read_menu_mode_brown);
			break;
		case green:
			fontModeRg.check(R.id.read_menu_mode_green);
			break;
		case custom:
			fontModeRg.check(R.id.read_menu_mode_custom);
			break;
		}
		// 亮度
		seekbar_font.setMax(CONSTANT.screen_light_max - CONSTANT.screen_light_min);
		mySwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ReadActivity.getInstance() != null)
					ReadActivity.getInstance().setLight(mySwitch.isChecked());
			}
		});
		seekbar_font.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (ReadActivity.getInstance() != null)
					ReadActivity.getInstance().setLight(seekbar_font.getProgress() + CONSTANT.screen_light_min);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				mySwitch.setChecked(false);
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (ReadActivity.getInstance() != null)
					ReadActivity.getInstance().setLightNotSave(seekbar_font.getProgress() + CONSTANT.screen_light_min, mySwitch.isChecked());
			}
		});
		if (isSystemLight) {
			mySwitch.setChecked(true);
		} else
			seekbar_font.setProgress(light);
		if (AsyncTxtLoader.isRunning(Cache.getBook().getId())) {
			menu_update.setVisibility(View.GONE);
			menu_stop.setVisibility(View.VISIBLE);
		} else {
			menu_update.setVisibility(View.VISIBLE);
			menu_stop.setVisibility(View.GONE);
		}
		// 语音
		seekbar_voice.setMax(100);
		seekbar_voice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				voiceUtils.setVoiceSpeed(seekbar_voice.getProgress());
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});
		SharedPreferences sp = getSharedPreferences(CONSTANT.SP_READ, 0);
		seekbar_voice.setProgress(sp.getInt("speed", CONSTANT.voice_speed));

		btn_voice.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				voiceUtils.onlongClick(startVoiceCb);
				return true;
			}
		});
	}

	public void sendButtonClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.read_menu_update:
			if (!NetworkUtils.isNetConnectedRefreshWhereNot())
				Util.toast(_this, R.string.net_not_connected);
			else
				showMenuLay(menuLay_update);
			break;
		case R.id.read_menu_stop:
			menu_stop.setVisibility(View.GONE);
			menu_update.setVisibility(View.VISIBLE);
			AsyncTxtLoader.stopLoadBook(Cache.getBook().getId());
			MainActivity.setLoadingOver();

			if (Cache.getBook().getLoadStatus() == LoadStatus.loading)
				Cache.getBook().setLoadStatus(LoadStatus.notLoaded);
			if (Cache.getChapters() != null)
				for (ChapterEntity e : Cache.getChapters()) {
					if (e.getLoadStatus() == LoadStatus.loading) {
						e.setLoadStatus(LoadStatus.notLoaded);
					}
				}
			break;
		case R.id.read_menu_bg:
			showMenuLay(menuLay_bg);
			break;
		case R.id.read_menu_font:
			showMenuLay(menuLay_font);
			break;
		case R.id.read_menu_more:
			showMenuLay(menuLay_more);
			break;
		case R.id.read_menu_cancel:
			cancelButtonClick(v);
			if (ReadActivity.getInstance() != null)
				ReadActivity.getInstance().cancelButtonClick(v);
		case R.id.read_menu_conext:
			if (!btn_voice.isEnabled()) {// 正在播放语音
				if (menuLay_voice.getVisibility() == View.GONE) {
					menuLay_voice.setVisibility(View.VISIBLE);
					menuLay_top.setVisibility(View.VISIBLE);
				} else {
					menuLay_voice.setVisibility(View.GONE);
					menuLay_top.setVisibility(View.GONE);
				}
			} else if (menuLay == null) {
				cancelButtonClick(v);
			} else {
				menuLay.setVisibility(View.GONE);
				menuLay = null;
				menu_update.setChecked(false);
				menu_bg.setChecked(false);
				menu_font.setChecked(false);
			}
			break;
		case R.id.read_menu_voice:
			voiceUtils.startVoice(startVoiceCb);
			break;
		case R.id.read_voice_exit:
			voiceUtils.stopVoice();
			cancelButtonClick(null);
			break;
		case R.id.read_voice_pause:
			if (btn_voicePause.isChecked())
				voiceUtils.resumeVoice();
			else
				voiceUtils.pauseVoice();
			break;
		case R.id.read_voice_voicer:
			voiceUtils.setVoicer();
			break;
		case R.id.read_menu_last:
			if (Cache.toLastChapter() && ReadActivity.getInstance() != null)
				ReadActivity.getInstance().changeChapter();
			btn_last.setEnabled(Cache.hasLastChapter());
			btn_next.setEnabled(true);
			break;
		case R.id.read_menu_next:
			if (Cache.toNextChapter() && ReadActivity.getInstance() != null)
				ReadActivity.getInstance().changeChapter();
			btn_last.setEnabled(true);
			btn_next.setEnabled(Cache.hasNextChapter());
			break;
		case R.id.read_menu_directory:
			intent = new Intent(_this, DirectoryActivity.class);
			intent.putExtra("class", _this.getClass().getName());
			startActivity(intent);
			finish();
			animationRightToLeft();
			break;
		case R.id.read_menu_font_minus:
			if (ReadActivity.getInstance() != null)
				ReadActivity.getInstance().setTextSize(false);
			break;
		case R.id.read_menu_font_add:
			if (ReadActivity.getInstance() != null)
				ReadActivity.getInstance().setTextSize(true);
			break;
		case R.id.read_menu_update1:// 智能下载本章
			loadThisChapter();
			break;
		case R.id.read_menu_update2:// 下载本章及后续章节
			updateFollowingChapters();
			break;
		case R.id.read_menu_update3:// 手工选择下载点
			intent = new Intent(_this, SiteSwitchActivity.class);
			intent.putExtra("class", _this.getClass().getName());
			intent.putExtra("book", Cache.getBook());
			startActivityForResult(intent, R.id.read_menu_update3);
			animationRightToLeft();
			break;
		case R.id.read_menu_del_cache:
			delete(false);
			break;
		case R.id.read_menu_del_all:
			delete(true);
			break;
		case R.id.read_menu_color:
			startActivity(new Intent(_this, ReadSetColorActivity.class));
			finish();
			animationRightToLeft();
			break;
		case R.id.read_menu_html:
			ChapterEntity e = Cache.getCurrentChapter();
			if (e != null) {
				intent = new Intent(_this, BrowserActivity.class);
				intent.putExtra("title", e.getName());
				intent.putExtra("url", e.getUrl());
				intent.putExtra("class", _this.getClass().getName());
				startActivity(intent);
				finish();
				animationRightToLeft();
			}
			break;
		case R.id.read_menu_baidu:
			ChapterEntity e2 = Cache.getCurrentChapter();
			if (e2 != null) {
				intent = new Intent(_this, BrowserActivity.class);
				BookEntity b2 = Cache.getBook();
				String key = b2.getName() + " " + b2.getAuthor() + " " + e2.getName();
				intent.putExtra("title", key);
				intent.putExtra("url", "https://www.baidu.com/s?wd=" + key);
				intent.putExtra("class", _this.getClass().getName());
				startActivity(intent);
				finish();
				animationRightToLeft();
			}
			break;
		default:
			break;
		}
	}

	private CallBackListener startVoiceCb = new CallBackListener() {
		@Override
		public void callBack(String... params) {
			btn_cancel.setVisibility(View.INVISIBLE);
			btn_dir.setVisibility(View.INVISIBLE);
			btn_voice.setEnabled(false);
			if (menuLay != null)
				menuLay.setVisibility(View.GONE);
			menuLay_bottom.setVisibility(View.GONE);
			menuLay_top.setVisibility(View.GONE);
		}
	};

	public void cancelButtonClick(View v) {
		finish();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (!btn_voice.isEnabled()) {// 正在播放语音
				if (menuLay_voice.getVisibility() == View.GONE) {
					menuLay_voice.setVisibility(View.VISIBLE);
					menuLay_top.setVisibility(View.VISIBLE);
				} else {
					menuLay_voice.setVisibility(View.GONE);
					menuLay_top.setVisibility(View.GONE);
				}
				return true;
			} else {
				return super.onKeyUp(keyCode, event);
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void showMenuLay(View _menuLay) {
		if (menuLay == _menuLay && menuLay != null) {
			menuLay.setVisibility(View.GONE);
			menuLay = null;
		} else {
			if (menuLay != null)
				menuLay.setVisibility(View.GONE);
			menuLay = _menuLay;
		}
		if (menuLay != null)
			menuLay.setVisibility(View.VISIBLE);
		if (menuLay != menuLay_update)
			menu_update.setChecked(false);
		if (menuLay != menuLay_bg)
			menu_bg.setChecked(false);
		if (menuLay != menuLay_font)
			menu_font.setChecked(false);
		if (menuLay != menuLay_more)
			menu_font.setChecked(false);
	}

	private void delete(final boolean isAll) {
		Util.dialog(_this, "确定要删除吗", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (isAll)
					deleteAll();
				else
					deleteCache();
			}
		});
	}

	private void deleteCache() {
		try {
			String bookPath = FileUtil.getBooksPath(Cache.getBook().getId());
			int index = Cache.getCurrentChapterIndex();
			ArrayList<ChapterEntity> chapters = Cache.getChapters();
			ChapterEntity chapter;
			for (int i = 0; i < index; i++) {
				chapter = chapters.get(i);
				File file = new File(bookPath, FileUtil.getChapterName(chapter.getName()));
				if (file.exists()) {
					file.delete();
					chapter.setLoadStatus(LoadStatus.notLoaded);
				}
			}
			LoadManager.asynSaveDirectory(Cache.getBook().getId(), chapters);
		} catch (Exception e) {
		}
		Util.toast(_this, "删除成功");
	}

	private void deleteAll() {
		long id = Cache.getBook().getId();
		String bookPath = FileUtil.getBooksPath(id);
		for (File file : new File(bookPath).listFiles()) {
			if (!file.toString().equals(FileUtil.BOOK_INDEX_NAME) && file.exists())
				file.delete();
		}
		List<ChapterEntity> chapters = Cache.getChapters();
		for (ChapterEntity chapter : chapters) {
			chapter.setLoadStatus(LoadStatus.notLoaded);
		}
		LoadManager.asynSaveDirectory(id, chapters);
		Util.toast(_this, "删除成功");
		finish();
		animationLeftToRight();
	}

	/**
	 * 智能下载本章章节
	 */
	private void loadThisChapter() {
		if (!NetworkUtils.isNetConnectedRefreshWhereNot()) {
			Util.toast(_this, R.string.net_not_connected);
		} else if (AsyncTxtLoader.getInstance().loadCurrentChapter(_this, Cache.getBook(), Cache.getCurrentChapter(), 0, true)) {
			menu_stop.setVisibility(View.VISIBLE);
			menuLay_update.setVisibility(View.GONE);
			menu_update.setVisibility(View.GONE);
			menu_update.setChecked(false);
		}
	}

	/**
	 * 更新后面的章节
	 */
	private void updateFollowingChapters() {
		if (!NetworkUtils.isNetConnectedRefreshWhereNot()) {
			Util.toast(_this, R.string.net_not_connected);
			return;
		}
		menu_stop.setVisibility(View.VISIBLE);
		menuLay_update.setVisibility(View.GONE);
		menu_update.setVisibility(View.GONE);
		menu_update.setChecked(false);

		new Thread() {
			public void run() {
				BookEntity book = Cache.getBook();
				if (book.getLoadStatus() == LoadStatus.failed)
					book.setLoadStatus(LoadStatus.notLoaded);
				List<ChapterEntity> chapters = ParserManager.updateBookAndDict(book);
				if (chapters == null || chapters.size() == 0) {
					// 先获取缓存，再查找本地
					chapters = Cache.getChapters();
					if (chapters == null || chapters.size() == 0) {
						chapters = LoadManager.getDirectory(book.getId());
					}
					// 没有更新,且本地没有章节,则直接下载
					if ((book.getLoadStatus() != LoadStatus.failed) && (chapters == null || chapters.size() == 0)) {
						chapters = ParserManager.getDict(book);
					}
					if (chapters == null || chapters.size() == 0) {
						if (NetworkUtils.isNetConnected(null))
							book.setLoadStatus(LoadStatus.failed);
					}
				}
				sendMsgOnThread(3, chapters);
			};
		}.start();
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		switch (what) {
		case 0:
			if (ReadActivity.getInstance() != null)
				ReadActivity.getInstance().updateText();
			menu_update.setVisibility(View.VISIBLE);
			menu_stop.setVisibility(View.GONE);
			break;
		case 1:
			if (ReadActivity.getInstance() != null)
				ReadActivity.getInstance().updateText();
			break;
		case 2:
			menu_update.setVisibility(View.VISIBLE);
			menu_stop.setVisibility(View.GONE);
			break;
		case 3:
			BookEntity book = Cache.getBook();
			ArrayList<ChapterEntity> chapters = (ArrayList<ChapterEntity>) o;
			if (chapters == null || chapters.size() == 0) {
				menu_update.setVisibility(View.VISIBLE);
				menu_stop.setVisibility(View.GONE);
				return;
			}
			Cache.setChapters(chapters);
			if (book.getLoadStatus() == LoadStatus.hasnew) {
				new BookDao().updateBook(book);
			}
			LoadManager.asynSaveDirectory(book.getId(), Cache.getChapters());
			if (ReadActivity.getInstance() != null)
				ReadActivity.getInstance().refreshChapter();
			int count = chapters.size() - Cache.getCurrentChapterIndex() + 1;
			if (count == 0) {
				AsyncTxtLoader.getInstance().loadCurrentChapter(_this, book, Cache.getCurrentChapter(), 0, false);
			} else {
				AsyncTxtLoader.getInstance().loadCurrentChapter(_this, book, Cache.getCurrentChapter(), 1, false);
				if (!AsyncTxtLoader.getInstance().waitToOverAndRefresh(this, book.getId(), 2)) {
					if (!AsyncTxtLoader.getInstance().load(_this, book, chapters, 2, Cache.getCurrentChapterIndex() + 1)) {
						menu_update.setVisibility(View.VISIBLE);
						menu_stop.setVisibility(View.GONE);
					}
				}
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == R.id.read_menu_more) {

		} else if (requestCode == R.id.read_menu_update3) {// 手工选择下载点)
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(_this, ReadActivity.class);
				intent.putExtras(data);
				intent.putExtra("class", SiteSwitchActivity.class.getName());
				startActivity(intent);
			}
			finish();
		}
	}

	public String getVoiceText() {
		if (ReadActivity.getInstance() != null)
			return ReadActivity.getInstance().getVoiceText(voiceUtils);
		finish();
		return null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		voiceUtils.onDestroy();
	}
}
