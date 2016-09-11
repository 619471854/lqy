package com.lqy.abook.activity;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.HistoryAdapter;
import com.lqy.abook.db.HistoryDao;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.entity.HistoryGroupEntity;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class BrowserHistoryActivity extends MenuActivity {
	private ExpandableListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_history);

		// new HistoryDao().saveHistory("百度一下", "https://www.baidu.com/");
		// new HistoryDao().saveHistory("指掌天下项目管理系统",
		// "http://192.168.1.40:81/zentao/bug-view-955.html");
		// new HistoryDao().saveHistory("百度翻译",
		// "http://fanyi.baidu.com/?aldtype=16047#auto/zh/");

		view_hint = (TextView) findViewById(R.id.listview_empty);
		listView = (ExpandableListView) findViewById(android.R.id.list);
		new HistoryDao().getHistoryList(_this, 0);
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		setListViev((List<HistoryGroupEntity>) o);
	}

	public void emptyClick(View v) {

		Util.dialog(_this, "确定要清除历史纪录吗", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new HistoryDao().emptyHistory();
				listView.setVisibility(View.GONE);
				view_hint.setVisibility(View.VISIBLE);
				view_hint.setText("没有浏览记录");
			}
		});
	}

	/**
	 * 设置listView
	 */
	private void setListViev(final List<HistoryGroupEntity> data) {
		if (data == null || data.size() == 0) {
			listView.setVisibility(View.GONE);
			view_hint.setVisibility(View.VISIBLE);
			view_hint.setText("没有浏览记录");
			return;
		}
		view_hint.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
		HistoryAdapter adapter = new HistoryAdapter(this, data);
		listView.setAdapter(adapter);
		listView.setGroupIndicator(null);
		listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				try {
					FavoriteEntity e = data.get(groupPosition).getHistoryList().get(childPosition);
					Intent intent = new Intent(_this, BrowserActivity.class);
					intent.putExtra("title", e.getTitle());
					intent.putExtra("url", e.getUrl());
					setResult(RESULT_OK, intent);
					finish();
					animationLeftToRight();
				} catch (Exception e) {
					MyLog.e(e);
				}
				return false;
			}
		});
		// 展开第一个组
		listView.expandGroup(0);
	}
}
