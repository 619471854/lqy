package com.lqy.abook.load;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.activity.CoverActivity;
import com.lqy.abook.activity.MainActivity;
import com.lqy.abook.activity.ReadActivity;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.img.ShowImageActivity;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class AsyncTxtLoader {
	public static AsyncTxtLoader instance;
	private static Object _o = new Object();

	public static AsyncTxtLoader getInstance() {
		if (instance == null) {
			synchronized (_o) {
				if (instance == null) {
					MyLog.i("AsyncTxtLoader.instance create");
					instance = new AsyncTxtLoader();
				}
			}
		}
		return instance;
	}

	public static boolean isRunning() {
		return instance != null && instance.isRunning;
	}

	public static boolean isRunning(long bookId) {
		return isRunning() && instance.totalCountMap != null && instance.totalCountMap.containsKey(bookId);
	}

	/**
	 * 停止，首页的暂停按钮调用
	 */
	public static void stopLoad() {
		if (instance != null) {
			try {
				instance.executor.shutdownNow();
				instance.isRunning = false;
				instance = null;
				instance.totalCountMap.clear();
				instance.loadedCountMap.clear();
				MyLog.i("stopLoad ");
			} catch (Exception e) {
				MyLog.e(e);
			}
		} else {
			MyLog.i(" not Required  stopLoad");
		}
	}

	/**
	 * 停止某一本书的下载
	 */
	public static void stopLoadBook(long bookId) {
		if (instance != null) {
			try {
				synchronized (instance.executor) {
					instance.totalCountMap.remove(bookId);
					instance.loadedCountMap.remove(bookId);
				}
				MyLog.i("stopLoadBook " + bookId);
			} catch (Exception e) {
				MyLog.e(e);
			}
		} else {
			MyLog.i(" not Required  stopLoad");
		}
	}

	private AsyncTxtLoader() {
		// 线程池：最大50条，每次执行：5条，空闲线程结束的超时时间：180秒
		executor = new ThreadPoolExecutor(5, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
	}

	private ThreadPoolExecutor executor;
	private boolean isRunning;
	private Map<Long, Integer> totalCountMap = new Hashtable<Long, Integer>();// 用于保存书的章节数
	private Map<Long, Integer> loadedCountMap = new Hashtable<Long, Integer>();// 用于保存章节已下载的数量

	/**
	 * 下载当前章节
	 */
	public boolean loadCurrentChapter(final MenuActivity activity, final BookEntity book, final ChapterEntity chapter, final int what, final boolean isReload) {
		if (book != null && chapter != null) {
			new Thread() {
				public void run() {
					String text = download(book, chapter, isReload);
					activity.sendMsgOnThread(what, text);
				}
			}.start();
			return true;
		}
		return false;
	}

	/**
	 * 下载本书部分章节
	 */
	public boolean load(CoverActivity activity, BookEntity book, List<ChapterEntity> chapters, int what) {
		return load(activity, book, chapters, what, 0, -1, true);
	}

	public boolean load(MenuActivity activity, BookEntity book, List<ChapterEntity> chapters, int what, int index) {
		return load(activity, book, chapters, what, index, -1, false);
	}

	public boolean load(ReadActivity activity, BookEntity book, List<ChapterEntity> chapters, int what, int index, int limit) {
		return load(activity, book, chapters, what, index, limit, false);
	}

	public boolean load(ShowImageActivity activity, BookEntity book, List<ChapterEntity> chapters, int what, int index, int limit) {
		return load(activity, book, chapters, what, index, limit, false);
	}

	private boolean load(MenuActivity activity, BookEntity book, List<ChapterEntity> chapters, int what, int index, int limit, boolean isSendProgress) {
		if (book == null || chapters == null || chapters.size() == 0) {
			return false;
		}
		if (limit < 0)
			limit = chapters.size();
		else
			limit = Math.min(limit, chapters.size());
		int count = limit - index;
		if (count < 1) {
			return false;
		}
		isRunning = true;
		count = 0;
		for (int i = index; i < limit; i++) {
			ChapterEntity chapter = chapters.get(i);
			if (chapter.isVip()) {
				break;
			}
			if (!Util.isEmpty(chapter.getUrl()) && chapter.getLoadStatus() != LoadStatus.completed) {
				// 如果前面没下载本章，则需要把下载内容传回界面
				count++;
				load(activity, book, chapter, what, isSendProgress);
			}
		}
		totalCountMap.put(book.getId(), count);
		loadedCountMap.put(book.getId(), 0);
		MyLog.i("totalChapterCount=" + count);
		if (count == 0) {// 没有需要下载的
			isRunning = false;
			activity.sendMsgOnThread(what, 100, null);
			return false;
		}
		book.setLoadStatus(LoadStatus.loading);
		return true;
	}

	/**
	 * 等待下载并显示进度
	 */
	public void waitToOverAndShowProgress(final CoverActivity activity, final long bookId, final int what) {
		new Thread() {
			public void run() {
				try {
					while (!activity.isFinishing() && isRunning(bookId)) {
						// MyLog.i("waitToOverAndShowProgress :" + bookId );
						int loadedCount = loadedCountMap.get(bookId);
						int totalCount = totalCountMap.get(bookId);
						activity.sendMsgOnThread(what, loadedCount * 100 / totalCount, null);
						Thread.sleep(300);
					}
				} catch (Exception e) {
					MyLog.e(e);
				}
				try {// 完成
					activity.sendMsgOnThread(what, 100, null);
					MyLog.i("waitToOverAndShowProgress 100%");
				} catch (Exception e) {
				}
			};
		}.start();
	}

	/**
	 * 等待下载并显示进度
	 */
	public boolean waitToOverAndRefresh(final MenuActivity activity, final long bookId, final int what) {
		if (isRunning(bookId)) {
			new Thread() {
				public void run() {
					try {
						Thread.sleep(300);
						while (!activity.isFinishing() && isRunning(bookId)) {
							// MyLog.i("waitToOverAndRefresh :" + bookId );
							activity.sendMsgOnThread(what, null);
							Thread.sleep(300);
						}
					} catch (Exception e) {
						MyLog.e(e);
					}
				};
			}.start();
			return true;
		}
		return false;
	}

	private void load(final MenuActivity activity, final BookEntity book, final ChapterEntity chapter, final int what, final boolean sendProgress) {
		chapter.setLoadStatus(LoadStatus.loading);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (!isRunning)
					return;
				download(book, chapter, false);
				// 进度
				synchronized (executor) {
					try {
						int loadedCount = loadedCountMap.get(book.getId()) + 1;
						int totalCount = totalCountMap.get(book.getId());
						if (loadedCount >= totalCount) {
							// 本书下载完成
							totalCountMap.remove(book.getId());
							loadedCountMap.remove(book.getId());
							book.setLoadStatus(LoadStatus.completed);
							if (totalCountMap.keySet().size() == 0) {
								isRunning = false;
								instance = null;
								MyLog.i("asynTextLoader all Over");
							} else {
								MyLog.i("asynTextLoader this Over");
							}
							// if (sendProgress)
							activity.sendMsgOnThread(what, 100, null);
							MainActivity.setLoadingOver();
						} else {// 未下载完成
							loadedCountMap.put(book.getId(), loadedCount);
							// 最多只显示99个进度(1-99)
							if (sendProgress && loadedCount % (totalCount / 100 + 1) == 0) {
								int progress = loadedCount * 100 / totalCount;
								MyLog.i("asynTextLoader " + progress + "%");
								activity.sendMsgOnThread(what, progress, null);
							} else {
								// MyLog.i("asynTextLoader chapter:" +
								// book.getId() + " " + loadedCount + " " +
								// totalCount);
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		});
	}

	private String download(BookEntity book, ChapterEntity chapter, boolean isReload) {
		try {
			String text = null;
			if (!isReload)
				text = LoadManager.getChapterContent(book.getId(), chapter.getName());

			if (Util.isEmpty(text)) {
				text = ParserManager.getChapterDetail(book, chapter.getUrl());
				text = LoadManager.saveChapterContent(book.getId(), chapter.getName(), text);

				chapter.setLoadStatus(text != null ? LoadStatus.completed : LoadStatus.failed);
			} else {
				chapter.setLoadStatus(LoadStatus.completed);
			}
			return text;

		} catch (Exception e) {
			MyLog.e(e);
			return null;
		}
	}
}
