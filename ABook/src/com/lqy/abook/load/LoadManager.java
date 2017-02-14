package com.lqy.abook.load;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lqy.abook.MenuActivity;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatusEnum;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.Util;

public class LoadManager {

	public static String getChapterContent(long bookId, String chapterName) {
		return FileUtil.readByLine(FileUtil.getBooksPath(bookId) + File.separator + FileUtil.getChapterName(chapterName));
	}

	public static String saveChapterContent(long bookId, String chapterName, String text) {
		if (Util.isEmpty(text)) {
			return null;
		}
		if (text.startsWith(chapterName) || text.startsWith(chapterName.replaceAll("\\s", CONSTANT.EMPTY)))
			text = "        " + text;
		else
			text = chapterName + "\n        " + text;
		FileUtil.write(text, FileUtil.getBooksPath(bookId), FileUtil.getChapterName(chapterName));
		return text;
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

	public static List<ChapterEntity> getDirectory(BookEntity book) {
		if (book.getSite() == SiteEnum.Single) {
			List<ChapterEntity> data = new ArrayList<ChapterEntity>();
			ChapterEntity e = new ChapterEntity();
			e.setName(book.getName());
			e.setLoadStatus(LoadStatusEnum.completed);
			data.add(e);
			return data;
		} else {
			String text = FileUtil.readByBytes(FileUtil.getBooksPath(book.getId()) + File.separator + FileUtil.BOOK_INDEX_NAME);
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
	}

	public static void asynGetDirectory(final MenuActivity activity, final BookEntity book, final int what) {
		new Thread() {
			public void run() {
				activity.sendMsgOnThread(what, getDirectory(book));
			}
		}.start();
	}
}
