package com.lqy.abook.load;

import java.util.ArrayList;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.Util;

public class Cache {

	private static BookEntity book;
	private static ArrayList<ChapterEntity> chapters;
	private static String lastSiteCurrentChapterName;// 切换网站前当前章节名字

	public static boolean exitChapters() {
		return chapters != null && chapters.size() > 0;
	}

	public static BookEntity getBook() {
		return book;
	}

	public static boolean toLastChapter() {
		if (exitChapters()) {
			if (book.getCurrentChapterId() > 0) {
				book.setCurrentChapterId(book.getCurrentChapterId() - 1);
				return true;
			}
		}
		return false;
	}

	public static boolean toNextChapter() {
		if (exitChapters()) {
			if (book.getCurrentChapterId() < chapters.size() - 1) {
				book.setCurrentChapterId(book.getCurrentChapterId() + 1);
				return true;
			}
		}
		return false;
	}

	public static boolean hasLastChapter() {
		if (exitChapters()) {
			return book.getCurrentChapterId() > 0;
		}
		return false;
	}

	public static boolean hasNextChapter() {
		if (exitChapters()) {
			return book.getCurrentChapterId() < chapters.size() - 1;
		}
		return false;
	}

	public static BookEntity setBook(BookEntity book) {
		if (book == null) {
			book = null;
			chapters = null;
		}
		if (Cache.book == null && book != null) {
			Cache.book = book;
		} else if (!Cache.book.equals(book)) {
			chapters = null;
			Cache.book = book;
		}
		return Cache.book;
	}

	public static ArrayList<ChapterEntity> getChapters() {
		return chapters;
	}

	public static void setChapters(ArrayList<ChapterEntity> chapters2) {
		Cache.chapters = chapters2;
	}

	public static void setCurrentChapter(ChapterEntity e) {
		book.setCurrentChapterId(e.getId());
	}

	public static ChapterEntity getCurrentChapter() {
		if (book == null || chapters == null)
			return null;
		if (book.getCurrentChapterId() < 0)
			book.setCurrentChapterId(0);
		if (book.getCurrentChapterId() >= chapters.size())
			book.setCurrentChapterId(chapters.size() - 1);
		return chapters.get(book.getCurrentChapterId());
	}

	public static int getCurrentChapterIndex() {
		if (book == null || chapters == null)
			return 0;
		if (book.getCurrentChapterId() < 0)
			book.setCurrentChapterId(0);
		if (book.getCurrentChapterId() >= chapters.size())
			book.setCurrentChapterId(chapters.size() - 1);
		return book.getCurrentChapterId();
	}

	public static String getCurrentChapterIndexString() {
		if (exitChapters()) {
			return (getCurrentChapterIndex() + 1) + "/" + chapters.size();
		}
		return CONSTANT.EMPTY;
	}

	public static void setLastSiteCurrentChapterName() {
		ChapterEntity e = getCurrentChapter();
		lastSiteCurrentChapterName = e != null ? e.getName() : null;
	}

	/**
	 * 切换网站后获取上一个网站当前章节的同名章节的位置
	 */
	public static void setCurrentChapterAfterChangeSite() {
		if (book == null || chapters == null || Util.isEmpty(lastSiteCurrentChapterName))
			return;
		for (int i = 0; i < chapters.size(); i++) {
			if (lastSiteCurrentChapterName.equals(chapters.get(i))) {
				book.setCurrentChapterId(i);
				lastSiteCurrentChapterName = null;
				return;
			}
		}
		book.setCurrentChapterId(0);
		lastSiteCurrentChapterName = null;
	}

}
