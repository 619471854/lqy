package com.lqy.abook.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.SiteSwitchAdapter;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.DisplayUtil;
import com.lqy.abook.tool.MyLog;

public class SiteSwitchActivity extends MenuActivity {
	private TextView view_title;
	private ListView listView;

	private List<BookEntity> books;
	private SiteSwitchAdapter adapter;
	private int parseNum;
	private int counter;;

	private BookEntity book;

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
		book = (BookEntity) getIntent().getSerializableExtra("book");

		if (book == null) {
			finish();
			return;
		}
		view_title.setText(book.getName());

		loadView = findViewById(R.id.loading_view);
		loadView.setLayoutParams(new LinearLayout.LayoutParams(-1, DisplayUtil.dip2px(_this, 100)));
		showProgressBar();

		view_hint.setVisibility(View.GONE);
		listView.setVisibility(View.GONE);

		books = new ArrayList<BookEntity>();
		books.add(book);

		notHideProgress = true;
		parseNum = ParserManager.asynSearchSite(_this, book, 0);
	}

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		counter++;
		BookEntity b = (BookEntity) o;
		if (b != null) {
			books.add(b);
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
			adapter = new SiteSwitchAdapter(_this, books, book.getSite());
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					try {
						BookEntity e = adapter.getItem(arg2);
						if (e.getSite() != book.getSite()) {
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
