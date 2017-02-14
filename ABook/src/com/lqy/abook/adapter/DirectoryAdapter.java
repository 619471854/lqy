package com.lqy.abook.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatusEnum;

public class DirectoryAdapter extends ArrayAdapter<ChapterEntity> {
	private LayoutInflater layoutInflater;
	private ViewHolder holder;

	public DirectoryAdapter(Activity activity, List<ChapterEntity> books) {
		super(activity, 0, books);
		this.layoutInflater = LayoutInflater.from(activity);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.directory_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.directory_item_title);
			holder.vip = convertView.findViewById(R.id.directory_item_vip);
			holder.status = (ImageView) convertView.findViewById(R.id.directory_item_status);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ChapterEntity chapter = getItem(position);
		holder.title.setText(chapter.getName());
		holder.vip.setVisibility(chapter.isVip() ? View.VISIBLE : View.GONE);

		LoadStatusEnum status = chapter.getLoadStatus();
		if (status == LoadStatusEnum.failed) {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setImageResource(R.drawable.status_disconnect);
		} else if (status == LoadStatusEnum.loading) {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setImageResource(R.drawable.status_refresh);
		} else if (status == LoadStatusEnum.completed) {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setImageResource(R.drawable.checked);
		} else {
			holder.status.setVisibility(View.GONE);
		}
		return convertView;
	}

	class ViewHolder {
		View vip;
		TextView title;
		ImageView status;
	}
}
