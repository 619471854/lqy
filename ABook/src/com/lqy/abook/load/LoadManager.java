package com.lqy.abook.load;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lqy.abook.MenuActivity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.tool.Util;

public class LoadManager {

	public static String getChapterContent(long bookId, String chapterName) {
		return FileUtil.readByLine(FileUtil.getBooksPath(bookId) + File.separator + FileUtil.getChapterName(chapterName));
	}

	public static List<String> getPicUrls(long bookId, String chapterName) {
		String path = FileUtil.getBooksPath(bookId) + File.separator + FileUtil.getChapterName(chapterName) + File.separator + FileUtil.BOOK_INDEX_NAME;
		String text = FileUtil.readByLine(path);
		if (!Util.isEmpty(text)) {
			try {
				Type type = new TypeToken<List<String>>() {
				}.getType();
				return new Gson().fromJson(text, type);
			} catch (Exception e) {
			}
		}
		return null;
	}

	synchronized public static boolean savePicUrls(long bookId, String chapterName, List<String> imgs) {
		if (imgs == null)
			return false;
		String path = FileUtil.getBooksPath(bookId) + File.separator + FileUtil.getChapterName(chapterName);

		if (imgs == null || imgs.size() == 0) {
			FileUtil.delFile(new File(path, FileUtil.BOOK_INDEX_NAME));
			return false;
		} else {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			return FileUtil.write(new Gson().toJson(imgs), path, FileUtil.BOOK_INDEX_NAME);
		}
	}

	synchronized public static boolean saveDirectory(long bookId, List<ChapterEntity> chapters) {
		if (chapters == null)
			return false;
		return FileUtil.write(new Gson().toJson(chapters), FileUtil.getBooksPath(bookId), FileUtil.BOOK_INDEX_NAME);
	}

	public static void asynSaveDirectory(final long bookId, final List<ChapterEntity> chapters) {
		if (chapters != null)
			new Thread() {
				public void run() {
					saveDirectory(bookId, chapters);
				}
			}.start();
	}

	public static List<ChapterEntity> getDirectory(long bookId) {
		String text = FileUtil.readByBytes(FileUtil.getBooksPath(bookId) + File.separator + FileUtil.BOOK_INDEX_NAME);
		if (!Util.isEmpty(text)) {
			try {
				Type type = new TypeToken<List<ChapterEntity>>() {
				}.getType();
				return new Gson().fromJson(text, type);
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static void asynGetDirectory(final MenuActivity activity, final long bookId, final int what) {
		new Thread() {
			public void run() {
				activity.sendMsgOnThread(what, getDirectory(bookId));
			}
		}.start();
	}
}
