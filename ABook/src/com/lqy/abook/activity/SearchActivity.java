package com.lqy.abook.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.adapter.SaveLocatedBook;
import com.lqy.abook.adapter.SearchAdapter;
import com.lqy.abook.db.SearchDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.DisplayUtil;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class SearchActivity extends MenuActivity {
	private EditText view_et;
	private ListView listView;
	protected View view_historyHint;

	private List<BookEntity> books;
	private SearchAdapter adapter;
	private SearchDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		init();

	}

	private void init() {
		view_et = (EditText) findViewById(R.id.search_edittext);

		view_hint = (TextView) findViewById(R.id.listview_empty);
		view_historyHint = findViewById(R.id.search_history);
		listView = (ListView) findViewById(android.R.id.list);

		loadView = findViewById(R.id.loading_view);
		loadView.setLayoutParams(new LinearLayout.LayoutParams(-1, DisplayUtil.dip2px(_this, 100)));

		dao = new SearchDao();
		dao.getList(_this, 0);

		// 搜索按钮
		view_et.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
					searchButtonClick(arg0);
					return true;
				}
				return false;
			}
		});

		String search = getIntent().getStringExtra("search");
		if (!Util.isEmpty(search)) {
			search = search.trim();
			view_et.setText(search);
			view_et.setSelection(search.length());
			searchButtonClick(view_et);
		}

		listView.addHeaderView(new View(this));// 显示分割线,这里不用在点击的时候arg2--
	}

	private SaveLocatedBook located;

	public void addClick(View v) {
		if (located == null)
			located = new SaveLocatedBook(_this);
		located.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (located != null && located.onActivityResult(requestCode, resultCode, data))
			return;
		super.onActivityResult(requestCode, resultCode, data);
	}

	private int parseNum;
	private int counter;;

	public void searchButtonClick(View v) {
		String key = view_et.getText().toString().trim();
		if (Util.isEmpty(key))
			return;
		if (adapter == null) {
			view_historyHint.setVisibility(View.GONE);
		}
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

		dao.add(key);

		// 关闭键盘
		Util.hideKeyboard(_this, view_et);
	}

	private int what = 1;

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		if (located != null && located.dealMsg(what, arg1, o))
			return;

		if (what == 0) {
			final List<String> data = (List<String>) o;
			if (data != null && data.size() > 0 && adapter == null) {
				// 输入提示
				listView.setVisibility(View.VISIBLE);
				view_historyHint.setVisibility(View.VISIBLE);
				view_hint.setVisibility(View.GONE);
				final ArrayAdapter hintAdapter = new ArrayAdapter<String>(this, R.layout.search_hint_item, data);
				listView.setAdapter(hintAdapter);

				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						arg2 = arg2 - 1;
						if (arg2 >= 0 && arg2 < data.size() && adapter == null) {
							view_et.setText(data.get(arg2));
							view_et.setSelection(view_et.getText().toString().length());
							searchButtonClick(view_et);
						}
					}
				});
				findViewById(R.id.search_empty).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Util.dialog(_this, "确定要清空搜索记录吗？", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dao.empty();
								listView.setVisibility(View.GONE);
								view_historyHint.setVisibility(View.GONE);
							}
						});
					}
				});
				listView.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
						final int pos = arg2 - 1;
						if (pos >= 0 && pos < data.size() && hintAdapter != null) {
							Util.dialog(_this, "确定要删除此记录吗？", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dao.delete(data.get(pos));
									data.remove(pos);
									hintAdapter.notifyDataSetChanged();
								}
							});
						}
						return true;
					}
				});
			}
		} else {
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
	}

	private void render() {
		if (counter == parseNum && books.size() == 0) {
			view_hint.setVisibility(View.VISIBLE);
			view_hint.setText("未找到相关的书籍");
		}
		listView.setVisibility(View.VISIBLE);
		if (adapter == null) {
			listView.setOnItemLongClickListener(null);
			adapter = new SearchAdapter(this, books);
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
