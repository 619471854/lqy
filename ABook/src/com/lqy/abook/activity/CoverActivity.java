package com.lqy.abook.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.load.AsyncTxtLoader;
import com.lqy.abook.load.Cache;
import com.lqy.abook.load.ImageLoader;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class CoverActivity extends MenuActivity {
	private BookEntity book;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_cover);

		init(getIntent());
	}

	private ImageView view_img;
	private TextView view_bookname;
	private TextView view_author;
	private View view_completed;
	private TextView view_describe;
	private TextView view_newChapter;
	private TextView view_site;
	private TextView view_intro;
	private TextView btn_save;

	private void init(Intent intent) {
		BookEntity _book = (BookEntity) intent.getSerializableExtra("book");
		boolean onlyRead = intent.getBooleanExtra("onlyRead", false);
		if (book == null && _book == null) {
			finish();// 没有数据
			return;
		} else if (book != null && _book == null)
			return;// 不用刷新数据
		book = _book;
		if (view_img == null) {
			view_img = (ImageView) findViewById(R.id.book_cover_img);
			view_bookname = (TextView) findViewById(R.id.toolbar_title);
			view_author = (TextView) findViewById(R.id.book_cover_author);
			view_completed = findViewById(R.id.book_cover_completedbook);
			view_describe = (TextView) findViewById(R.id.book_cover_describe);
			view_newChapter = (TextView) findViewById(R.id.book_cover_new_chapter);
			view_site = (TextView) findViewById(R.id.book_cover_site);
			view_intro = (TextView) findViewById(R.id.book_cover_intro);
			btn_save = (TextView) findViewById(R.id.book_cover_save);
		}

		view_bookname.setText(book.getName());
		view_author.setText(book.getAuthor());
		view_site.setText("下载点：" + book.getSite().getName());
		showDetailInfo();
		if (onlyRead) {
			findViewById(R.id.book_cover_menu_layout).setVisibility(View.GONE);
			view_site.setCompoundDrawables(null, null, null, null);
			view_site.setClickable(false);
			view_newChapter.setClickable(false);
		} else {
			getDetail();
		}
	}

	/**
	 * 显示需要更新的信息
	 */
	private void showDetailInfo() {
		// 封面
		ImageLoader.load(_this, view_img, book.getCover(), null);
		// 类别和字数
		String describe = "暂未分类";
		if (!Util.isEmpty(book.getType()))
			describe = book.getType();
		if (book.getWords() > 0) {
			int w = book.getWords() / 10000;
			int q = book.getWords() % 10000 / 1000;
			if (q == 0 || w > 10)
				describe += "  " + w + "万字";
			else {
				describe += "  " + w + "." + q + "万字";
			}
		}
		view_describe.setText(describe.trim());

		if (book.isCompleted())
			view_completed.setVisibility(View.VISIBLE);
		else
			view_completed.setVisibility(View.GONE);

		view_newChapter.setText("最新章节:" + Util.toString(book.getNewChapter()));
		view_intro.setText(book.getTip());
	}

	private boolean isLoading = true;

	private void getDetail() {
		new Thread() {
			public void run() {
				BookDao dao = new BookDao();
				if (dao.checkBook(book)) {
					sendMsgOnThread(0, null);
				} else if (!ParserManager.getBookDetail(book)) {
					sendErrorOnThread("加载失败");
				} else {
					sendMsgOnThread(1, null);
				}

			};
		}.start();
	}

	private void saveBook(final boolean isToRead) {
		if (isToRead && book.getId() != CONSTANT.MSG_ERROR) {
			dealMsg(4, 0, "true");
			return;
		}
		if (!isToRead) {
			btn_save.setText("缓存中..");
			btn_save.setTextColor(getResources().getColor(R.color.hint));
			btn_save.setEnabled(false);
			btn_save.setClickable(false);
		}
		new Thread() {
			public void run() {
				if (book.getId() == CONSTANT.MSG_ERROR)
					new BookDao().addBook(book);
				if (isToRead) {
					sendMsgOnThread(4, "true");
					return;
				} else {
					sendMsgOnThread(4, "false");
				}
				if (book.getId() != CONSTANT.MSG_ERROR) {
					List<ChapterEntity> data = ParserManager.getDict(book);
					if (data != null) {
						LoadManager.saveDirectory(book.getId(), data);
						sendMsgOnThread(2, data);
					} else {
						book.setLoadStatus(LoadStatus.failed);
						sendErrorOnThread("加载失败");
					}
				} else {
					book.setLoadStatus(LoadStatus.failed);
					sendErrorOnThread("保存失败");
				}
			}
		}.start();
	}

	@Override
	protected void dealErrorMsg(int what, Object o) {
		isLoading = false;
	};

	@Override
	protected void dealMsg(int what, int arg1, Object o) {
		switch (what) {
		case 0:// 该书已缓存
			isLoading = false;
			showDetail(true);
			break;
		case 1:
			isLoading = false;
			showDetail(false);
			break;
		case 2:
			// 获取目录后下载
			ArrayList<ChapterEntity> chapters = (ArrayList<ChapterEntity>) o;
			Cache.setChapters(chapters);
			if (!AsyncTxtLoader.getInstance().load(this, book, chapters, 3)) {
				btn_save.setText("已缓存");
			}
			break;
		case 3:
			if (arg1 == 100) {
				btn_save.setText("已缓存");
				LoadManager.asynSaveDirectory(book.getId(), Cache.getChapters());
			} else {
				btn_save.setText("缓存 " + arg1 + "%");
			}
			break;
		case 4:// 缓存或者开始阅读本书
			boolean isToRead = "true".equals(o);
			MainActivity.isAddBook = true;
			book = Cache.setBook(book);
			book.setReadBegin(0);
			if (isToRead) {
				startActivity(new Intent(_this, ReadActivity.class));
				animationRightToLeft();
			}
			break;
		default:
			break;
		}
	}

	private void showDetail(boolean isSave) {
		if (isSave) {
			btn_save.setText("已缓存");
			btn_save.setEnabled(false);
			btn_save.setClickable(false);
			btn_save.setTextColor(getResources().getColor(R.color.hint));

			if (book.getId() != CONSTANT.MSG_ERROR && AsyncTxtLoader.isRunning(book.getId())) {
				btn_save.setText("缓存中..");
				AsyncTxtLoader.getInstance().waitToOverAndShowProgress(this, book.getId(), 3);
			}
		}
		showDetailInfo();
	}

	public void sendButtonClick(View v) {
		if (isLoading && v.getId() != R.id.book_cover_site) {
			Util.toast(_this, "正在加载，请稍后");
			return;
		}
		Intent intent;
		switch (v.getId()) {
		case R.id.book_cover_site:
			intent = new Intent(_this, SiteSwitchActivity.class);
			intent.putExtra("book", book);
			startActivityForResult(intent, 0);
			animationRightToLeft();
			break;
		case R.id.book_cover_dict:
			MainActivity.isAddBook = true;
			book = Cache.setBook(book);
			intent = new Intent(_this, DirectoryActivity.class);
			startActivity(intent);
			animationRightToLeft();
			break;
		case R.id.book_cover_save:// 缓存
			MyLog.i("缓存 :" + book.getId());
			saveBook(false);
			break;
		case R.id.book_cover_read:
			saveBook(true);
			break;
		case R.id.book_cover_new_chapter:
			MainActivity.isAddBook = true;
			book = Cache.setBook(book);
			intent = new Intent(_this, DirectoryActivity.class);
			intent.putExtra("last", true);
			startActivity(intent);
			animationRightToLeft();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			init(data);
		}
	}
}
