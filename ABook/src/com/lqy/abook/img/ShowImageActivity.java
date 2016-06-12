package com.lqy.abook.img;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.activity.DirectoryActivity;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.load.AsyncTxtLoader;
import com.lqy.abook.load.Cache;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;

public class ShowImageActivity extends MenuActivity {
	// private ViewPager pager;
	private List<String> urls;
	private static final String STATE_POSITION = "STATE_POSITION";

	private ImageViewPager pager;
	private ImagePagerAdapter adapter;
	private TextView view_toast;
	private View view_del;
	private View view_last;
	private View view_next;

	private ChapterEntity chapter;
	private BookEntity book;
	private BookDao dao = new BookDao();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_show_more);

		init();
		asynGetData();
	}

	private void init() {

		book = Cache.getBook();
		if (book == null) {
			finish();
			return;
		}
		chapter = Cache.getCurrentChapter();

		// 界面初始化
		view_toast = (TextView) findViewById(R.id.image_show_toast);
		view_del = findViewById(R.id.toolbar_del);
		view_last = findViewById(R.id.toolbar_last);
		view_next = findViewById(R.id.toolbar_next);

		view_last.setEnabled(false);
		view_next.setEnabled(false);

		setProgress();
	}

	public void updateReadLoation(int readBegin) {
		book.setReadBegin(readBegin);
		dao.updateReadLoation(book.getId(), book.getCurrentChapterId(), readBegin);
	}

	private TextView view_title;
	private TextView view_num;
	private ProgressBar view_progress;

	private void setProgress() {
		if (view_title == null) {
			view_title = (TextView) findViewById(R.id.image_show_title);
			view_num = (TextView) findViewById(R.id.image_show_num);
			view_progress = (ProgressBar) findViewById(R.id.image_show_progress);
		}
		if (chapter != null) {
			int cur = Cache.getCurrentChapterIndex();
			if (urls == null || urls.size() == 0) {
				view_progress.setProgress(0);
				view_title.setText(chapter.getName());
				view_num.setText((cur + 1) + "/" + Cache.getChapters().size());
			} else {
				int pos = pager != null ? pager.getCurrentItem() : 0;
				view_progress.setProgress((pos + 1) * 100 / urls.size());
				view_title.setText(chapter.getName());
				view_num.setText((cur + 1) + "/" + Cache.getChapters().size());
			}
		} else {
			view_title.setText(CONSTANT.EMPTY);
		}
	}

	private void asynGetData() {
		if (!NetworkUtils.isNetConnectedRefreshWhereNot()) {
			Util.toast(_this, R.string.net_not_connected);
		}
		view_toast.setVisibility(View.VISIBLE);
		view_toast.setText("加载中..");
		if (pager != null)
			pager.setVisibility(View.GONE);
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
					List<String> urls = LoadManager.getPicUrls(book.getId(), chapter.getName());
					if (urls == null || urls.size() == 0) {
						chapter.setLoadStatus(LoadStatus.failed);
						sendMsgOnThread(2, null);
					} else {
						chapter.setLoadStatus(LoadStatus.completed);
						sendMsgOnThread(1, urls);
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
			render((List<String>) o);
			break;
		case 2:// 下载
				// chapter = Cache.getCurrentChapter();
			List<ChapterEntity> chapters = Cache.getChapters();
			if (chapters != null) {// 不会为空，否则chapter不存在，不会进入这里
				// 下载本章
				AsyncTxtLoader.getInstance().loadCurrentChapterUrls(this, book, Cache.getCurrentChapter(), 1, false);
				// 下载后续章节
				if (!AsyncTxtLoader.isRunning(book.getId())) {
					int current = Cache.getCurrentChapterIndex();
					int limit = current + CONSTANT.auto_load_size;
					AsyncTxtLoader.getInstance().load(this, book, chapters, 5, current + 1, limit);
				}
			}
			break;
		case 4:
			view_toast.setVisibility(View.VISIBLE);
			view_toast.setText("获取目录失败");
			break;
		default:

			break;
		}
	}

	private int delCount = 0;

	@Override
	protected void onDestroy() {
		if (delCount > 0) {
			deleteChapter();
		}
		super.onDestroy();
	}

	private void deleteChapter() {
		List<ChapterEntity> chapters = Cache.getChapters();
		LoadManager.asynSaveDirectory(book.getId(), chapters);
		delCount = 0;

		if (book.getCurrentChapterId() >= chapters.size())
			book.setCurrentChapterId(chapters.size() - 1);
		book.setUnReadCount(chapters.size() - book.getCurrentChapterId() - 1);
	}

	private void render(List<String> imgs) {
		if (imgs == null || imgs.size() == 0) {
			// 如果没有图片，删除该章节
			Cache.getChapters().remove(chapter);
			delCount++;
			if (Cache.exitChapters()) {
				chapter = Cache.getCurrentChapter();
				asynGetData();
				MyLog.i("自动删除了一章  剩余" + Cache.getChapters().size());

				if (delCount > 10) {
					LoadManager.asynSaveDirectory(book.getId(), Cache.getChapters());
					delCount = 0;
				}
			} else {
				view_toast.setVisibility(View.VISIBLE);
				view_toast.setText("没有内容");
				view_last.setEnabled(false);
				view_next.setEnabled(false);
				deleteChapter();
			}
			return;
		}
		if (delCount > 0) {
			deleteChapter();
		}
		// 按钮状态
		int cur = book.getCurrentChapterId();
		if (cur > 0)
			view_last.setEnabled(true);
		else
			view_last.setEnabled(false);
		if (cur < Cache.getChapters().size() - 1)
			view_next.setEnabled(true);
		else
			view_next.setEnabled(false);

		view_toast.setVisibility(View.GONE);
		if (urls == null) {
			urls = new ArrayList<String>();
		}
		urls.clear();
		urls.addAll(imgs);

		if (pager == null) {
			pager = (ImageViewPager) findViewById(R.id.image_show_pager);
			adapter = new ImagePagerAdapter(this, urls, book.getId());
			adapter.setErrorPageListener(new ErrorPageListener(pager));// 加载失败或加载中的拖动事件
			pager.setAdapter(adapter);
			pager.setOnPageChangeListener(new ViewPagerChangeListener());
		} else {
			adapter.notifyDataSetChanged();
		}
		pager.setVisibility(View.VISIBLE);
		int pagerPosition = book.getReadBegin();
		pagerPosition = pagerPosition >= urls.size() ? 0 : pagerPosition;
		pager.setCurrentItem(pagerPosition, false);
		setProgress();

	}

	private class ViewPagerChangeListener implements OnPageChangeListener {
		public void onPageSelected(int arg0) {
			int pagerPosition = pager.getCurrentItem();
			setProgress();
			updateReadLoation(pagerPosition);
		}

		public void onPageScrollStateChanged(int arg0) {
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

	}

	public void delPicClick(View v) {
		Util.dialog(_this, "确定要删除吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int pos = pager.getCurrentItem();
				urls.remove(pos);
				adapter.notifyDataSetChanged();

				LoadManager.savePicUrls(book.getId(), chapter.getName(), urls);
			}
		});
	};

	public void sendButtonClick(View v) {
		switch (v.getId()) {
		case R.id.toolbar_last:
			if (Cache.toLastChapter()) {
				chapter = Cache.getCurrentChapter();
				updateReadLoation(0);
				asynGetData();
			}
			view_last.setEnabled(Cache.hasLastChapter());
			view_next.setEnabled(true);
			view_del.setVisibility(View.GONE);
			break;
		case R.id.toolbar_next:
			if (Cache.toNextChapter()) {
				chapter = Cache.getCurrentChapter();
				updateReadLoation(0);
				asynGetData();
			}
			view_last.setEnabled(true);
			view_next.setEnabled(Cache.hasNextChapter());
			view_del.setVisibility(View.GONE);
			break;
		case R.id.read_menu_directory:
			Intent intent = new Intent(_this, DirectoryActivity.class);
			intent.putExtra("class", _this.getClass().getName());
			startActivity(intent);
			finish();
			animationRightToLeft();
			break;
		case R.id.toolbar_del:
			Util.dialog(_this, "确定要删除吗？", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int pos = pager.getCurrentItem();
					urls.remove(pos);
					adapter.notifyDataSetChanged();

					LoadManager.savePicUrls(book.getId(), chapter.getName(), urls);
				}
			});
			break;
		default:
			break;
		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		if (chapter == null || chapter.getId() != Cache.getCurrentChapterIndex()) {
			// 切换章节
			chapter = Cache.getCurrentChapter();
			updateReadLoation(0);
			asynGetData();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
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

		if (pager != null) {
			int pagerPosition = savedInstanceState.getInt(STATE_POSITION);
			if (pagerPosition < chapters.size())
				pager.setCurrentItem(pagerPosition);
			setProgress();
			updateReadLoation(pagerPosition);
		}

		super.onRestoreInstanceState(savedInstanceState);
	}
}