//package com.lqy.abook.load;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//import com.lqy.abook.MenuActivity;
//import com.lqy.abook.entity.BookEntity;
//import com.lqy.abook.entity.ChapterEntity;
//import com.lqy.abook.entity.LoadStatus;
//import com.lqy.abook.parser.ParserManager;
//import com.lqy.abook.tool.CONSTANT;
//import com.lqy.abook.tool.MyLog;
//import com.lqy.abook.tool.Util;
//
//public class AsyncTxtLoader2 {
//	public static AsyncTxtLoader2 instance;
//	private static Object _o = new Object();
//
//	public static AsyncTxtLoader2 getInstance() {
//		if (instance == null) {
//			synchronized (_o) {
//				if (instance == null) {
//					MyLog.i("AsyncTxtLoader.instance create");
//					instance = new AsyncTxtLoader2();
//				}
//			}
//		}
//		return instance;
//	}
//
//	public static boolean isRunning() {
//		return instance != null && instance.isRunning;
//	}
//
//	public static boolean isRunning(long bookId) {
//		return isRunning() && instance.totalCountMap.containsKey(bookId);
//	}
//
//	/**
//	 * 停止，首页的暂停按钮调用
//	 */
//	public static void stopLoad() {
//		if (instance != null) {
//			try {
//				instance.executor.shutdownNow();
//				instance.isRunning = false;
//				instance = null;
//				MyLog.i("stopLoad ");
//			} catch (Exception e) {
//				MyLog.e(e);
//			}
//		} else {
//			MyLog.i(" not Required  stopLoad");
//		}
//	}
//
//	private AsyncTxtLoader2() {
//		// 线程池：最大50条，每次执行：5条，空闲线程结束的超时时间：180秒
//		executor = new ThreadPoolExecutor(5, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
//	}
//
//	private ThreadPoolExecutor executor;
//	private boolean isRunning;
//	private Map<Long, Integer> totalCountMap = new HashMap<Long, Integer>();// 用于保存书的章节数
//	private Map<Long, Integer> loadedCountMap = new HashMap<Long, Integer>();// 用于保存章节已下载的数量
//	private long waitBookId = -1;// 等待章些先下载
//	private List<Integer> waitChapterIds = null;// 等待章些先下载
//
//	/**
//	 * 下载本书所有章节
//	 */
//	public void load(MenuActivity activity, BookEntity book, List<ChapterEntity> chapters, int what) {
//		if (book == null || chapters == null || chapters.size() == 0) {
//			return;
//		}
//		book.setLoadStatus(LoadStatus.loading);
//		isRunning = true;
//
//		int totalCount = chapters.size();
//		totalCountMap.put(book.getId(), totalCount);
//		int loadedCount = 0;// 当前已下载的数量
//		for (int i = 0; i < totalCount; i++) {
//			ChapterEntity chapter = chapters.get(i);
//			if (chapter.isVip()) {
//				totalCountMap.put(book.getId(), i);
//				break;
//			}
//			if (Util.isEmpty(chapter.getUrl()) || chapter.getLoadStatus() == LoadStatus.completed) {
//				loadedCount++;
//			} else {
//				load(activity, book, chapter, what);
//			}
//		}
//		loadedCountMap.put(book.getId(), loadedCount);
//	}
//
//	/**
//	 * 暂停，等待某些章节先下载
//	 */
//
//	private void pauseWaitLoadChapter(long bookId, List<ChapterEntity> chapters, int index, int limit) {
//		if (!isRunning)
//			return;
//		if (waitChapterIds == null)
//			waitChapterIds = new ArrayList<Integer>();
//		else
//			waitChapterIds.clear();
//		synchronized (waitChapterIds) {
//			waitBookId = bookId;
//			for (int i = index; i < limit; i++) {
//				waitChapterIds.add(chapters.get(i).getId());
//			}
//		}
//	}
//
//	/**
//	 * 下载本书部分章节
//	 */
//	public void load(MenuActivity activity, BookEntity book, List<ChapterEntity> chapters, int what, int index) {
//		if (book == null || chapters == null || chapters.size() == 0) {
//			return;
//		}
//		book.setLoadStatus(LoadStatus.loading);
//		isRunning = true;
//
//		if (index < 0)
//			index = 0;
//		if (index >= chapters.size())
//			index = chapters.size() - 1;
//		int limit = Math.min(CONSTANT.auto_load_size + index, chapters.size());
//		int totalCount = chapters.size();
//		totalCountMap.put(book.getId(), totalCount);
//		int loadedCount = 0;// 当前已下载的数量
//		for (int i = 0; i < totalCount; i++) {
//			ChapterEntity chapter = chapters.get(i);
//			if (chapter.isVip()) {
//				totalCountMap.put(book.getId(), i);
//				break;
//			}
//			if (Util.isEmpty(chapter.getUrl()) || chapter.getLoadStatus() == LoadStatus.completed) {
//				loadedCount++;
//			} else {
//				load(activity, book, chapter, what);
//			}
//		}
//		loadedCountMap.put(book.getId(), loadedCount);
//	}
//
//	/**
//	 * 等待下载并显示进度
//	 */
//	public void waitToOverAndShowProgress(final MenuActivity activity, final long bookId, final int what) {
//		new Thread() {
//			public void run() {
//				try {
//					do {
//						Thread.sleep(1000);
//						int loadedCount = loadedCountMap.get(bookId);
//						int totalCount = totalCountMap.get(bookId);
//						// 最多只显示99个进度(1-99)
//						if (loadedCount % (totalCount / 100 + 1) == 0) {
//							activity.sendMsgOnThread(what, loadedCount * 100 / totalCount);
//						}
//					} while (!activity.isFinishing() && isRunning(bookId));
//					// 完成
//					activity.sendMsgOnThread(what, 100);
//				} catch (Exception e) {
//				}
//			};
//		}.start();
//	}
//
//	private void load(final MenuActivity activity, final BookEntity book, final ChapterEntity chapter, final int what) {
//		chapter.setLoadStatus(LoadStatus.loading);
//		executor.execute(new Runnable() {
//			@Override
//			public void run() {
//				if (!isRunning)
//					return;
//				// 等待某些章节先下载,最多等待1分钟
//				boolean isWait = true;
//				int waitNum = 0;
//				do {
//					if (waitChapterIds == null || waitChapterIds.size() == 0)
//						isWait = false;
//					else if (isWait)
//						try {
//							waitNum++;
//							Thread.sleep(100);
//						} catch (Exception e) {
//						}
//				} while (isWait && waitNum < 600);
//				// 当前的chapter是否在等待队列里
//				boolean isWaitChapter = false;
//				if (waitBookId == book.getId()) {
//					for (int chapterId : waitChapterIds) {
//						if (chapterId == chapter.getId()) {
//							isWaitChapter = true;
//							break;
//						}
//					}
//				}
//				if (!isRunning)
//					return;
//				File file = new File(FileUtil.getBooksPath(book.getId()), FileUtil.getChapterName(chapter.getName()));
//				boolean success = false;
//				if (file.exists() && file.length() > 0) {
//					success = true;
//				} else {
//					String text = ParserManager.asynGetChapterDetail(chapter.getUrl(), book.getSite());
//					if (!Util.isEmpty(text)) {
//						text = chapter.getName() + "\n" + text;
//						success = FileUtil.write(text, FileUtil.getBooksPath(book.getId()), FileUtil.getChapterName(chapter.getName()));
//					}
//				}
//				chapter.setLoadStatus(success ? LoadStatus.completed : LoadStatus.failed);
//				// 进度
//				synchronized (executor) {
//					try {
//						int loadedCount = loadedCountMap.get(book.getId()) + 1;
//						int totalCount = totalCountMap.get(book.getId());
//						if (loadedCount >= totalCount) {
//							// 本书下载完成
//							totalCountMap.remove(book.getId());
//							loadedCountMap.remove(book.getId());
//							book.setLoadStatus(LoadStatus.completed);
//							if (totalCountMap.keySet().size() == 0) {
//								isRunning = false;
//								MyLog.i("asynTextLoader all Over");
//							} else {
//								MyLog.i("asynTextLoader this Over");
//							}
//							instance = null;
//							activity.sendMsgOnThread(what, 100);
//						} else {// 未下载完成
//							loadedCountMap.put(book.getId(), loadedCount);
//							// 最多只显示99个进度(1-99)
//							if (loadedCount % (totalCount / 100 + 1) == 0) {
//								activity.sendMsgOnThread(what, loadedCount * 100 / totalCount);
//							}
//						}
//					} catch (Exception e) {
//						// TODO: handle exception
//					}
//				}
//			}
//		});
//	}
//}
