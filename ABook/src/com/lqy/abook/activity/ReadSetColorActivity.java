package com.lqy.abook.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.entity.FontMode;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.CallBackListener;
import com.lqy.abook.widget.ColorPicker;

public class ReadSetColorActivity extends MenuActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_set_color);

		init();
	}

	private TextView view_current;
	private ColorPicker picker;
	private RadioGroup rg;
	private SharedPreferences sp;
	private FontMode customerMode;

	private void init() {
		sp = getSharedPreferences(CONSTANT.SP_READ, 0);
		customerMode = FontMode.getCustomer(sp);

		view_current = (TextView) findViewById(R.id.set_color_current);
		picker = (ColorPicker) findViewById(R.id.set_color_picker);
		rg = (RadioGroup) findViewById(R.id.set_color_rg);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.set_color_text) {
					picker.setColor(customerMode.textColor);
				} else {
					picker.setColor(customerMode.bgColor);
				}
			}
		});
		picker.setColorChangelistener(new CallBackListener() {

			@Override
			public void callBack(String... params) {
				try {
					int color = Integer.parseInt(params[0]);
					if (rg.getCheckedRadioButtonId() == R.id.set_color_text) {
						customerMode.textColor = color;
						view_current.setTextColor(customerMode.textColor);
					} else {
						customerMode.bgColor = color;
						view_current.setBackgroundColor(customerMode.bgColor);
					}
				} catch (Exception e) {
				}
			}
		});
		picker.setColor(customerMode.textColor);
		view_current.setTextColor(customerMode.textColor);
		view_current.setBackgroundColor(customerMode.bgColor);
	}

	public void sendButtonClick(View v) {
		switch (v.getId()) {
		case R.id.toolbar_btn_default:
			customerMode = FontMode.getCustomerDefault();
			view_current.setTextColor(customerMode.textColor);
			view_current.setBackgroundColor(customerMode.bgColor);
			if (rg.getCheckedRadioButtonId() == R.id.set_color_text) {
				picker.setColor(customerMode.textColor);
			} else {
				picker.setColor(customerMode.bgColor);
			}
			break;
		case R.id.toolbar_btn_ok:
			if (ReadActivity.getInstance() == null)
				customerMode.save(sp);
			else
				ReadActivity.getInstance().setFontMode(customerMode);
			cancelButtonClick(v);
			break;
		}
	}

}
