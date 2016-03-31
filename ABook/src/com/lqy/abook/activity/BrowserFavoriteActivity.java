package com.lqy.abook.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.FavoriteAdapter;
import com.lqy.abook.db.FavoriteDao;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.load.Assits;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class BrowserFavoriteActivity extends MenuActivity {
	private ListView listView;

	private List<FavoriteEntity> data;
	private List<FavoriteEntity> favorite;// 收藏夹
	private List<FavoriteEntity> recommendation;// 推荐
	private FavoriteAdapter adapter;
	private FavoriteDao dao = new FavoriteDao();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser_favorite);

		init();
	}

	private void init() {
		view_hint = (TextView) findViewById(R.id.listview_empty);
		listView = (ListView) findViewById(android.R.id.list);
		listView.setEmptyView(view_hint);
		// new HistoryDao().getHistoryList(_this, 0);
		data = new ArrayList<FavoriteEntity>();
		int index = getIntent().getIntExtra("index", 0);
		if (index == 1) {
			dao.getFavoriteList(_this, 0);
		} else {
			Assits.loadFavorite(_this, "favorite.text", 1);
		}
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		switch (what) {
		case 0:
			favorite = (List<FavoriteEntity>) o;
			render(favorite);
			break;
		case 1:
			recommendation = (List<FavoriteEntity>) o;
			render(recommendation);
			break;
		case 2:// 删除
			for (int i = 0; i < favorite.size(); i++) {
				if (favorite.get(i).getId() == arg1) {
					favorite.remove(i);
					break;
				}
			}
			adapter.notifyDataSetChanged();
			break;
		}
	}

	private void render(List<FavoriteEntity> _data) {
		data.clear();
		data.addAll(_data);
		if (adapter == null) {
			adapter = new FavoriteAdapter(this, data);
			listView.addHeaderView(new View(this));// 显示分割线,这里不用在点击的时候arg2--
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					try {
						FavoriteEntity e = adapter.getItem(arg2 - 1);
						Intent intent = new Intent(_this, BrowserActivity.class);
						intent.putExtra("title", e.getTitle());
						intent.putExtra("url", e.getUrl());
						setResult(RESULT_OK, intent);
						finish();
						animationLeftToRight();
					} catch (Exception e) {
						MyLog.e(e);
					}
				}
			});
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					try {
						final FavoriteEntity e = adapter.getItem(arg2 - 1);
						if (e.getId() != CONSTANT.ID_DEFAULT) {
							Util.dialog(_this, "确定要删除吗？", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dao.deleteFavorite(_this, 2, e.getId());
								}
							});
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
					return true;
				}
			});
		} else {
			adapter.notifyDataSetChanged();
		}

	}
}
