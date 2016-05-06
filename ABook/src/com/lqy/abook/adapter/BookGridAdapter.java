package com.lqy.abook.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lqy.abook.R;
import com.lqy.abook.activity.BrowserActivity;
import com.lqy.abook.activity.CoverActivity;
import com.lqy.abook.activity.DirectoryActivity;
import com.lqy.abook.activity.MainActivity;
import com.lqy.abook.activity.ReadActivity;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.load.AsyncImageLoader;
import com.lqy.abook.load.Cache;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.NetworkUtils;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.MyAlertDialog;

public class BookGridAdapter extends ArrayAdapter<BookEntity> {
	private MainActivity activity;
	private List<BookEntity> books;
	private LayoutInflater layoutInflater;
	public AsyncImageLoader asyncImageLoader;
	private ViewHolder holder;

	private int pages;
	private Map<Integer, Integer> location;
	private int numPerPage = 9;// 每页的item数量

	public BookGridAdapter(MainActivity activity, List<BookEntity> books, int pages, int rows) {
		super(activity, 0, books);
		this.activity = activity;
		this.books = books;
		this.pages = pages;// 页数
		this.layoutInflater = LayoutInflater.from(activity);
		asyncImageLoader = new AsyncImageLoader();
		numPerPage = rows * 3;// 每页的item数量
		if (books.size() <= numPerPage) {
			return;
		}
		// 如果大于一页，需要调整顺序为：
		// 1 2 3 10 11 12 19 20 21
		// 4 5 6 13 14 15 22 23
		// 7 8 9 16 17 18
		location = new HashMap<Integer, Integer>(pages * numPerPage);
		int key = 0;// 显示位置
		int value = 0;// books的位置
		int clumnNum = pages * 3;// 每一行的列数
		for (int p = 0; p < pages; p++) {
			for (int r = 0; r < rows; r++) {
				key = r * clumnNum + p * 3;
				for (int c = 0; c < 3; c++) {
					location.put(new Integer(key + c), new Integer(value++));
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rows; i++) {
			sb = new StringBuilder();
			for (int j = 0; j < clumnNum; j++) {
				int index = i * clumnNum + j;
				sb.append(" " + index + "=" + getIndex(index));
			}
			MyLog.i(sb.toString());
		}
	}

	@Override
	public int getCount() {
		if (books.size() > numPerPage) {
			return pages * numPerPage;
		}
		return books.size();
	}

	@Override
	public BookEntity getItem(int position) {
		// BookEntity e = books.get(position + start * CONSTANT.numPerPage);
		// return e == null ? new BookEntity() : e;
		if (position >= books.size())
			return null;
		return books.get(position);
	}

	private int getIndex(int position) {
		if (location == null)
			return position;
		else
			return location.get(new Integer(position)).intValue();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.book_grid_item, null);
			holder = new ViewHolder();
			holder.cover = (ImageView) convertView.findViewById(R.id.books_cover);
			holder.status = (ImageView) convertView.findViewById(R.id.books_status);
			holder.noread = (TextView) convertView.findViewById(R.id.books_noread);
			holder.title = (TextView) convertView.findViewById(R.id.books_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		int booksIndex = getIndex(position);// 超过一页，需要调整顺序
		// MyLog.i("getView " + booksIndex);
		BookEntity book = getItem(booksIndex);
		if (book == null) {
			convertView.setVisibility(View.INVISIBLE);
			convertView.setEnabled(false);
			return convertView;
		} else {
			convertView.setVisibility(View.VISIBLE);
			convertView.setEnabled(true);
		}
		if (book.getUnReadCount() > 0) {
			holder.noread.setText(book.getUnReadCount() + CONSTANT.EMPTY);
			holder.noread.setVisibility(View.VISIBLE);
		} else {
			holder.noread.setVisibility(View.GONE);
		}
		holder.title.setText(book.getName());
		LoadStatus status = book.getLoadStatus();
		if (status == LoadStatus.failed) {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setImageResource(R.drawable.status_disconnect);
		} else if (status == LoadStatus.loading) {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setImageResource(R.drawable.status_refresh);
		} else if (status == LoadStatus.hasnew) {
			holder.status.setVisibility(View.VISIBLE);
			holder.status.setImageResource(R.drawable.status_new);
		} else {
			holder.status.setVisibility(View.GONE);
		}
		// 延迟加载图片
		asyncImageLoader.loadDrawable(holder.cover, book.getCover(), R.drawable.book_cover_default);
		holder.cover.setId(booksIndex);
		holder.cover.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					MyLog.i("main books click " + v.getId());
					Cache.setBook(books.get(v.getId()));
					Intent intent = new Intent(activity, ReadActivity.class);
					activity.startActivity(intent);
					activity.animationRightToLeft();
				} catch (Exception e) {
				}
			}
		});
		holder.cover.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				try {
					RelativeLayout layout = (RelativeLayout) v.getParent();
					ImageView view_status = (ImageView) layout.findViewById(R.id.books_status);
					showArrayDialog(view_status, books.get(v.getId()));
				} catch (Exception e) {
				}
				return true;
			}
		});
		return convertView;
	}

	private void showArrayDialog(final ImageView view_status, final BookEntity e) {
		new MyAlertDialog(activity).setTitle(e.getName() + "-" + e.getSite().getName()).setItems(R.array.book_menu, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent;
				switch (which) {
				case 0:// 置顶
					books.remove(e);
					books.add(0, e);
					notifyDataSetChanged();
					new BookDao().updateBookSort(e.getId());
					break;
				case 1:// 更新本书
					if (!NetworkUtils.isNetConnectedRefreshWhereNot()) {
						Util.toast(activity, R.string.net_not_connected);
					} else if (activity.update(e)) {
						view_status.setVisibility(View.VISIBLE);
						view_status.setImageResource(R.drawable.status_refresh);
					}
					break;
				case 2:// 查看详情
					intent = new Intent(activity, CoverActivity.class);
					intent.putExtra("book", e);
					intent.putExtra("onlyRead", true);
					activity.startActivity(intent);
					break;
				case 3:// 查看目录
					intent = new Intent(activity, DirectoryActivity.class);
					Cache.setBook(e);
					activity.startActivity(intent);
					break;
				case 4:
					if (Util.isEmpty(e.getDirectoryUrl())) {
						Util.dialog(activity, "未找到原网页");
					} else {
						intent = new Intent(activity, BrowserActivity.class);
						intent.putExtra("title", e.getName());
						intent.putExtra("url", e.getDirectoryUrl());
						intent.putExtra("class", activity.getClass().getName());
						activity.startActivity(intent);
						activity.animationRightToLeft();
					}
					break;
				case 5:
					intent = new Intent(activity, BrowserActivity.class);
					String key = e.getName() + " " + e.getAuthor();
					intent.putExtra("title", key);
					intent.putExtra("url", "https://www.baidu.com/s?wd=" + key);
					intent.putExtra("class", activity.getClass().getName());
					activity.startActivity(intent);
					activity.animationRightToLeft();
					break;
				case 6:// 删除
					delete(e);
					break;

				default:
					break;
				}
			}
		}).show();
	}

	private void delete(final BookEntity e) {
		Util.dialog(activity, "确定要删除" + e.getName() + "吗？", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.delete(e);
			}
		});
	}

	class ViewHolder {
		ImageView cover;
		TextView noread;
		ImageView status;
		TextView title;
	}
}
