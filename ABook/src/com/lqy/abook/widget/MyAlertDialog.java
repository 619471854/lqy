package com.lqy.abook.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.tool.DisplayUtil;
import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class MyAlertDialog extends AlertDialog.Builder {

	private int buttonCount = 0;// 按钮数量
	public AlertDialog dialog;
	public Context context;
	private boolean isClickBtnCancel = true;// 点击按钮是否关闭对话框

	private TextView view_title;// 标题
	private TextView view_msg;// 内容
	private TextView positiveButton;// 3个按钮
	private TextView neutralButton;
	private TextView negativeButton;
	private LinearLayout lay_button;
	private View view_parting;// 标题下面的分割线
	private View view_left;// 3个按钮的左右间隙，当只显示一个按钮时 visible
	private View view_right;
	private LinearLayout lay_view;// msg view
	private boolean isUseCustomStyle = false;// 使用自定义样式,false 会提示错误
	private boolean btnIsAutowidth = false;// 3个按钮自动宽度,3个按钮宽度随文本长度适配

	public MyAlertDialog(Context context) {
		super(context);

		this.context = context;
		init();
	}

	public MyAlertDialog(Context context, boolean btnIsAutowidth) {
		super(context);

		this.context = context;
		this.btnIsAutowidth = btnIsAutowidth;
		init();
	}

	public boolean isShowing() {
		return dialog != null && dialog.isShowing();
	}

	private void init() {
		dialog = this.create();
		dialog.setView(getView(), 0, 0, 0, 0);
	}

	protected View getView() {
		View view = LayoutInflater.from(context).inflate(R.layout.my_dialog, null);
		view_title = (TextView) view.findViewById(R.id.my_alert_dialog_title);
		view_msg = (TextView) view.findViewById(R.id.my_alert_dialog_msg);
		view_parting = view.findViewById(R.id.my_alert_dialog_parting);
		view_left = view.findViewById(R.id.my_alert_dialog_left);
		view_right = view.findViewById(R.id.my_alert_dialog_right);
		lay_button = (LinearLayout) view.findViewById(R.id.my_alert_dialog_btn_lay);
		lay_view = (LinearLayout) view.findViewById(R.id.my_alert_dialog_view);
		positiveButton = (TextView) view.findViewById(R.id.my_alert_dialog_ok);
		neutralButton = (TextView) view.findViewById(R.id.my_alert_dialog_abolish);
		negativeButton = (TextView) view.findViewById(R.id.my_alert_dialog_cancel);

		if (btnIsAutowidth) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, DisplayUtil.dip2px(context, 40), 3);
			int padding = DisplayUtil.dip2px(context, 5);
			params.setMargins(padding, 0, padding, 0);
			positiveButton.setLayoutParams(params);
			neutralButton.setLayoutParams(params);
			negativeButton.setLayoutParams(params);
		}
		return view;
	}

	/**
	 * 点击按钮是否关闭对话框
	 * 
	 * @param isClickBtnCancel
	 */
	public void setClickBtnCancel(boolean isClickBtnCancel) {
		this.isClickBtnCancel = isClickBtnCancel;
	}

	@Override
	public MyAlertDialog setTitle(CharSequence title) {
		view_title.setText(title);
		view_title.setVisibility(View.VISIBLE);
		view_parting.setVisibility(View.VISIBLE);
		return this;
	}

	public MyAlertDialog setTitleSingleLine(boolean singleLine) {
		view_title.setSingleLine(singleLine);
		return this;
	}

	@Override
	public MyAlertDialog setTitle(int titleId) {
		view_title.setText(titleId);
		view_title.setVisibility(View.VISIBLE);
		view_parting.setVisibility(View.VISIBLE);
		return this;
	}

	@Override
	public MyAlertDialog setMessage(CharSequence message) {
		isUseCustomStyle = true;
		view_msg.setText(message);
		view_msg.setVisibility(View.VISIBLE);
		// 支持滚动
		view_msg.setMovementMethod(ScrollingMovementMethod.getInstance());
		return this;
	}

	@Override
	public MyAlertDialog setMessage(int messageId) {
		isUseCustomStyle = true;
		view_msg.setText(messageId);
		view_msg.setVisibility(View.VISIBLE);
		view_msg.setMovementMethod(ScrollingMovementMethod.getInstance());
		return this;
	}

	@Override
	public MyAlertDialog setView(View view) {
		isUseCustomStyle = true;

		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lay_view.addView(view, p);
		lay_view.setVisibility(View.VISIBLE);
		return this;
	}

	public MyAlertDialog setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
		isUseCustomStyle = true;
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lay_view.addView(view, p);
		lay_view.setVisibility(View.VISIBLE);

		MarginLayoutParams params = (MarginLayoutParams) lay_view.getLayoutParams();
		params.setMargins(viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
		lay_view.setLayoutParams(params);
		return this;
	}

	/**
	 * 数组列表对话框
	 */
	public MyAlertDialog setItems(String[] array, OnClickListener listener) {
		if (array != null) {
			ListView view = getArrayView(array, listener);
			setView(view, 0, 0, 0, 0);
		}
		return this;
	}

	/**
	 * setItem方法
	 */
	public MyAlertDialog setItems(int itemsId, OnClickListener listener) {
		return setItems(context.getResources().getStringArray(itemsId), listener);
	}

	/**
	 * setItem方法
	 */
	public MyAlertDialog setItems(CharSequence[] items, OnClickListener listener) {
		// List<CharSequence> data = Arrays.asList(items);
		// String[] array = (String[]) data.toArray(new String[data.size()]);
		String[] array = new String[items.length];
		for (int i = 0; i < items.length; i++)
			array[i] = items[i].toString();

		return setItems(array, listener);
	}

	/**
	 * 数组列表对话框列表
	 */
	private ListView getArrayView(String[] array, final OnClickListener listener) {
		ListView listView = new ListView(context);
		listView.setBackgroundColor(Color.TRANSPARENT);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setDivider(new ColorDrawable(context.getResources().getColor(R.color.line_color)));
		listView.setDividerHeight(1);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.my_dialog_list, array);
		listView.setAdapter(adapter);
		// 点击事件
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (listener != null)
					listener.onClick(dialog, arg2);
				if (isClickBtnCancel)
					cancel();
			}
		});
		return listView;
	}

	/**
	 * list列表对话框
	 */
	public MyAlertDialog setList(BaseAdapter adapter) {
		return setList(adapter, null);
	}

	/**
	 * list列表对话框
	 */
	public MyAlertDialog setList(BaseAdapter adapter, OnClickListener listener) {
		if (adapter != null) {
			ListView view = getListView(adapter, listener);
			int p = (int) (GlobalConfig.getDensity() * 10);
			setView(view, p, 0, p, 0);
		}
		return this;
	}

	/**
	 * list列表对话框列表
	 */
	private ListView getListView(BaseAdapter adapter, final OnClickListener listener) {
		ListView listView = new ListView(context);
		listView.setBackgroundColor(Color.WHITE);
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setDivider(new ColorDrawable(context.getResources().getColor(R.color.line_color)));
		listView.setDividerHeight(1);
		listView.setAdapter(adapter);
		// 点击事件
		if (listener != null)
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					listener.onClick(dialog, arg2);
					if (isClickBtnCancel)
						cancel();
				}
			});
		return listView;
	}

	@Override
	public MyAlertDialog setCancelable(boolean cancelable) {
		dialog.setCancelable(cancelable);
		return this;
	}

	@Override
	public AlertDialog show() {
		try {
			if (isUseCustomStyle) {
				if (buttonCount > 1 || (btnIsAutowidth && buttonCount > 0)) {
					view_left.setVisibility(View.GONE);
					view_right.setVisibility(View.GONE);
				} else if (buttonCount < 1)
					lay_button.setVisibility(View.GONE);
				dialog.show();
				return dialog;
			} else {
				Util.dialog(context, "dialog show error!");
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	@Override
	public MyAlertDialog setPositiveButton(int textId, OnClickListener listener) {
		return setPositiveButton(context.getString(textId), listener);
	}

	@Override
	public MyAlertDialog setNeutralButton(int textId, OnClickListener listener) {
		return setNeutralButton(context.getString(textId), listener);
	}

	@Override
	public MyAlertDialog setNegativeButton(int textId, OnClickListener listener) {
		return setNegativeButton(context.getString(textId), listener);
	}

	@Override
	public MyAlertDialog setPositiveButton(CharSequence text, final OnClickListener listener) {
		try {

			positiveButton.setText(text);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						if (listener != null)
							listener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						if (isClickBtnCancel)
							dialog.cancel();
					} catch (Exception e) {
						cancel();
						MyLog.e(e);
					}
				}
			});
			if (positiveButton.getVisibility() != View.VISIBLE)
				buttonCount++;
			positiveButton.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			MyLog.e(e);
		}
		return this;
	}

	@Override
	public MyAlertDialog setNeutralButton(CharSequence text, final OnClickListener listener) {
		try {
			neutralButton.setText(text);
			neutralButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						if (listener != null)
							listener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
						if (isClickBtnCancel)
							dialog.cancel();
					} catch (Exception e) {
						cancel();
						MyLog.e(e);
					}
				}
			});
			if (neutralButton.getVisibility() != View.VISIBLE)
				buttonCount++;
			neutralButton.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			MyLog.e(e);
		}
		return this;
	}

	@Override
	public MyAlertDialog setNegativeButton(CharSequence text, final OnClickListener listener) {
		try {
			negativeButton.setText(text);
			negativeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						if (listener != null)
							listener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
						cancel();
					} catch (Exception e) {
						MyLog.e(e);
						cancel();
					}
				}
			});
			if (negativeButton.getVisibility() != View.VISIBLE)
				buttonCount++;
			negativeButton.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			MyLog.e(e);
		}
		return this;
	}

	public void cancel() {
		if (dialog != null && dialog.isShowing())
			dialog.cancel();
	}

}
