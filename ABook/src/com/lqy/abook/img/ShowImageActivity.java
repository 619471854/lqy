package com.lqy.abook.img;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.activity.DirectoryActivity;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatusEnum;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.load.Cache;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.MyAlertDialog;

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
		setContentView(R.layout.image_show);

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
		view_del.setVisibility(View.INVISIBLE);

		setProgress();

		view_del.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				deleteDialog();
				return true;
			}
		});
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
					List<ChapterEntity> chapters = LoadManager.getDirectory(book);
					if (chapters == null || chapters.size() == 0) {
						// 获取目录失败，请换源下载
						if (NetworkUtils.isNetConnected(null))
							book.setLoadStatus(LoadStatusEnum.failed);
						sendMsgOnThread(4, null);
					} else {
						if (book.getLoadStatus() == LoadStatusEnum.failed) {
							book.setLoadStatus(LoadStatusEnum.notLoaded);
						}
						sendMsgOnThread(0, chapters);
					}
				} else {// 从本地获取
					List<String> urls = LoadManager.getPicUrls(book.getId(), chapter.getName());

					chapter.setLoadStatus(LoadStatusEnum.completed);
					sendMsgOnThread(1, urls);
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
			deleteChapterResult();
		}
		super.onDestroy();
	}

	private void deleteChapterResult() {
		List<ChapterEntity> chapters = Cache.getChapters();
		LoadManager.asynSaveDirectory(book.getId(), chapters);
		delCount = 0;

		if (book.getCurrentChapterId() >= chapters.size())
			book.setCurrentChapterId(chapters.size() - 1);
		book.setUnReadCount(chapters.size() - book.getCurrentChapterId() - 1);
	}

	private boolean deleteChapter() {
		// 如果没有图片，删除该章节
		List<ChapterEntity> chapters = Cache.getChapters();
		chapters.remove(chapter);
		for (int i = 0; i < chapters.size(); i++) {
			chapters.get(i).setId(i);
		}
		delCount++;
		if (Cache.exitChapters()) {
			chapter = Cache.getCurrentChapter();
			asynGetData();
			MyLog.i("自动删除了一章  剩余" + chapters.size());

			if (delCount > 10) {
				LoadManager.asynSaveDirectory(book.getId(), chapters);
				delCount = 0;
			}
			return true;
		} else {
			view_toast.setVisibility(View.VISIBLE);
			view_toast.setText("没有内容");
			view_last.setEnabled(false);
			view_next.setEnabled(false);
			deleteChapterResult();
			return false;
		}
	}

	private void render(List<String> imgs) {
		if (imgs == null || imgs.size() == 0) {
			deleteChapter();
			return;
		}
		if (delCount > 0) {
			deleteChapterResult();
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
		view_del.setVisibility(View.VISIBLE);
		if (urls == null) {
			urls = new ArrayList<String>();
		}
		urls.clear();
		urls.addAll(imgs);

		if (pager == null) {
			pager = (ImageViewPager) findViewById(R.id.image_show_pager);
			pager.setOnPageChangeListener(new ViewPagerChangeListener());
			adapter = new ImagePagerAdapter(this, urls, book.getId(), chapter.getName());
			adapter.setErrorPageListener(new ErrorPageListener(pager));// 加载失败或加载中的拖动事件
			pager.setAdapter(adapter);
		} else {
			adapter = new ImagePagerAdapter(this, urls, book.getId(), chapter.getName());
			adapter.setErrorPageListener(new ErrorPageListener(pager));// 加载失败或加载中的拖动事件
			pager.setAdapter(adapter);
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
			view_del.setVisibility(View.INVISIBLE);
			break;
		case R.id.toolbar_next:
			if (Cache.toNextChapter()) {
				chapter = Cache.getCurrentChapter();
				updateReadLoation(0);
				asynGetData();
			}
			view_last.setEnabled(true);
			view_next.setEnabled(Cache.hasNextChapter());
			view_del.setVisibility(View.INVISIBLE);
			break;
		case R.id.toolbar_directory:
			Intent intent = new Intent(_this, DirectoryActivity.class);
			intent.putExtra("class", _this.getClass().getName());
			startActivity(intent);
			finish();
			animationRightToLeft();
			break;
		case R.id.toolbar_del:
			deleteImgToast(0);
			break;
		default:
			break;
		}
	};

	private void deleteDialog() {
		new MyAlertDialog(_this).setItems(R.array.delete_pic_menu, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteImgToast(which);
			}
		}).show();
	}

	private void deleteImgToast(final int which) {
		String[] arrays = getResources().getStringArray(R.array.delete_pic_menu);
		Util.dialog(_this, "确定要" + arrays[which] + "吗？", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int w) {
				deleteImg(which);
			}
		});
	}

	private void deleteImg(int which) {
		int pos = pager.getCurrentItem();
		switch (which) {
		case 0:// 删除此图片
			urls.remove(pos);
			if (urls.size() == 0) {
				deleteImg(3);
			} else {
				adapter.notifyDataSetChanged();
				LoadManager.savePicUrls(book.getId(), chapter.getName(), urls);
			}
			break;
		case 1:// 删除之前的所有图片
			for (int i = 0; i < pos; i++) {
				urls.remove(0);
			}
			if (urls.size() == 0) {
				deleteImg(3);
			} else {
				adapter.notifyDataSetChanged();
				LoadManager.savePicUrls(book.getId(), chapter.getName(), urls);
			}
			break;
		case 2:// 删除之后的所有图片
			for (int i = urls.size() - 1; i > pos; i--) {
				urls.remove(i);
			}
			if (urls.size() == 0) {
				deleteImg(3);
			} else {
				adapter.notifyDataSetChanged();
				LoadManager.savePicUrls(book.getId(), chapter.getName(), urls);
			}
			break;
		case 3:// 删除本章的所有图片
			deleteChapter();
			break;
		case 4:// 匹配删除
			deleteReg();
			break;
		}
	}

	/**
	 * 匹配删除
	 */
	private void deleteReg() {
		final EditText et = new EditText(_this);
		et.setBackgroundColor(Color.WHITE);
		int pos = pager.getCurrentItem();
		final String url = urls.get(pos);
		et.setText(url);
		et.setSelection(et.getText().toString().trim().length());
		new MyAlertDialog(_this).setTitleSingleLine(false).setTitle("以下显示了当前图片的地址，请保留部分地址再点击确定，将删除所有的地址与输入不匹配的图片").setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String value = et.getText().toString().trim();
						if (value.equals(url)) {
							Util.dialog(_this, "请输入更少的字符，否则会删除此外的所有图片");
						} else if (!Util.isEmpty(value)) {
							deleteReg(value);
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	private void deleteReg(final String urlReg) {
		showLoadingDialog("删除中");
		new Thread() {
			public void run() {
				long bookId = book.getId();
				List<ChapterEntity> chapters = Cache.getChapters();
				boolean hasDelChapter = false;
				for (int i = 0; i < chapters.size(); i++) {
					ChapterEntity chapter = chapters.get(i);
					List<String> urls = LoadManager.getPicUrls(bookId, chapter.getName());
					if (urls != null && urls.size() > 0) {
						boolean hasDelUrl = false;
						for (int j = 0; j < urls.size(); j++) {
							if (!urls.get(j).contains(urlReg)) {
								urls.remove(j);
								j--;
								hasDelUrl = true;
							}
						}
						if (hasDelUrl && urls.size() > 0) {
							LoadManager.savePicUrls(bookId, chapter.getName(), urls);
						}
					}
					if (urls == null || urls.size() == 0) {
						chapters.remove(i);
						i--;
						hasDelChapter = true;
					}
				}
				if (hasDelChapter) {
					LoadManager.saveDirectory(bookId, chapters);
				}
				runOnUiThread(new Runnable() {
					public void run() {
						Util.toast(_this, "删除成功,请重新打开界面");
						hideLoadingDialog();

						finish();
						animationRightToLeft();
					}
				});
			};
		}.start();
	}

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