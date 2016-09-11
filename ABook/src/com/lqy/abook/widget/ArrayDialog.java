package com.lqy.abook.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.tool.Util;

/**
 * 从界面下部弹出的 Dialog
 */
public class ArrayDialog extends Dialog {
	private Context activity;

	public ArrayDialog(Context context) {
		super(context, R.style.array_dialog);
		this.activity = context;
		setView();
	}

	private TextView view_title;
	private ListView listView;
	private View view_btn;

	private void setView() {
		View v = LayoutInflater.from(activity).inflate(R.layout.array_dialog, null);
		this.setContentView(v);

		v.findViewById(R.id.toolbar_btn_back).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cancel();
			}
		});

		view_title = (TextView) v.findViewById(R.id.toolbar_title);
		view_btn = findViewById(R.id.toolbar_btn_ok);

		listView = (ListView) findViewById(android.R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (isMultiple) {// 多选
					CheckBox cb = (CheckBox) arg1.findViewById(R.id.array_dialog_item_check);
					if (cb != null)
						cb.setChecked(!cb.isChecked());
					select[arg2] = cb.isChecked();
				} else {// 单选
					if (singleListener != null)
						singleListener.onClick(ArrayDialog.this, arg2);
					cancel();
				}
			}
		});

	}

	private String[] titles;
	private DialogInterface.OnClickListener singleListener;
	private Click multipleListener;
	private boolean isMultiple = false;
	private boolean[] select;

	public ArrayDialog setTitle(String title) {
		view_title.setText(title);
		return this;
	}

	public ArrayDialog setItems(String[] _titles, int select, DialogInterface.OnClickListener listener) {
		this.titles = _titles;
		this.singleListener = listener;
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		isMultiple = false;

		Adapter adapter = new Adapter(activity, 0, titles);
		listView.setAdapter(adapter);

		if (select < titles.length)
			listView.setItemChecked(select, true);
		return this;
	}

	public ArrayDialog setItems(String[] _titles, boolean[] _select, Click listener) {
		this.titles = _titles;
		this.multipleListener = listener;
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		isMultiple = true;

		Adapter adapter = new Adapter(activity, 0, titles);
		listView.setAdapter(adapter);

		view_btn.setVisibility(View.VISIBLE);
		view_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				List<Integer> data = new ArrayList<Integer>();
				boolean hasChecked = false;
				for (int i = 0; i < titles.length; i++) {
					if (listView.isItemChecked(i)) {
						hasChecked = true;
						break;
					}
				}
				if (!hasChecked) {
					Util.dialog(activity, "至少要选一个网站");
				} else {
					if (multipleListener != null)
						multipleListener.onClick(select);
					cancel();
				}
			}
		});
		if (_select == null) {
			_select = new boolean[_titles.length];
		} else {
			for (int i = 0; i < _select.length; i++) {
				listView.setItemChecked(i, _select[i]);
			}
		}
		this.select = _select;

		return this;
	}

	public void show() {
		try {
			super.show();
		} catch (Exception e) {
		}
	}

	public void cancel() {
		try {
			if (isShowing())
				dismiss();
		} catch (Exception e) {
		}
	}

	public interface Click {
		public void onClick(boolean[] select);
	}

	class Adapter extends ArrayAdapter<String> {

		private LayoutInflater inflater;
		private ViewHolder holder;

		public Adapter(Context context, int res, String[] groups) {
			super(context, res, groups);
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.array_dialog_item, null);
				holder.name = (TextView) convertView.findViewById(R.id.array_dialog_item_name);
				holder.check = (CheckBox) convertView.findViewById(R.id.array_dialog_item_check);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			convertView.setId(position);

			holder.name.setText(getItem(position));
			holder.check.setChecked(listView.isItemChecked(position));
			return convertView;
		}
	}

	class ViewHolder {
		TextView name;
		CheckBox check;
	}
}