package com.lqy.abook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final String DBNAME = "abook.db";
	private static final int VERSION = 7;

	private static final String CREATE_TABLE_BOOK = "create table " + BookDao.table_name + " (" + BookDao.column_id + " integer primary key autoincrement,"
			+ BookDao.column_name + " text not null," + BookDao.column_cover + " text," + BookDao.column_type + " text," + BookDao.column_author + " text,"
			+ BookDao.column_site + " integer," + BookDao.column_tip + " text," + BookDao.column_detailUrl + " text," + BookDao.column_words + " integer,"
			+ BookDao.column_updateTime + " text," + BookDao.column_newChapter + " text," + BookDao.column_directoryUrl + " text not null,"
			+ BookDao.column_isCompleted + " boolean," + BookDao.column_sortTime + " integer ," + BookDao.column_currentChapterId + " integer ,"
			+ BookDao.column_readBegin + " integer," + BookDao.column_ext + " text );";

	private static final String CREATE_TABLE_HISTORY = "create table " + HistoryDao.table_name + " (" + HistoryDao.column_updateTime + " integer primary key,"
			+ HistoryDao.column_title + " text not null," + HistoryDao.column_url + " text  );";

	private static final String CREATE_TABLE_FAVORITE = "create table " + FavoriteDao.table_name + " (" + FavoriteDao.column_id
			+ " integer primary key autoincrement," + FavoriteDao.column_url + " text," + FavoriteDao.column_title + " text not null);";

	private static final String CREATE_TABLE_SEARCH = "create table " + SearchDao.table_name + " (" + SearchDao.column_name + " text primary key ,"
			+ SearchDao.column_time + " integer);";

	public DBHelper(Context context) {
		super(context, DBNAME, null, VERSION);// 默认
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_BOOK);
		db.execSQL(CREATE_TABLE_HISTORY);
		db.execSQL(CREATE_TABLE_FAVORITE);
		db.execSQL(CREATE_TABLE_SEARCH);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 6) {
			if (oldVersion != newVersion) {
				db.execSQL("drop table " + HistoryDao.table_name);
				db.execSQL(CREATE_TABLE_HISTORY);
			}
			db.execSQL("drop table " + SearchDao.table_name);
			db.execSQL(CREATE_TABLE_SEARCH);
		}
	}

	private static DBHelper instance;

	public static DBHelper getInstance(Context context) {
		if (instance == null) {
			DatabaseContext dbContext = new DatabaseContext(context);
			instance = new DBHelper(dbContext);
		}
		return instance;
	}

	public void closeDB() {
		if (instance != null) {
			try {
				SQLiteDatabase db = instance.getWritableDatabase();
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			instance = null;
		}
	}

}
