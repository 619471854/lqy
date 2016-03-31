package com.lqy.abook.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.SiteSwitchAdapter;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class SiteSwitchActivity extends MenuActivity {
	private TextView view_title;
	private ListView listView;

	private List<BookEntity> books;
	private SiteSwitchAdapter adapter;

	private String name;
	private String author;
	private int site;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.site_switch);

		init();

	}

	private void init() {
		view_title = (TextView) findViewById(R.id.toolbar_title);
		view_hint = (TextView) findViewById(R.id.listview_empty);
		listView = (ListView) findViewById(android.R.id.list);
		name = getIntent().getStringExtra("name");
		author = getIntent().getStringExtra("author");
		site = getIntent().getIntExtra("site", 0);

		if (Util.isEmpty(name) || Util.isEmpty(author)) {
			finish();
			return;
		}
		view_title.setText(name);

		showProgressBar();
		view_hint.setVisibility(View.GONE);
		listView.setVisibility(View.GONE);
		ParserManager.asynSearchSite(_this, name, author, 0);
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		view_hint.setVisibility(View.VISIBLE);
		listView.setVisibility(View.VISIBLE);
		if (books == null)
			books = new ArrayList<BookEntity>();
		else
			books.clear();
		if (o != null) {
			books.addAll((List<BookEntity>) o);
		}
		if (adapter == null) {
			adapter = new SiteSwitchAdapter(_this, books, site);
			// listView.addHeaderView(new View(_this));// 显示分割线,这里不用在点击的时候arg2--
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					try {
						BookEntity e = adapter.getItem(arg2);
						if (e.getSite().ordinal() != site) {
							Intent intent = new Intent(_this, CoverActivity.class);
							intent.putExtra("book", e);
							setResult(RESULT_OK, intent);
						}
						finish();
						animationLeftToRight();
					} catch (Exception e) {
						MyLog.e(e);
					}
				}
			});
		} else {
			adapter.notifyDataSetChanged();
		}
	}

}
