package com.lqy.abook.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.entity.HistoryGroupEntity;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class HistoryDao {
	public static final String table_name = "history";
	public static final String column_title = "title";// 名
	public static final String column_url = "url";// 地址
	public static final String column_updateTime = "updateTime";// 更新日期

	/**
	 * 添加记录
	 */
	public void saveHistory(final String title, final String url) {
		if (Util.isEmpty(title) || Util.isEmpty(url)) {
			MyLog.e("saveHistory empty " + title + " " + url);
		} else {
			new Thread() {
				public void run() {
					long id = DBManager.getInstance().saveHistory(title, url);
					MyLog.e("saveHistory id= " + id);
				}
			}.start();
		}
	}

	/**
	 * 获取最后1条历史纪录
	 */
	public void getLastestHistory(final MenuActivity activity, final int what) {
		new Thread() {
			public void run() {
				FavoriteEntity history = DBManager.getInstance().getLastestHistory();
				// MyLog.i(history.toString());
				activity.sendMsgOnThread(what, history);
			}
		}.start();
	}

	/**
	 * 获取历史纪录,每项最多1千条
	 */
	public void getHistoryList(final MenuActivity activity, final int what) {
		new Thread() {
			public void run() {
				DBManager dbm = DBManager.getInstance();
				List<HistoryGroupEntity> data = new ArrayList<HistoryGroupEntity>();
				// 获取今天的记录
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.setTimeInMillis(cal.getTimeInMillis() / 1000 * 1000);
				long endtime = System.currentTimeMillis() + 1;
				long startTime = cal.getTimeInMillis() - 1;
				List<FavoriteEntity> child = dbm.getHistoryList(startTime, endtime);
				if (child.size() > 0) {
					data.add(new HistoryGroupEntity("今天", child));
				}
				// 获取最近7天的记录
				cal.add(Calendar.DAY_OF_MONTH, -6);
				endtime = startTime + 1;
				startTime = cal.getTimeInMillis() - 1;
				child = dbm.getHistoryList(startTime, endtime);
				if (child.size() > 0) {
					data.add(new HistoryGroupEntity("近7天", child));
				}
				// 获取最近1个月的记录
				cal.add(Calendar.DAY_OF_MONTH, 6);
				cal.add(Calendar.MONTH, -1);
				endtime = startTime + 1;
				startTime = cal.getTimeInMillis() - 1;
				child = dbm.getHistoryList(startTime, endtime);
				if (child.size() > 0) {
					data.add(new HistoryGroupEntity("近1个月", child));
				}
				// 获取超过1个月的记录
				endtime = startTime + 1;
				startTime = 0;
				child = dbm.getHistoryList(startTime, endtime);
				if (child.size() > 0) {
					data.add(new HistoryGroupEntity("超过1个月", child));
				}
				// MyLog.i(data);
				activity.sendMsgOnThread(what, data);
			}
		}.start();
	}

	/**
	 * 删除多余的数据
	 */
	public void deleteOverdueHistory() {
		new Thread() {
			public void run() {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -3);
				DBManager.getInstance().deleteOverdueHistory(cal.getTimeInMillis());
			}
		}.start();
	}

	/**
	 * 删除所有数据
	 */
	public void emptyHistory() {
		new Thread() {
			public void run() {
				DBManager.getInstance().emptyHistory();
			}
		}.start();
	}
}
