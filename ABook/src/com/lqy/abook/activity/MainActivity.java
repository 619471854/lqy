package com.lqy.abook.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
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
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.load.AsyncTxtLoader;
import com.lqy.abook.load.Cache;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.DisplayUtil;
import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.DrawerHScrollView;
import com.lqy.abook.widget.DrawerHScrollView.IDrawerPresenter;

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
		if (waitLoadBooks != null && waitLoadBooks.contains(e)) {
			waitLoadBooks.remove(e);
			if (waitLoadBooks.size() == 0) {
				waitLoadBooks = null;
			}
		}
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
			} else if (books != null && books.size() > 0) {
				btn_update.setVisibility(View.GONE);
				btn_stop.setVisibility(View.VISIBLE);
				// 全部的状态设置成更新中
				for (BookEntity book : books) {
					book.setLoadStatus(LoadStatus.loading);
				}
				refresh(false);
				// 下载第一本书，且其他书添加到等待栈里
				if (books.size() > 1) {
					if (waitLoadBooks == null)
						waitLoadBooks = new ArrayList<BookEntity>();
					else
						waitLoadBooks.clear();
					waitLoadBooks.addAll(books);
					asynUpdateBook(waitLoadBooks.remove(0));
				} else {
					asynUpdateBook(books.get(0));
				}
			}
			break;
		case R.id.pager_stop:
			AsyncTxtLoader.stopLoad();
			waitLoadBooks = null;
			currentBook = null;
			btn_stop.setVisibility(View.GONE);
			btn_update.setVisibility(View.VISIBLE);
			boolean hasLoading = false;
			if (books != null)
				for (BookEntity b : books) {
					if (b.getLoadStatus() == LoadStatus.loading) {
						hasLoading = true;
						b.setLoadStatus(LoadStatus.notLoaded);
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

	@Override
	protected void onResume() {
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
		case 0:// 没有更新
			updateOver();
			break;
		case 1:// 更新结束
			updateOver();
			break;
		case 2:// 其它界面开启的下载，下载结束后执行
			if (!isBack && currentBook == null && waitLoadBooks == null) {
				btn_stop.setVisibility(View.GONE);
				btn_update.setVisibility(View.VISIBLE);
			}
			break;
		case 3:
			if (currentBook != null && o != null) {
				List<ChapterEntity> chapters = (List<ChapterEntity>) o;
				LoadManager.asynSaveDirectory(currentBook.getId(), chapters);
				// 如果正在阅读本书，更新章节
				if (Cache.getBook() != null && Cache.getBook().getId() == currentBook.getId() && Cache.getChapters() != null)
					Cache.setChapters((ArrayList<ChapterEntity>) chapters);
			}
			break;
		}
	}

	/**
	 * 更新结束
	 */
	private void updateOver() {
		btn_stop.setVisibility(View.GONE);
		btn_update.setVisibility(View.VISIBLE);
		if (currentBook != null && currentBook.getLoadStatus() != LoadStatus.failed) {
			MyLog.i("asynUpdateOver " + currentBook.getName());
			currentBook.setLoadStatus(LoadStatus.completed);
		}
		refresh(false);
		if (waitLoadBooks == null || waitLoadBooks.size() == 0) {
			waitLoadBooks = null;
			currentBook = null;
		} else if (!NetworkUtils.isNetConnectedRefreshWhereNot()) {
			Util.toast(_this, R.string.net_not_connected);
			waitLoadBooks = null;
			currentBook = null;
		} else {
			// 继续更新下一本
			asynUpdateBook(waitLoadBooks.remove(0));
		}
	}

	/**
	 * 显示加载状态
	 */
	public static void setLoadingOver() {
		if (getInstance() != null && !instance.isBack)
			instance.sendMsgOnThread(2, null);
	}

	private BookEntity currentBook;// 当前下载的
	private List<BookEntity> waitLoadBooks;// 等待下载的

	public boolean isLoading() {
		return currentBook != null;
	}

	synchronized public void asynUpdateBook(final BookEntity book) {
		MyLog.i("asynUpdateBook " + book.getName());
		currentBook = book;
		btn_stop.setVisibility(View.VISIBLE);
		btn_update.setVisibility(View.GONE);
		new Thread() {
			public void run() {
				book.setLoadStatus(LoadStatus.loading);
				List<ChapterEntity> chapters = ParserManager.updateBookAndDict(book);
				if (chapters == null || chapters.size() == 0) {
					if (book.getLoadStatus() == LoadStatus.failed) {
						if (!NetworkUtils.isNetConnected(null))// 网络原因的不显示失败
							book.setLoadStatus(LoadStatus.notLoaded);
						sendMsgOnThread(0, null);
						return;
					} else {// 没有更新
						chapters = LoadManager.getDirectory(book.getId());
						if (chapters == null || chapters.size() == 0) {
							chapters = ParserManager.getDict(book);
						}
						if (chapters == null || chapters.size() == 0) {
							if (NetworkUtils.isNetConnected(null))
								book.setLoadStatus(LoadStatus.failed);
							sendMsgOnThread(0, null);// 更新失败
							return;
						}
					}
				}
				// 获取剩余未读章节数
				if (book.getCurrentChapterId() < 0)
					book.setCurrentChapterId(0);
				if (book.getCurrentChapterId() >= chapters.size())
					book.setCurrentChapterId(chapters.size() - 1);
				book.setUnReadCount(chapters.size() - book.getCurrentChapterId() - 1);

				if (!AsyncTxtLoader.getInstance().load(_this, book, chapters, 1, book.getCurrentChapterId()))
					sendMsgOnThread(0, null);// 没有需要更新的

				sendMsgOnThread(3, chapters);// 没有需要更新的

			};
		}.start();
	}
}
