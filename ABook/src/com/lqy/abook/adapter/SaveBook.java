package com.lqy.abook.adapter;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;

import com.lqy.abook.R;
import com.lqy.abook.activity.BrowserActivity;
import com.lqy.abook.activity.LoadingActivity;
import com.lqy.abook.activity.MainActivity;
import com.lqy.abook.activity.SearchActivity;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.parser.ParserManager;
import com.lqy.abook.parser.site.ParserOther;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.MyAlertDialog;

public class SaveBook {
	private BrowserActivity activity;
	public static final int WHAT_SAVEBOOK1 = 1001;
	public static final int WHAT_SAVEBOOK2 = 1002;
	public static final int WHAT_SAVEBOOK3 = 1003;
	public static final int WHAT_SAVEBOOK4 = 1004;
	public static final int WHAT_SAVEBOOK5 = 1005;
	public static final int WHAT_SAVEBOOK6 = 1006;

	public SaveBook(BrowserActivity a) {
		activity = a;

		activity.findViewById(R.id.browser_save).setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(final View v) {
				new MyAlertDialog(activity).setItems(R.array.save_menu, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveBookClick(which);
					}
				}).show();
				return true;
			}
		});
	}

	public boolean dealMsg(int what, int arg1, Object o, String title) {
		switch (what) {
		case WHAT_SAVEBOOK1:// 添加到书架
			MyLog.i("savebook1");
			activity.showLoadingDialog("正在获取小说目录");
			String[] params = (String[]) o;
			ParserManager.parserBrowser(activity, activity.webView.getUrl(), params[1], params[0], WHAT_SAVEBOOK2, true);
			break;
		case WHAT_SAVEBOOK2:// 添加到书架
			MyLog.i("savebook2 ", o);
			saveBook((BookAndChapters) o, title);
			break;
		case WHAT_SAVEBOOK3:// 添加到书架成功，启动首页
			MyLog.i("savebook3 ", o);
			if (MainActivity.getInstance() == null) {
				activity.startActivity(new Intent(activity, LoadingActivity.class));
				activity.finish();
			} else {
				MainActivity.isAddBook = true;
				Intent intent = new Intent(activity, MainActivity.class);
				intent.putExtra("book", (BookEntity) o);
				activity.startActivity(intent);
				activity.animationRightToLeft();
				activity.finish();
			}
			break;
		case WHAT_SAVEBOOK4:// 添加图片到书架
			MyLog.i("savebook4");
			activity.showLoadingDialog("正在获取小说目录");
			String[] params2 = (String[]) o;
			ParserManager.parserBrowser(activity, activity.webView.getUrl(), params2[1], params2[0], WHAT_SAVEBOOK5, false);
			break;
		case WHAT_SAVEBOOK5:// 添加到书架
			MyLog.i("savebook2 ", o);
			saveImgs((BookAndChapters) o, title);
			break;
		case WHAT_SAVEBOOK6:// 添加本页内容到书架
			MyLog.i("savebook6");
			String text = ParserOther.parseChapterDetail(((String[]) o)[1]);
			if (Util.isEmpty(text)) {
				Util.dialog(activity, "获取失败");
				break;
			}
			BookEntity book = new BookEntity();
			book.setSite(Site.Single);
			book.setTip(text);
			saveBook(new BookAndChapters(book, null), title);
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * 添加到书架
	 */
	private void saveImgs(final BookAndChapters result, final String title) {
		if (result == null || result.getResult() != BookAndChapters.SearchResult.InputName) {
			Util.dialog(activity, "获取目录失败");
		} else {
			final EditText et = new EditText(activity);
			et.setBackgroundColor(Color.WHITE);
			if (!Util.isEmpty(title)) {
				et.setText(title);
				et.setSelection(title.length());
			}
			new MyAlertDialog(activity).setTitle("请输入要添加的书籍名字").setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = et.getText().toString().trim();
					if (!Util.isEmpty(name)) {
						result.getBook().setName(name);
						result.getBook().setSite(Site.Pic);
						saveBook(result.getBook(), result.getChapters());
					}
				}
			}).setNegativeButton("取消", null).show();
		}
	}

	/**
	 * 添加到书架
	 */
	public void saveBookClick(final int which) {
		if (which == 0) {
			Util.dialog(activity, "请确认目前显示的是小说目录页。\n(部分网页不能获取到小说目录，只能在线看小说)", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					activity.loadUrl("javascript:window.local_obj.saveBook(" + WHAT_SAVEBOOK1
							+ ",document.cookie,document.getElementsByTagName('html')[0].innerHTML);");
				}
			});
		} else if (which == 1) {
			Util.dialog(activity, "请确认目前显示的是小说目录页。\n(部分网页不能获取到小说目录，只能在线看小说。这里只会保存链接里面的图片，建议看漫画等等可以使用此功能)", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					activity.loadUrl("javascript:window.local_obj.saveBook(" + WHAT_SAVEBOOK4
							+ ",document.cookie,document.getElementsByTagName('html')[0].innerHTML);");
				}
			});
		} else if (which == 2) {
			Util.dialog(activity, "确定要保存此网页)", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					activity.loadUrl("javascript:window.local_obj.saveBook(" + WHAT_SAVEBOOK6 + ",1,document.getElementsByTagName('body')[0].innerText);");
				}
			});
		} else {
			Util.notCompleted(activity);
		}
	}

	/**
	 * 添加到书架
	 */
	private void saveBook(final BookAndChapters result, final String title) {
		if (result == null || result.getResult() == BookAndChapters.SearchResult.Failed) {
			Util.dialog(activity, "获取目录失败");
		} else if (result.getResult() == BookAndChapters.SearchResult.Search) {
			String msg = "不能获取到目录，是否去搜索小说《" + result.getBook().getName() + "》";
			Util.dialog(activity, msg, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(activity, SearchActivity.class);
					intent.putExtra("search", result.getBook().getName() + " " + result.getBook().getAuthor());
					activity.startActivity(intent);
				}
			});
		} else if (result.getResult() == BookAndChapters.SearchResult.InputName) {
			final EditText et = new EditText(activity);
			et.setBackgroundColor(Color.WHITE);
			if (!Util.isEmpty(title)) {
				et.setText(title);
				et.setSelection(title.length());
			}
			new MyAlertDialog(activity).setTitle("请输入要添加的书籍名字").setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String name = et.getText().toString().trim();
					if (!Util.isEmpty(name)) {
						result.getBook().setName(name);
						saveBook(result.getBook(), result.getChapters());
					}
				}
			}).setNegativeButton("取消", null).show();
		} else {
			saveBook(result.getBook(), result.getChapters());
		}
	}

	/**
	 * 添加到书架
	 */
	private void saveBook(final BookEntity book, final List<ChapterEntity> chapters) {
		activity.showLoadingDialog("正在保存小说");
		new Thread() {
			public void run() {
				String text = book.getTip();
				if (book.getSite() == Site.Single) {
					if (text.length() > 100) {
						book.setTip(text.substring(0, 100) + "...");
					}
					book.setDirectoryUrl(CONSTANT.EMPTY);// 不能为空
				}
				if (new BookDao().addBook(book)) {
					if (book.getSite() == Site.Single) {
						List<ChapterEntity> cs = LoadManager.getDirectory(book);
						LoadManager.saveDirectory(book.getId(), cs);
						LoadManager.saveChapterContent(book.getId(), book.getName(), text);
						book.setUnReadCount(cs.size());
					} else {
						LoadManager.saveDirectory(book.getId(), chapters);
						book.setUnReadCount(chapters.size());
					}
					activity.sendMsgOnThread(WHAT_SAVEBOOK3, book);
				} else {
					activity.sendErrorOnThread("保存小说失败");
				}
			};
		}.start();
	}
}
