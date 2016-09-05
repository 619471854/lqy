package com.lqy.abook.db;

import java.util.List;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class FavoriteDao {
	public static final String table_name = "favorite";
	public static final String column_id = "id";// id
	public static final String column_title = "title";// 名
	public static final String column_url = "url";// 地址

	/**
	 * 添加收藏
	 */
	public boolean saveFavorite(final String title, final String url) {
		if (Util.isEmpty(title) || Util.isEmpty(url)) {
			MyLog.e("saveFavorite empty " + title + " " + url);
			return false;
		} else {
			new Thread() {
				public void run() {
					long id = DBManager.getInstance().saveFavorite(title, url);
					MyLog.e("saveHistory id= " + id);
				}
			}.start();
			return true;
		}
	}

	/**
	 * 获取收藏列表
	 */
	public void getFavoriteList(final MenuActivity activity, final int what) {
		new Thread() {
			public void run() {
				List<FavoriteEntity> data = DBManager.getInstance().getFavoriteList();
				activity.sendMsgOnThread(what, data);
			}
		}.start();
	}

	/**
	 * 删除
	 */
	public void deleteFavorite(final MenuActivity activity, final int what, final int id) {
		new Thread() {
			public void run() {
				int re = DBManager.getInstance().deleteFavorite(id);
				activity.sendMsgOnThread(what, id, null);
			}
		}.start();
	}

}
