package com.lqy.abook.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.tool.MatcherTool;

public class MyHelpActivity extends MenuActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_helper);

		TextView textView = (TextView) findViewById(R.id.my_helper);

		String text = textView.getText().toString();
		SpannableString spanableInfo = new SpannableString(text);
		int start = 0;
		Pattern p = Pattern.compile(MatcherTool.SiteReg);
		Matcher m = p.matcher(text);
		while (m.find(start)) {
			String url = text.substring(m.start(), m.end());
			spanableInfo.setSpan(new Clickable(url), m.start(), m.end(), Spanned.SPAN_MARK_MARK);
			start = m.end();
		}
		textView.setText(spanableInfo);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
	}

	class Clickable extends ClickableSpan implements View.OnClickListener {

		private View.OnClickListener mListener;

		public Clickable(final String url) {
			mListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(_this, BrowserActivity.class);
					intent.putExtra("url", url);
					startActivity(intent);
					animationRightToLeft();
				}
			};
		}

		@Override
		public void onClick(View view) {
			mListener.onClick(view);
		}
	}
}
