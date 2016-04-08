package com.lqy.abook.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.db.HistoryDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.entity.ResultEntity;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.site.ParserBaidu;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.WebServer;

public class LoadingActivity extends MenuActivity {

	private BookDao dao = new BookDao();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);

		getBooks();
		// new Thread() {
		// public void run() {
		//
		// // ResultEntity e =
		// // WebServer.hcGetData(Config.getBaiduConfig().searchUrl + "武神",
		// // "utf-8");
		// // FileUtil.write(e.getMsg(), FileUtil.getDBPath(), "aa");
		// ArrayList<BookEntity> d = new ArrayList<BookEntity>();
		// new ParserBaidu().parserSearchSite(d, "绝世唐门", "唐家三少");
		// MyLog.i(d);
		// }
		// }.start();
	}

	private void step(ArrayList<BookEntity> books) {
		Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
		if (books != null)
			intent.putExtra("books", books);
		startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		finish();
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		step((ArrayList<BookEntity>) o);
		new HistoryDao().deleteOverdueHistory();
	}

	private void getBooks() {
		new Thread() {
			public void run() {
				List<BookEntity> books = dao.getBookList();
				if (books != null && books.size() > 0) {
					for (BookEntity book : books) {
						try {
							check(book);
						} catch (Exception e) {
						}
					}
				}
				sendMsgOnThread(0, books);
			}
		}.start();
	}

	/**
	 * 整理章节状态
	 */
	private void check(BookEntity book) {
		List<ChapterEntity> chapters = LoadManager.getDirectory(book.getId());
		// if (chapters == null || chapters.size() == 0) {
		// chapters = ParserManager.getDict(book);
		// book.setLoadStatus(LoadStatus.hasnew);
		// }
		if (chapters == null || chapters.size() == 0) {
			// book.setLoadStatus(LoadStatus.failed);
		} else {// 获取剩余未读章节数
			if (book.getCurrentChapterId() < 0)
				book.setCurrentChapterId(0);
			if (book.getCurrentChapterId() >= chapters.size())
				book.setCurrentChapterId(chapters.size() - 1);
			book.setUnReadCount(chapters.size() - book.getCurrentChapterId() - 1);
			// 更新章节状态
			File file;
			String path = FileUtil.getBooksPath(book.getId());
			for (ChapterEntity chapter : chapters) {
				if (!chapter.isVip()) {
					file = new File(path, FileUtil.getChapterName(chapter.getName()));
					if (file.exists() && file.length() > 0) {
						chapter.setLoadStatus(LoadStatus.completed);
					} else {
						chapter.setLoadStatus(LoadStatus.notLoaded);
					}
				}
			}
			LoadManager.saveDirectory(book.getId(), chapters);

		}
	}
}
