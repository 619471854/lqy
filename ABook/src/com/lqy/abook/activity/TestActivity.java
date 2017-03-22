package com.lqy.abook.activity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.tool.ParserTest;

public class TestActivity extends MenuActivity {
	private EditText view_et;
	private TextView view_result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_test);

		init();

	}

	private void init() {
		view_et = (EditText) findViewById(R.id.search_edittext);
		view_result = (TextView) findViewById(R.id.search_result);
		view_result.setMovementMethod(ScrollingMovementMethod.getInstance());

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

		view_et.setText("诛仙");
	}

	public void searchButtonClick(View v) {
		v.setEnabled(false);
		view_result.setText("开始测试...");

		ParserTest.keyword = view_et.getText().toString().trim();

		for (final SiteEnum site : SiteEnum.allSearchSite) {
			task++;
			testSite(site);
		}
	}

	private int task = 0;

	private void testSite(final SiteEnum site) {
		new Thread() {
			public void run() {
				final ParserTest test = new ParserTest(site.getParser());
				try {
					test.test();
				} catch (Exception e) {
					test.resultData.add(e.toString() + " " + e.getMessage());
				}
				runOnUiThread(new Runnable() {
					public void run() {
						view_result.append("\n++++++++++++++" + site.toString() + "++++++++++++++++++++++");
						for (String re : test.resultData) {
							view_result.append("\n" + re);
						}
						view_result.append("\n----------------------------------------------");
						view_result.append("\n----------------------------------------------");

						task--;
						if (task <= 0) {
							view_result.append("\n----------------结束------------------");
							task = 0;
							findViewById(R.id.search_button).setEnabled(true);
						}
					}
				});
			};

		}.start();
	}
}
