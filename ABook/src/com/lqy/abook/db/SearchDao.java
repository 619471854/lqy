package com.lqy.abook.db;

import java.util.Calendar;
import java.util.List;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.tool.MyLog;

public class SearchDao {
	public static final String table_name = "search_history";
	public static final String column_name = "name";// 书名
	public static final String column_time = "time";// 日期

	/**
	 * 查询搜索记录
	 */
	public void getList(final MenuActivity a, final int what) {
		new Thread() {
			public void run() {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -30);
				List<String> data = DBManager.getInstance().getSearchList(cal.getTimeInMillis());
				a.sendMsgOnThread(what, data);
			}
		}.start();
	}

	/**
	 * 添加查询搜索记录
	 */
	public void add(final String name) {
		new Thread() {
			public void run() {
				long id = DBManager.getInstance().addSearchHistory(name, System.currentTimeMillis());
				MyLog.i("SearchDao add " + id);
			}
		}.start();
	}

	/**
	 * 清空搜索记录
	 */
	public void empty() {
		new Thread() {
			public void run() {
				DBManager.getInstance().emptySearchHistory();
			}
		}.start();
	}
}
