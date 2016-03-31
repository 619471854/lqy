package com.lqy.abook.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.entity.FavoriteEntity;

public class FavoriteAdapter extends ArrayAdapter<FavoriteEntity> {
	private LayoutInflater layoutInflater;
	private ViewHolder holder;

	public FavoriteAdapter(Activity activity, List<FavoriteEntity> data) {
		super(activity, 0, data);
		this.layoutInflater = LayoutInflater.from(activity);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.browser_favorite_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.browser_favorite_item_title);
			holder.url = (TextView) convertView.findViewById(R.id.browser_favorite_item_url);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		FavoriteEntity book = getItem(position);
		holder.title.setText(book.getTitle());
		holder.url.setText(book.getUrl());
		return convertView;
	}

	class ViewHolder {
		TextView title;
		TextView url;
	}
}
