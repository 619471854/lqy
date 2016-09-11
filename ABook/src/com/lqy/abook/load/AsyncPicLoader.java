package com.lqy.abook.load;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.parser.site.ParserPic;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class AsyncPicLoader {
	public static boolean isRunning;
	public static boolean isStop;

	private Object _o = new Object();
	private ThreadPoolExecutor executor;
	private BookEntity book;

	public boolean load(final MenuActivity activity, BookEntity _book, final int what) {
		if (_book == null) {
			return false;
		}
		if (isRunning) {
			Util.dialog(activity, "正在下载，请稍后");
			return false;

		}
		executor = new ThreadPoolExecutor(5, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
		isRunning = true;
		isStop = false;
		this.book = _book;
		book.setLoadStatus(LoadStatus.loading);
		// 等待下载并显示进度
		new Thread() {
			public void run() {
				List<ChapterEntity> chapters = LoadManager.getDirectory(book);
				if (chapters == null || chapters.size() == 0) {
					chapters = ParserManager.getDict(book);
				}
				if (chapters == null || chapters.size() == 0) {
					book.setLoadStatus(LoadStatus.failed);
					activity.sendMsgOnThread(what);
					return;
				}
				if (!isRunning) {
					book.setLoadStatus(LoadStatus.notLoaded);
					activity.sendMsgOnThread(what);
					return;
				}
				for (int i = 0; i < chapters.size(); i++) {
					if (isStop) {
						book.setLoadStatus(LoadStatus.completed);
						activity.sendMsgOnThread(what);
					} else if (isRunning) {
						int progress = i * 98 / chapters.size() + 1;
						activity.sendMsgOnThread(what, progress, null);

						ChapterEntity chapter = chapters.get(i);
						if (!load(chapter)) {
							chapters.remove(chapter);
							i--;
						}
						chapter.setLoadStatus(LoadStatus.completed);
					} else {
						book.setLoadStatus(LoadStatus.notLoaded);
						activity.sendMsgOnThread(what);
						return;
					}
				}

				for (int i = 0; i < chapters.size(); i++) {
					chapters.get(i).setId(i);
				}
				LoadManager.asynSaveDirectory(book.getId(), chapters);
				if (!isRunning) {
					try {
						executor.shutdownNow();
					} catch (Exception e) {
					}
					book.setLoadStatus(LoadStatus.notLoaded);
				} else {
					book.setLoadStatus(LoadStatus.completed);
					book.setPicLoadOver(true);
					book.setCurrentChapterId(0);
					book.setUnReadCount(chapters.size() - 1);
					new BookDao().updateBook(book);
				}

				if (!isRunning) {
					activity.sendMsgOnThread(what);
				} else {
					activity.sendMsgOnThread(what, 100, null);
					isRunning = false;
				}
			};
		}.start();
		return true;
	}

	private int totalCount;
	private int loadCount;

	private boolean load(ChapterEntity chapter) {
		chapter.setLoadStatus(LoadStatus.loading);
		List<String> urls = null;
		try {
			urls = LoadManager.getPicUrls(book.getId(), chapter.getName());
			if (urls == null || urls.size() == 0) {
				urls = new ParserPic().parserUrl(book, chapter.getUrl());
				if (urls == null || urls.size() == 0) {
					urls = null;
				}
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		if (urls == null || urls.size() == 0)
			return false;

		totalCount = urls.size();
		loadCount = 0;
		String path = FileUtil.getBooksPath(book.getId()) + File.separator + FileUtil.getChapterName(chapter.getName());
		List<String> loadedUrls = new ArrayList<String>();
		for (String url : urls) {
			if (!isRunning) {
				LoadManager.savePicUrls(book.getId(), chapter.getName(), urls);
				return true;
			}
			loadPics(loadedUrls, path, url);
		}
		while (isRunning && loadCount < totalCount) {
			try {
				Thread.sleep(300);
			} catch (Exception e) {
			}
		}
		if (loadedUrls == null || loadedUrls.size() == 0) {
			loadedUrls = null;
			new File(path).delete();
			return false;
		} else {
			LoadManager.savePicUrls(book.getId(), chapter.getName(), loadedUrls);
		}
		return true;
	}

	private int minLength = 5 * 1024;

	private void loadPics(final List<String> loadedUrls, final String path, final String url) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (!isRunning)
					return;

				String file = FileUtil.loadImageForUrl(url, path);
				boolean hasDelete = false;
				if (file == null) {
					hasDelete = true;
				} else {
					File f = new File(file);
					if (f.length() < minLength) {
						hasDelete = true;
						f.delete();
					}
				}
				// 进度
				synchronized (_o) {
					if (!hasDelete)
						loadedUrls.add(url);
					loadCount++;
				}
			}
		});
	}
}
