package com.lqy.abook.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.DirectoryAdapter;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.entity.Site;
import com.lqy.abook.img.ShowImageActivity;
import com.lqy.abook.load.AsyncTxtLoader;
import com.lqy.abook.load.Cache;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class DirectoryActivity extends MenuActivity {
	private ListView listView;
	private DirectoryAdapter adapter;
	private boolean toLast;
	private BookEntity book;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.directory);

		init();

	}

	private void init() {
		toLast = getIntent().getBooleanExtra("last", false);
		book = Cache.getBook();
		if (book == null) {
			finish();
			return;
		}

		TextView view_title = (TextView) findViewById(R.id.toolbar_title);
		view_hint = (TextView) findViewById(R.id.listview_empty);
		listView = (ListView) findViewById(android.R.id.list);
		listView.setVisibility(View.GONE);

		String title = book.getSite().getName();
		if (!Util.isEmpty(title)) {
			title = book.getName() + "-" + title;
		} else {
			title = book.getName();
		}
		view_title.setText(title);

		getData();
	}

	private void getData() {
		if (Cache.exitChapters()) {// 如果数据全都在，直接显示目录
			showDirectory();
		} else if (book.getId() > 0) {// 如果有bookid，没有章节，获取章节
			showProgressBar();
			new Thread() {
				public void run() {
					List<ChapterEntity> chapters = LoadManager.getDirectory(book.getId());
					if (chapters == null || chapters.size() == 0) {
						chapters = ParserManager.getDict(book);
					}
					if (chapters == null || chapters.size() == 0) {
						book.setLoadStatus(LoadStatus.failed);
						sendErrorOnThread("获取目录失败");
					} else {
						sendMsgOnThread(0, chapters);
					}
				}
			}.start();
		} else {// 没有bookid
			showProgressBar();
			new Thread() {
				public void run() {
					long id = new BookDao().getOrSaveBookId(book);
					if (id != CONSTANT.MSG_ERROR) {
						List<ChapterEntity> chapters = ParserManager.getDict(book);
						if (chapters != null && chapters.size() > 0) {
							sendMsgOnThread(0, chapters);
							return;
						}
					}
					book.setLoadStatus(LoadStatus.failed);
					sendErrorOnThread("获取目录失败");
				}
			}.start();
		}
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		if (what == 0) {// 获取目录
			Cache.setChapters((ArrayList<ChapterEntity>) o);
			LoadManager.asynSaveDirectory(book.getId(), Cache.getChapters());
			showDirectory();
		} else if (what == 1) {
			LoadManager.asynSaveDirectory(book.getId(), Cache.getChapters());
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void showDirectory() {
		listView.setVisibility(View.VISIBLE);
		if (Cache.getChapters().size() == 0) {
			view_hint.setVisibility(View.VISIBLE);
			view_hint.setText("未找到章节");
		} else {
			view_hint.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			if (adapter == null) {
				adapter = new DirectoryAdapter(_this, Cache.getChapters());
				// listView.addHeaderView(new View(_this));//
				// 显示分割线,这里不用在点击的时候arg2--
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						try {
							book.setReadBegin(0);
							Cache.setCurrentChapter(adapter.getItem(arg2));
							Intent intent;
							if (book.getSite() == Site.Pic) {
								intent = new Intent(_this, ShowImageActivity.class);
							} else {
								intent = new Intent(_this, ReadActivity.class);
							}
							intent.putExtra("class", _this.getClass().getName());
							// intent.putExtra("book", book);
							// intent.putExtra("chapter", e);
							startActivity(intent);
							finish();
							if (ReadMenuActivity.class.getName().equals(getIntent().getStringExtra("class"))
									|| ShowImageActivity.class.getName().equals(getIntent().getStringExtra("class")))
								animationLeftToRight();
							else
								animationRightToLeft();
						} catch (Exception e) {
							MyLog.e(e);
						}
					}
				});
				if (toLast) {
					listView.setSelection(adapter.getCount() - 1);
					toLast = false;
				} else {
					if (book.getCurrentChapterId() < adapter.getCount())
						listView.setSelection(book.getCurrentChapterId());
				}
			} else {
				adapter.notifyDataSetChanged();
				if (book.getCurrentChapterId() < adapter.getCount())
					listView.setSelection(book.getCurrentChapterId());
			}

			AsyncTxtLoader.getInstance().waitToOverAndRefresh(this, Cache.getBook().getId(), 1);
		}
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
		getData();
		super.onRestoreInstanceState(savedInstanceState);
	}
}
