package com.lqy.abook.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.entity.HistoryGroupEntity;

public class HistoryAdapter extends BaseExpandableListAdapter {
	// 设置组视图的图片
	private Context context;
	private List<HistoryGroupEntity> data;
	private Drawable imgExpand;// 展开的图标
	private Drawable imgCollapse;// 收起的图标

	public HistoryAdapter(Context context, List<HistoryGroupEntity> data) {
		this.context = context;
		this.data = data;
		// 得到图标
		imgExpand = context.getResources().getDrawable(R.drawable.arrow_down);
		imgExpand.setBounds(0, 0, imgExpand.getIntrinsicWidth(), imgExpand.getIntrinsicHeight());
		imgCollapse = context.getResources().getDrawable(R.drawable.arrow_up);
		imgCollapse.setBounds(0, 0, imgCollapse.getIntrinsicWidth(), imgCollapse.getIntrinsicHeight());
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public String getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return data.get(groupPosition).getDate();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		int count = data.get(groupPosition).getListSize();
		return count;
	}

	@Override
	public FavoriteEntity getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return data.get(groupPosition).getHistoryList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.browser_history_list, null);
		}
		TextView view = (TextView) convertView;
		view.setText(getGroup(groupPosition));
		// 打开收起icon设置

		if (getChildrenCount(groupPosition) != 0) {
			if (isExpanded)
				view.setCompoundDrawables(null, null, imgExpand, null);
			else
				view.setCompoundDrawables(null, null, imgCollapse, null);
		} else {
			view.setCompoundDrawables(null, null, null, null);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.browser_favorite_item, null);
		}
		FavoriteEntity e = getChild(groupPosition, childPosition);
		if (e != null) {
			TextView title = (TextView) convertView.findViewById(R.id.browser_favorite_item_title);
			TextView url = (TextView) convertView.findViewById(R.id.browser_favorite_item_url);
			title.setText(e.getTitle());
			url.setText(e.getUrl());
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

};
