package com.lqy.abook.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.SearchAdapter;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.DisplayUtil;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class SearchActivity extends MenuActivity {
	private EditText view_content;
	private ListView listView;

	private List<BookEntity> books;
	private SearchAdapter adapter;
	private int what = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		init();

		searchButtonClick(null);
	}

	private void init() {
		view_content = (EditText) findViewById(R.id.search_edittext);
		view_hint = (TextView) findViewById(R.id.listview_empty);
		listView = (ListView) findViewById(android.R.id.list);

		String search = getIntent().getStringExtra("search");
		if (!Util.isEmpty(search)) {
			search = search.trim();
			view_content.setText(search);
			view_content.setSelection(search.length());
		}

		loadView = findViewById(R.id.loading_view);
		loadView.setLayoutParams(new LinearLayout.LayoutParams(-1, DisplayUtil.dip2px(_this, 100)));
	}

	public void addClick(View v) {
		Util.notCompleted(_this);
	}

	private int parseNum;
	private int counter;;

	public void searchButtonClick(View v) {
		String key = view_content.getText().toString().trim();
		if (Util.isEmpty(key))
			return;
		showProgressBar();
		view_hint.setVisibility(View.GONE);
		listView.setVisibility(View.GONE);
		counter = 0;
		if (books == null)
			books = new ArrayList<BookEntity>();
		else
			books.clear();
		notHideProgress = true;
		parseNum = ParserManager.asynSearch(this, key, ++what);
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		if (this.what != what)
			return;
		counter++;
		List<BookEntity> data = (List<BookEntity>) o;
		if (data != null && data.size() > 0) {
			// 按顺序添加到列表里
			boolean isAdd = false;
			for (BookEntity b : data) {
				isAdd = false;
				for (int i = 0; i < books.size(); i++) {
					if (b.getMatchWords() > books.get(i).getMatchWords()) {
						books.add(i, b);
						isAdd = true;
						break;
					}
				}
				if (!isAdd)
					books.add(b);
			}
		}
		if (counter == parseNum) {
			notHideProgress = false;
			hideProgressBar();
		}
		render();
	}

	private void render() {
		if (counter == parseNum && books.size() == 0) {
			view_hint.setVisibility(View.VISIBLE);
			view_hint.setText("未找到相关的书籍");
		}
		listView.setVisibility(View.VISIBLE);
		if (adapter == null) {
			adapter = new SearchAdapter(this, books);
			listView.addHeaderView(new View(this));// 显示分割线,这里不用在点击的时候arg2--
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					try {
						BookEntity e = adapter.getItem(arg2 - 1);
						Intent intent = new Intent(_this, CoverActivity.class);
						intent.putExtra("book", e);
						startActivity(intent);
						animationRightToLeft();
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
