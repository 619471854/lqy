package com.lqy.abook.db;

import java.util.List;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.tool.CONSTANT;

public class BookDao {
	public static final String table_name = "books";
	public static final String column_id = "id";// id
	public static final String column_name = "name";// 书名
	public static final String column_cover = "cover";// 封面图片
	public static final String column_type = "type";// 类型
	public static final String column_author = "author";// 作者
	public static final String column_site = "site";// 网站
	public static final String column_tip = "tip";// 简介
	public static final String column_detailUrl = "detailUrl";// 封面地址
	public static final String column_words = "words";// 字数
	public static final String column_updateTime = "updateTime";// 最新章节更新日期
	public static final String column_newChapter = "newChapter";// 最新章节名字
	public static final String column_directoryUrl = "directoryUrl";// 目录页地址	
	public static final String column_isCompleted = "isCompleted";// 本书是否已完结
	public static final String column_sortTime = "sortTime";// 开始阅读的时间，用于排序
	public static final String column_currentChapterId = "currentChapterId";// 当前章节
	public static final String column_readBegin = "readBegin";// 当前章节当前阅读位置
	public static final String column_ext = "ext";// 其他信息

	/**
	 * 获取书的id，如果书不存在则添加本书
	 */
	public long getOrSaveBookId(BookEntity book) {
		long id = DBManager.getInstance().getBookId(book);
		if (id == CONSTANT._1) {
			id = DBManager.getInstance().addBook(book);
		}
		if (id != CONSTANT._1) {
			book.setId(id);
		}
		return id;
	}

	/**
	 * 查询该书
	 */
	public boolean checkBook(BookEntity book) {
		return DBManager.getInstance().checkBook(book);
	}

	/**
	 * 添加本书
	 */
	public boolean addBook(BookEntity book) {
		long id = DBManager.getInstance().addBook(book);
		if (id != CONSTANT._1) {
			book.setId(id);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 更新本书
	 */
	public boolean updateBook(BookEntity book) {
		if (book.getId() == CONSTANT._1) {
			return addBook(book);
		} else {
			return DBManager.getInstance().updateBook(book);
		}
	}

	/**
	 * 删除本书
	 */
	public void deleteBook(long bookId) {
		DBManager.getInstance().deleteBook(bookId);
	}

	/**
	 * 获取书列表
	 */
	public List<BookEntity> getBookList() {
		return DBManager.getInstance().getBookList();
	}

	/**
	 * 更新下载状态
	 */
	public void updateReadLoation(final long bookId, final int currentId, final int readBegin) {
		new Thread() {
			public void run() {
				DBManager.getInstance().updateReadLoation(bookId, currentId, readBegin);
			}
		}.start();
	}

	/**
	 * 当前书排最前面
	 */
	public void updateBookSort(final int bookId) {
		new Thread() {
			public void run() {
				DBManager.getInstance().updateBookSort(bookId);
			}
		}.start();
	}
}
