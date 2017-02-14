package com.lqy.abook.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.BookGridAdapter;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatusEnum;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.load.AsyncPicLoader;
import com.lqy.abook.load.AsyncTxtLoader;
import com.lqy.abook.load.Cache;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.DisplayUtil;
import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.MatcherTool;
import com.lqy.abook.tool.MyClipboard;
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.DrawerHScrollView;
import com.lqy.abook.widget.DrawerHScrollView.IDrawerPresenter;
import com.lqy.abook.widget.MyAlertDialog;

public class MainActivity extends MenuActivity {
	private DrawerHScrollView hscrollview;
	private GridView gridView;
	private LinearLayout numLay;
	public static boolean isAddBook;
	private List<BookEntity> books;
	private View btn_update;
	private View btn_stop;
	private boolean isBack = false;
	private static MainActivity instance;
	private BookGridAdapter adapter;
	private BookDao dao = new BookDao();

	public static MainActivity getInstance() {
		if (instance.isFinishing())
			return null;
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_book);
		instance = this;
		init();

		refresh();

		// 启动时默认检查更新
		if (NetworkUtils.isNetConnected(null)) {
			SharedPreferences sp = getSharedPreferences(CONSTANT.SP_CENTER, 0);
			boolean autoCheckUpdate = !sp.getBoolean("not_auto_check_udate", false);
			if (autoCheckUpdate) {
				update(books, true);
			}
		}
	}

	private void init() {
		books = (List<BookEntity>) getIntent().getSerializableExtra("books");
		// for (int i = 0; i < 15; i++) {
		// BookEntity e = new BookEntity();
		// e.setName("测试_" + books.size());
		// books.add(e);
		// }

		view_hint = (TextView) findViewById(R.id.listview_empty);
		hscrollview = (DrawerHScrollView) findViewById(R.id.hscrollview);
		gridView = (GridView) findViewById(R.id.gridView);
		numLay = (LinearLayout) findViewById(R.id.pager_num);

		hscrollview.setDrawerPresenter(new IDrawerPresenter() {
			public void dispatchEvent(int totalPages, int currentPage) {
				// 获取标签按钮
				Button lastBtn = (Button) numLay.findViewWithTag("selected");
				if (lastBtn != null) {
					lastBtn.setBackgroundResource(R.drawable.point_nor);
					lastBtn.setTag(CONSTANT.EMPTY);
				}
				Button currentBtn = (Button) numLay.findViewById(currentPage);
				if (currentBtn != null) {
					currentBtn.setBackgroundResource(R.drawable.point_seclect);
					currentBtn.setTag("selected");
				}
			}
		});
		btn_update = findViewById(R.id.pager_update);
		btn_stop = findViewById(R.id.pager_stop);

	}

	@Override
	protected void onNewIntent(Intent intent) {
		List<BookEntity> data = (List<BookEntity>) intent.getSerializableExtra("books");
		if (books != null && data != null) {
			books.clear();
			books.addAll(data);
			Cache.setBook(null);
		} else if (isAddBook) {// BrowserActivity过来
			BookEntity book = (BookEntity) intent.getSerializableExtra("book");
			if (book != null) {
				if (books == null) {
					books = new ArrayList<BookEntity>();
					view_hint.setVisibility(View.GONE);
				}
				books.add(0, book);
			}
			isAddBook = false;
		}
		refresh();
		super.onNewIntent(intent);
	}

	private void refresh() {
		if (books == null) {
			view_hint.setText(NetworkUtils.isNetConnected(_this) ? "获取书籍失败" : "找不到网络");
			view_hint.setVisibility(View.VISIBLE);
			books = new ArrayList<BookEntity>();
		} else if (books.size() == 0) {
			view_hint.setText("还没有收藏书籍");
			view_hint.setVisibility(View.VISIBLE);
		} else {
			view_hint.setVisibility(View.GONE);
		}
		refresh(true);
	}

	public void delete(BookEntity e) {
		FileUtil.delFile(new File(FileUtil.getBooksPath(e.getId())));
		new BookDao().deleteBook(e.getId());
		books.remove(e);
		if (Cache.getBook() != null && Cache.getBook().getId() == e.getId()) {
			Cache.setBook(null);
		}
		AsyncTxtLoader.stopLoadBook(e.getId());
		refresh();
	}

	/**
	 * 未改变数量的刷新
	 */
	int lastPages = 0;

	public void refresh(boolean changeNum) {
		if (!changeNum) {
			adapter.notifyDataSetChanged();
			return;
		}
		// 每页3列，行数通过计算出来,下间隙是142dp，每行145dp
		int rows = (GlobalConfig.getScreenHeight() - DisplayUtil.dip2px(_this, 142)) / DisplayUtil.dip2px(_this, 145);
		int scrollWid = GlobalConfig.getScreenWidth();
		int spaceing = DisplayUtil.dip2px(_this, 20);
		int columnWidth = (scrollWid - spaceing * 4) / 3;
		int pages = (books.size() - 1) / (rows * 3) + 1;// 页数
		if (lastPages != pages) {
			lastPages = pages;
			LayoutParams params = new LayoutParams((columnWidth + spaceing) * pages * 3 + spaceing, -1);
			gridView.setLayoutParams(params);
			gridView.setNumColumns(pages * 3);
		}

		if (adapter == null) {
			hscrollview.setParameters(pages, 0, scrollWid - spaceing);
			gridView.setPadding(spaceing, 0, spaceing, 0);
			gridView.setColumnWidth(columnWidth);
			gridView.setHorizontalSpacing(spaceing);
			gridView.setVerticalSpacing(spaceing);
			gridView.setStretchMode(GridView.NO_STRETCH);
			adapter = new BookGridAdapter(this, books, pages, rows);
			gridView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}

		// 设置页码
		numLay.removeAllViews();
		if (pages == 1)
			return;
		int width = DisplayUtil.dip2px(_this, 8);
		LayoutParams params = new LinearLayout.LayoutParams(width, width);
		Button b;
		int margin = DisplayUtil.dip2px(_this, 4);
		for (int i = 0; i < pages; i++) {
			b = new Button(this);
			numLay.addView(b);
			if (i == 0) {
				b.setBackgroundResource(R.drawable.point_seclect);
				b.setTag("selected");
			} else {
				b.setBackgroundResource(R.drawable.point_nor);
			}
			params.setMargins(margin, margin, margin, margin);
			b.setLayoutParams(params);
			b.setId(i);
		}
	}

	public void sendButtonClick(View v) {
		switch (v.getId()) {
		case R.id.pager_update:
			if (!NetworkUtils.isNetConnectedRefreshWhereNot()) {
				Util.toast(_this, R.string.net_not_connected);
			} else {
				update(books, false);
			}
			break;
		case R.id.pager_stop:
			AsyncTxtLoader.stopLoad();
			updateBookCount = 0;
			btn_stop.setVisibility(View.GONE);
			btn_update.setVisibility(View.VISIBLE);
			boolean hasLoading = false;
			if (books != null)
				for (BookEntity b : books) {
					if (b.getLoadStatus() == LoadStatusEnum.loading) {
						hasLoading = true;
						b.setLoadStatus(LoadStatusEnum.notLoaded);
					}
				}

			if (Cache.getChapters() != null)
				for (ChapterEntity e : Cache.getChapters()) {
					if (e.getLoadStatus() == LoadStatusEnum.loading) {
						e.setLoadStatus(LoadStatusEnum.notLoaded);
					}
				}
			if (hasLoading)
				refresh(false);
			break;
		case R.id.pager_add:
			startActivity(new Intent(this, SearchActivity.class));
			animationRightToLeft();
			break;
		case R.id.pager_toWeb:
			startActivity(new Intent(this, BrowserActivity.class));
			animationRightToLeft();
			break;
		case R.id.pager_myCenter:
			startActivity(new Intent(this, MyCenterActivity.class));
			animationRightToLeft();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onStop() {
		isBack = true;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		instance = null;
		super.onDestroy();
	}

	private String lastClipboardText;

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// 更新阅读记录
		if (books != null && books.size() > 0) {
			List<BookEntity> current = dao.getBookList();
			if (current != null && current.size() > 0) {
				for (BookEntity book : books) {
					for (BookEntity e : current) {
						if (book.getId() == e.getId()) {
							book.setCurrentChapterId(e.getCurrentChapterId());
							book.setReadBegin(e.getReadBegin());
							break;
						}
					}
				}
			}
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		// 获取剪贴板数据
		final String clipboardText = MyClipboard.get(_this);
		if (!Util.isEmpty(clipboardText) && !clipboardText.equals(lastClipboardText) && MatcherTool.matchWebSite(clipboardText)) {
			lastClipboardText = clipboardText;
			new MyAlertDialog(_this).setTitle("是否打开复制链接？").setMessage(clipboardText).setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(_this, BrowserActivity.class);
					intent.putExtra("url", clipboardText);
					startActivity(intent);
					animationRightToLeft();
				}
			}).setNegativeButton("取消", null).setNeutralButton("清空", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					MyClipboard.copy(_this, CONSTANT.EMPTY);
				}
			}).show();
		}

		if (Cache.getBook() != null && Cache.exitChapters())
			Cache.getBook().setUnReadCount(Cache.getChapters().size() - Cache.getCurrentChapterIndex() - 1);
		isBack = false;
		if (isAddBook && Cache.getBook() != null) {
			isAddBook = false;
			if (books == null) {
				books = new ArrayList<BookEntity>();
				view_hint.setVisibility(View.GONE);
			}
			// 查找此书是否存在，如果存在，则删除后再添加到第一个
			long id = Cache.getBook().getId();
			for (int i = 0; i < books.size(); i++) {
				if (id == books.get(i).getId()) {
					books.remove(i);
					break;
				}
			}
			books.add(0, Cache.getBook());

			refresh();
		} else if (books != null && books.size() > 0) {
			refresh(false);
		}
		if (AsyncTxtLoader.isRunning()) {
			btn_stop.setVisibility(View.VISIBLE);
			btn_update.setVisibility(View.GONE);
		}
		super.onResume();
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		switch (what) {
		case 0:
			if (o != null) {
				BookAndChapters e = (BookAndChapters) o;
				BookEntity book = e.getBook();
				ArrayList<ChapterEntity> chapters = (ArrayList<ChapterEntity>) e.getChapters();
				if (book.getLoadStatus() == LoadStatusEnum.hasnew) {
					dao.updateBook(book);
				}
				LoadManager.asynSaveDirectory(book.getId(), chapters);
				// 如果正在阅读本书，更新章节
				if (Cache.getBook() != null && Cache.getBook().getId() == book.getId() && Cache.getChapters() != null)
					Cache.setChapters(chapters);

				if (arg1 == 0) {// 下载未读章节
					if (!AsyncTxtLoader.getInstance().load(_this, book, chapters, 1, book.getCurrentChapterId())) {
						book.setLoadStatus(LoadStatusEnum.completed);
						updateBookCount--;
					}
				}
			} else
				updateBookCount--;
			if (arg1 == 0 && !isLoading()) {
				btn_stop.setVisibility(View.GONE);
				btn_update.setVisibility(View.VISIBLE);
			}
			if (arg1 == 0)
				refresh(false);
			break;
		case 1:
			updateBookCount--;
			if (!isLoading()) {
				btn_stop.setVisibility(View.GONE);
				btn_update.setVisibility(View.VISIBLE);
			}
			refresh(false);
			break;
		case 2:// 其它界面开启的下载，下载结束后执行
			if (!isBack && !isLoading()) {
				btn_stop.setVisibility(View.GONE);
				btn_update.setVisibility(View.VISIBLE);
			}
			break;
		case 4:
			break;
		case 5:
			if (arg1 == 0 || arg1 == 100) {
				updateBookCount--;
				if (!isLoading()) {
					btn_stop.setVisibility(View.GONE);
					btn_update.setVisibility(View.VISIBLE);
				}
				refresh(false);
			} else if (dialog != null && dialog.isShowing()) {
				dialog.setProgress(arg1);
			}
			break;
		}
	}

	/**
	 * 显示加载状态
	 */
	public static void setLoadingOver() {
		if (getInstance() != null && !instance.isBack)
			instance.sendMsgOnThread(2, null);
	}

	public boolean isLoading() {
		return updateBookCount > 0;
	}

	private int updateBookCount = 0;

	private void update(List<BookEntity> books, boolean onlyCheck) {
		if (books == null || books.size() == 0)
			return;
		if (!onlyCheck) {
			btn_update.setVisibility(View.GONE);
			btn_stop.setVisibility(View.VISIBLE);

			updateBookCount = books.size();
		}
		for (BookEntity book : books) {
			if (book.supportUpdated())
				asynUpdateBook(book, 0, onlyCheck);
			else if (!onlyCheck)
				updateBookCount--;
		}
		if (!onlyCheck)
			refresh(false);
	}

	private ProgressDialog dialog;

	public boolean update(BookEntity book) {
		if (book.getSite() == SiteEnum.Pic) {
			final AsyncPicLoader loader = new AsyncPicLoader();
			if (loader.load(_this, book, 5)) {
				btn_update.setVisibility(View.GONE);
				btn_stop.setVisibility(View.VISIBLE);
				updateBookCount = 1;

				dialog = new ProgressDialog(this);
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setCancelable(false);
				dialog.setProgress(0);
				dialog.setTitle("下载中...");
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "完成", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						loader.isStop = true;
					}
				});
				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						loader.isRunning = false;
					}
				});
				dialog.show();
				return true;
			} else {
				return false;
			}
		} else if (!book.supportUpdated()) {
			Util.dialog(_this, "本书不支持更新");
			return false;
		} else if (isLoading()) {
			Util.dialog(_this, "正在更新，请稍后");
			return false;
		} else {
			btn_update.setVisibility(View.GONE);
			btn_stop.setVisibility(View.VISIBLE);
			updateBookCount = 1;
			asynUpdateBook(book, 0, false);
			return true;
		}
	}

	private void asynUpdateBook(final BookEntity book, final int what, boolean onlyCheck) {
		if (!onlyCheck)
			book.setLoadStatus(LoadStatusEnum.loading);
		final int onlyCheckInt = onlyCheck ? 1 : 0;
		new Thread() {
			public void run() {
				List<ChapterEntity> chapters = ParserManager.updateBookAndDict(book);
				if (chapters != null && chapters.size() > 0) {
					// 检测到更新，获取剩余未读章节数
					if (book.getCurrentChapterId() < 0)
						book.setCurrentChapterId(0);
					if (book.getCurrentChapterId() >= chapters.size())
						book.setCurrentChapterId(chapters.size() - 1);
					book.setUnReadCount(chapters.size() - book.getCurrentChapterId() - 1);

					sendMsgOnThread(what, onlyCheckInt, new BookAndChapters(book, chapters));
				} else if (onlyCheckInt == 1) {// 仅仅检查更新
					sendMsgOnThread(what, onlyCheckInt, null);
				} else if (book.getLoadStatus() == LoadStatusEnum.failed) {// 仅仅检查更新失败
					if (!NetworkUtils.isNetConnected(null))// 网络原因的不显示失败
						book.setLoadStatus(LoadStatusEnum.notLoaded);
					sendMsgOnThread(what, onlyCheckInt, null);
				} else {// 没有更新，获取本地章节目录
					chapters = LoadManager.getDirectory(book);
					if (chapters == null || chapters.size() == 0) {
						chapters = ParserManager.getDict(book);
					}
					if (chapters == null || chapters.size() == 0) {
						if (NetworkUtils.isNetConnected(null))
							book.setLoadStatus(LoadStatusEnum.failed);
						sendMsgOnThread(what, onlyCheckInt, null);// 更新失败
					} else {
						// 获取剩余未读章节数
						if (book.getCurrentChapterId() < 0)
							book.setCurrentChapterId(0);
						if (book.getCurrentChapterId() >= chapters.size())
							book.setCurrentChapterId(chapters.size() - 1);
						book.setUnReadCount(chapters.size() - book.getCurrentChapterId() - 1);
						// 更新章节状态
						File file;
						String path = FileUtil.getBooksPath(book.getId());
						boolean notSupportUpdated = !book.getSite().supportUpdated();
						for (ChapterEntity chapter : chapters) {
							if (notSupportUpdated) {
								chapter.setLoadStatus(LoadStatusEnum.completed);
							} else if (!chapter.isVip()) {
								file = new File(path, FileUtil.getChapterName(chapter.getName()));
								if (file.exists() && file.length() > 0) {
									chapter.setLoadStatus(LoadStatusEnum.completed);
								} else {
									chapter.setLoadStatus(LoadStatusEnum.notLoaded);
								}
							}
						}
						sendMsgOnThread(what, onlyCheckInt, new BookAndChapters(book, chapters));
					}
				}
			};
		}.start();
	}
}
