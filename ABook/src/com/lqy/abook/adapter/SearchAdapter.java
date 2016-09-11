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
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.load.AsyncImageLoader;
import com.lqy.abook.tool.Util;

public class SearchAdapter extends ArrayAdapter<BookEntity> {
	private LayoutInflater layoutInflater;
	public AsyncImageLoader asyncImageLoader;
	private ViewHolder holder;

	public SearchAdapter(Activity activity, List<BookEntity> books) {
		super(activity, 0, books);
		this.layoutInflater = LayoutInflater.from(activity);
		asyncImageLoader = new AsyncImageLoader();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.search_item, null);
			holder = new ViewHolder();
			holder.cover = (ImageView) convertView.findViewById(R.id.search_item_cover);
			holder.title = (TextView) convertView.findViewById(R.id.search_item_title);
			holder.type = (TextView) convertView.findViewById(R.id.search_item_type);
			holder.author = (TextView) convertView.findViewById(R.id.search_item_author);
			holder.tip = (TextView) convertView.findViewById(R.id.search_item_tips);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		BookEntity book = getItem(position);
		holder.title.setText(book.getName());
		holder.author.setText(book.getSite().getName() + "  " + book.getAuthor());
		holder.type.setText(Util.isEmpty(book.getType()) ? "暂未分类" : book.getType());
		holder.tip.setText(book.getTip());
		// 延迟加载图片
		asyncImageLoader.loadDrawable(holder.cover, book.getCover(), R.drawable.book_cover_default, false);
		return convertView;
	}

	class ViewHolder {
		ImageView cover;
		TextView title;
		TextView type;
		TextView author;
		TextView tip;
	}
}
