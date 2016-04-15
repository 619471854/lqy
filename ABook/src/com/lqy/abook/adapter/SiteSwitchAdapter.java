package com.lqy.abook.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;

public class SiteSwitchAdapter extends ArrayAdapter<BookEntity> {
	private LayoutInflater layoutInflater;
	private ViewHolder holder;
	private Site currentSite;

	public SiteSwitchAdapter(Activity activity, List<BookEntity> books, Site currentSite) {
		super(activity, 0, books);
		this.layoutInflater = LayoutInflater.from(activity);
		this.currentSite = currentSite;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.site_switch_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.site_switch_item_title);
			holder.updatedTime = (TextView) convertView.findViewById(R.id.site_switch_item_time);
			holder.newChapter = (TextView) convertView.findViewById(R.id.site_switch_item_new);
			holder.check = convertView.findViewById(R.id.site_switch_item_check);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		BookEntity book = getItem(position);
		holder.title.setText(book.getSite().getName());
		holder.updatedTime.setText("最近更新：" + book.getUpdateTime());
		holder.newChapter.setText("更新至" + Config.blank + "：" + book.getNewChapter());
		holder.check.setVisibility(currentSite == book.getSite() ? View.VISIBLE : View.GONE);
		return convertView;
	}

	class ViewHolder {
		TextView title;
		TextView updatedTime;
		TextView newChapter;
		View check;
	}
}
