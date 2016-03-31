package com.lqy.abook.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lqy.abook.MyApp;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;

public class DBManager {
	private static DBManager dbMgr;
	private DBHelper dbHelper;
	private static Object _o = new Object();

	private DBManager() {
	}

	public static DBManager getInstance() {
		if (dbMgr == null) {
			synchronized (_o) {
				if (dbMgr == null) {
					dbMgr = new DBManager();
					dbMgr.dbHelper = DBHelper.getInstance(MyApp.getInstance());
				}
			}
		}
		return dbMgr;
	}

	/**
	 * 获取书列表
	 */
	synchronized public List<BookEntity> getBookList() {
		try {
			List<BookEntity> data = new ArrayList<BookEntity>();
			BookEntity e;
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(BookDao.table_name, null, null, null, null, null, BookDao.column_sortTime + " desc");
			while (c.moveToNext()) {
				e = new BookEntity();
				getBookForCursor(c, e);
				data.add(e);
			}
			c.close();
			return data;
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	/**
	 * 获取书的id
	 */
	synchronized public long getBookId(BookEntity book) {
		int id = CONSTANT.MSG_ERROR;
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(BookDao.table_name, new String[] { BookDao.column_id }, BookDao.column_author + "=? and " + BookDao.column_name + "=? and " + BookDao.column_site + "=?", new String[] {
					book.getAuthor(), book.getName(), book.getSite().ordinal() + CONSTANT.EMPTY }, null, null, null, "1");// 只取1项
			if (c.moveToNext())
				id = c.getInt(0);
			c.close();
		} catch (Exception e) {
			MyLog.e(e);
		}
		return id;
	}

	private BookEntity getBookForCursor(Cursor c, BookEntity e) {
		e.setId(c.getLong(c.getColumnIndex(BookDao.column_id)));
		e.setCover(c.getString(c.getColumnIndex(BookDao.column_cover)));
		e.setName(c.getString(c.getColumnIndex(BookDao.column_name)));
		e.setType(c.getString(c.getColumnIndex(BookDao.column_type)));
		e.setAuthor(c.getString(c.getColumnIndex(BookDao.column_author)));
		e.setSite(Site.valueOf(c.getInt(c.getColumnIndex(BookDao.column_site))));
		e.setTip(c.getString(c.getColumnIndex(BookDao.column_tip)));
		e.setDetailUrl(c.getString(c.getColumnIndex(BookDao.column_detailUrl)));
		e.setWords(c.getInt(c.getColumnIndex(BookDao.column_words)));
		e.setUpdateTime(c.getString(c.getColumnIndex(BookDao.column_updateTime)));
		e.setNewChapter(c.getString(c.getColumnIndex(BookDao.column_newChapter)));
		e.setDirectoryUrl(c.getString(c.getColumnIndex(BookDao.column_directoryUrl)));
		e.setCompleted(c.getInt(c.getColumnIndex(BookDao.column_isCompleted)) == 1);
		e.setCurrentChapterId(c.getInt(c.getColumnIndex(BookDao.column_currentChapterId)));
		e.setReadBegin(c.getInt(c.getColumnIndex(BookDao.column_readBegin)));
		// MyLog.i(c.getLong(c.getColumnIndex(BookDao.column_sortTime)) + "");
		return e;
	}

	/**
	 * 查询该书
	 */
	synchronized public boolean checkBook(BookEntity book) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(BookDao.table_name, null, BookDao.column_author + "=? and " + BookDao.column_name + "=? and " + BookDao.column_site + "=?",
					new String[] { book.getAuthor(), book.getName(), book.getSite().ordinal() + CONSTANT.EMPTY }, null, null, null, "1");// 只取1项
			if (c.moveToNext()) {
				getBookForCursor(c, book);
				return true;
			}
			c.close();
		} catch (Exception e) {
			MyLog.e(e);
		}
		return false;
	}

	/**
	 * 当前书排最前面
	 */
	synchronized public void updateBookSort(int bookId) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(BookDao.column_sortTime, System.currentTimeMillis());
			db.update(BookDao.table_name, values, BookDao.column_id + "=?", new String[] { bookId + CONSTANT.EMPTY });
		} catch (Exception e) {
		}
	}

	/**
	 * 保存一本书
	 */
	synchronized public long addBook(BookEntity book) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(BookDao.column_cover, book.getCover());
			values.put(BookDao.column_name, book.getName());
			values.put(BookDao.column_type, book.getType());
			values.put(BookDao.column_author, book.getAuthor());
			values.put(BookDao.column_site, book.getSite().ordinal());
			values.put(BookDao.column_tip, book.getTip());
			values.put(BookDao.column_detailUrl, book.getDetailUrl());
			values.put(BookDao.column_words, book.getWords());
			values.put(BookDao.column_updateTime, book.getUpdateTime());
			values.put(BookDao.column_newChapter, book.getNewChapter());
			values.put(BookDao.column_directoryUrl, book.getDirectoryUrl());
			values.put(BookDao.column_isCompleted, book.isCompleted() ? 1 : 0);
			values.put(BookDao.column_sortTime, System.currentTimeMillis());
			values.put(BookDao.column_currentChapterId, book.getCurrentChapterId());
			values.put(BookDao.column_readBegin, book.getReadBegin());
			return db.insert(BookDao.table_name, null, values);
		} catch (Exception e) {
			MyLog.e(e);
		}
		return -1;

	}

	/**
	 * 更新一本书
	 */
	synchronized public boolean updateBook(BookEntity book) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(BookDao.column_cover, book.getCover());
			values.put(BookDao.column_name, book.getName());
			values.put(BookDao.column_type, book.getType());
			values.put(BookDao.column_author, book.getAuthor());
			values.put(BookDao.column_site, book.getSite().ordinal());
			values.put(BookDao.column_tip, book.getTip());
			values.put(BookDao.column_detailUrl, book.getDetailUrl());
			values.put(BookDao.column_words, book.getWords());
			values.put(BookDao.column_updateTime, book.getUpdateTime());
			values.put(BookDao.column_newChapter, book.getNewChapter());
			values.put(BookDao.column_directoryUrl, book.getDirectoryUrl());
			values.put(BookDao.column_isCompleted, book.isCompleted() ? 1 : 0);
			values.put(BookDao.column_sortTime, System.currentTimeMillis());
			values.put(BookDao.column_currentChapterId, book.getCurrentChapterId());
			values.put(BookDao.column_readBegin, book.getReadBegin());
			int result = db.update(BookDao.table_name, values, BookDao.column_id + "=" + book.getId(), null);
			return result > 0;
		} catch (Exception e) {
		}
		return false;

	}

	/**
	 * 删除一本书
	 */
	synchronized public void deleteBook(long bookId) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(BookDao.table_name, "id=" + bookId, null);
		} catch (Exception e) {
		}

	}

	/**
	 * 更新当前章节
	 */
	synchronized public void updateReadLoation(long bookId, int currentId, int readBegin) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(BookDao.column_currentChapterId, currentId);
			values.put(BookDao.column_readBegin, readBegin);
			db.update(BookDao.table_name, values, BookDao.column_id + "=?", new String[] { bookId + CONSTANT.EMPTY });
		} catch (Exception e) {
		}
	}

	/**
	 * 获取历史纪录,最多1千条
	 */
	synchronized public List<FavoriteEntity> getHistoryList(long startTime, long endtime) {
		List<FavoriteEntity> data = new ArrayList<FavoriteEntity>();
		try {
			FavoriteEntity e;
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String selection = HistoryDao.column_updateTime + " > " + startTime + " and " + HistoryDao.column_updateTime + " < " + endtime;
			Cursor c = db.query(HistoryDao.table_name, null, selection, null, null, null, HistoryDao.column_updateTime + " desc", "1000");
			while (c.moveToNext()) {
				e = new FavoriteEntity();
				e.setTitle(c.getString(c.getColumnIndex(HistoryDao.column_title)));
				e.setUrl(c.getString(c.getColumnIndex(HistoryDao.column_url)));
				data.add(e);
			}
			c.close();
		} catch (Exception e) {
			MyLog.e(e);
		}
		return data;
	}

	/**
	 * 获取历史纪录,最多1千条
	 */
	synchronized public FavoriteEntity getLastestHistory() {
		FavoriteEntity e = null;
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(HistoryDao.table_name, null, null, null, null, null, HistoryDao.column_updateTime + " desc", "1");
			if (c.moveToNext()) {
				e = new FavoriteEntity();
				e.setTitle(c.getString(c.getColumnIndex(HistoryDao.column_title)));
				e.setUrl(c.getString(c.getColumnIndex(HistoryDao.column_url)));
			}
			c.close();
		} catch (Exception e1) {
			MyLog.e(e1);
		}
		return e;
	}

	/**
	 * 保存一条历史纪录
	 */
	synchronized public long saveHistory(String title, String url) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(HistoryDao.column_title, title);
			values.put(HistoryDao.column_url, url);
			values.put(HistoryDao.column_updateTime, System.currentTimeMillis());
			return db.replace(HistoryDao.table_name, null, values);
		} catch (Exception e) {
			MyLog.e(e);
		}
		return -1;
	}

	/**
	 * 删除多余的数据
	 */
	public void deleteOverdueHistory(long endtime) {
		try {
			String whereClause = HistoryDao.column_updateTime + " < " + endtime;
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(HistoryDao.table_name, whereClause, null);
		} catch (Exception e) {
		}

	}

	/**
	 * 删除所以数据
	 */
	public void emptyHistory() {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(HistoryDao.table_name, null, null);
		} catch (Exception e) {
		}

	}

	synchronized public void closeDB() {
		if (dbHelper != null) {
			dbHelper.closeDB();
		}
	}

	/**
	 * 获取收藏列表
	 */
	synchronized public List<FavoriteEntity> getFavoriteList() {
		List<FavoriteEntity> data = new ArrayList<FavoriteEntity>();
		try {
			FavoriteEntity e;
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(FavoriteDao.table_name, null, null, null, null, null, null);
			while (c.moveToNext()) {
				e = new FavoriteEntity();
				e.setId(c.getInt(c.getColumnIndex(FavoriteDao.column_id)));
				e.setTitle(c.getString(c.getColumnIndex(FavoriteDao.column_title)));
				e.setUrl(c.getString(c.getColumnIndex(FavoriteDao.column_url)));
				data.add(e);
			}
			c.close();
		} catch (Exception e) {
			MyLog.e(e);
		}
		return data;
	}

	/**
	 * 添加收藏
	 */
	synchronized public long saveFavorite(String title, String url) {
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(FavoriteDao.column_title, title);
			values.put(FavoriteDao.column_url, url);
			return db.insert(FavoriteDao.table_name, null, values);
		} catch (Exception e) {
			MyLog.e(e);
		}
		return -1;
	}

	/**
	 * 删除收藏
	 */
	synchronized public int deleteFavorite(int id) {
		try {
			String whereClause = FavoriteDao.column_id + " = " + id;
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			return db.delete(FavoriteDao.table_name, whereClause, null);
		} catch (Exception e) {
		}
		return -1;
	}

}