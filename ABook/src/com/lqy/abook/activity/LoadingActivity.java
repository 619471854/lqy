package com.lqy.abook.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.db.DBManager;
import com.lqy.abook.db.HistoryDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.site.ParserBaidu;
import com.lqy.abook.tool.MyLog;

public class LoadingActivity extends MenuActivity {

	private BookDao dao = new BookDao();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);

		Drawable drawable = FileUtil.loadDrawable(FileUtil.getAppPath(), FileUtil.LOADING_NAME);
		if (drawable != null) {
			ImageView view = (ImageView) findViewById(R.id.loading_bg);
			view.setImageDrawable(drawable);
		}
		getBooks();
		// testGetData();
	}

	private static void testGetData() {
		new Thread() {
			public void run() {
				try {
					ArrayList<BookEntity> data = new ArrayList<BookEntity>();
					new ParserBaidu().parserSearch(data, "武神");
					MyLog.i(data);
				} catch (Exception e) {
					MyLog.i(e);
				}
			}
		}.start();
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
		List<ChapterEntity> chapters = LoadManager.getDirectory(book);
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
			boolean notSupportUpdated = !book.getSite().supportUpdated();
			for (ChapterEntity chapter : chapters) {
				if (notSupportUpdated) {
					chapter.setLoadStatus(LoadStatus.completed);
				} else if (!chapter.isVip()) {
					file = new File(path, FileUtil.getChapterName(chapter.getName()));
					if (file.exists() && file.length() > 0) {
						chapter.setLoadStatus(LoadStatus.completed);
					} else {
						chapter.setLoadStatus(LoadStatus.notLoaded);
					}
				}
			}
			if (!notSupportUpdated) {
				LoadManager.saveDirectory(book.getId(), chapters);
			}
		}
	}
}
