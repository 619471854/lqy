package com.lqy.abook.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.activity.LoadingActivity;
import com.lqy.abook.activity.MainActivity;
import com.lqy.abook.db.BookDao;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.load.LoadManager;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.EncodingDetect;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.MyAlertDialog;

public class SaveLocatedBook {

	private MenuActivity activity;

	private int requestCode_file = 10001;
	private int msg_error = 10003;
	private int msg_ok = 10004;
	private int msg_progress = 10005;
	private ProgressDialog progressDialog;

	/**
	 * 照片选择对话框
	 */
	public SaveLocatedBook(MenuActivity context) {
		this.activity = context;
	}

	/**
	 * 显示照片选择对话框
	 */
	public void show() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("text/plain");
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		try {
			activity.startActivityForResult(Intent.createChooser(intent, "选择一个文本文件"), requestCode_file);
		} catch (android.content.ActivityNotFoundException ex) {
			Util.toast(activity, "请安装文件管理器");
		}
	}

	/**
	 * 返回 界面 回调
	 */
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == requestCode_file) {
				dealFile(FileUtil.getPath(activity, data.getData()));
				return true;
			}
		}
		return false;
	}

	/**
	 * 消息 回调
	 */
	public boolean dealMsg(int what, int arg1, Object o) {
		if (what == msg_error || what == msg_ok || what == msg_progress) {
			if (what == msg_error) {
				Util.dialog(activity, o.toString());
			} else if (what == msg_ok) {
				if (progressDialog != null)
					progressDialog.cancel();

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
			} else {
				if (progressDialog != null)
					progressDialog.setProgress(arg1);
			}
			return true;
		}
		return false;
	}

	private void dealFile(String path) {
		if (path != null) {
			final File file = new File(path);
			if (!file.isDirectory() && file.exists() && file.length() > 0) {
				try {
					View view = LayoutInflater.from(activity).inflate(R.layout.save_located_book, null);
					final EditText et1 = (EditText) view.findViewById(R.id.save_located_book_start1);
					final EditText et2 = (EditText) view.findViewById(R.id.save_located_book_start2);
					final EditText et3 = (EditText) view.findViewById(R.id.save_located_book_length);
					et1.setSelection(et1.length());
					final MyAlertDialog dialog = new MyAlertDialog(activity).setTitle("请设置章节划分规则").setView(view);
					dialog.setClickBtnCancel(false);
					dialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface d, int which) {
							final String t1 = et1.getText().toString().trim();
							final String t2 = et2.getText().toString().trim();
							final int length = Util.toInt(et3.getText().toString().trim());
							if (t1.length() == 0 || t2.length() == 0) {
								Util.toast(activity, "章节头不能为空");
								return;
							}
							if (length < 1000 || length > 9999) {
								Util.toast(activity, "章节字数不能少于1000，不能大于9999");
								return;
							}
							new Thread() {
								public void run() {
									// 匹配“第几章 章节名字”
									String reg = "^\\s*(%s[0-9\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341\u767e\u5343\u4e07\u58f9\u8d30\u53c1\u8086\u4f0d\u9646\u67d2\u634c\u7396\u62fe\u4f70\u4edf]{1,7}+%s\\s*(\\S{1,10}+)?)";
									reg = String.format(reg, t1, t2);
									saveBook(file, reg, length);
								}
							}.start();

							dialog.cancel();

							progressDialog = new ProgressDialog(activity);
							progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
							progressDialog.setCancelable(false);
							progressDialog.setProgress(0);
							progressDialog.setTitle("下载中...");
							progressDialog.show();
						}
					});
					dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();
				} catch (Exception e) {
					MyLog.e(e);
				}
				return;
			}
		}
		Util.toast(activity, "获取文件失败");
	}

	private void saveBook(File file, String startReg, int length) {
		BookEntity book = new BookEntity();
		book.setDirectoryUrl(CONSTANT.EMPTY);
		book.setSite(SiteEnum.Located);
		String name = file.getName();
		int index = name.lastIndexOf(".");
		if (index != -1) {
			name = name.substring(0, index);
		}
		book.setName(name);
		if (!new BookDao().addBook(book)) {
			activity.sendMsgOnThread(msg_error, "保存小说失败");
			return;
		}
		List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
		ChapterEntity c = null;
		String encode = EncodingDetect.getJavaEncode(file);
		FileInputStream fis = null;// 输入流
		BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis, encode));
			StringBuffer text = new StringBuffer();// 章节内容
			String line = null;
			Pattern p = Pattern.compile(startReg);
			Matcher m = null;
			int size = 0;
			int progress = 0;
			int lastProgress = 0;
			int total = (int) (file.length() / 200);// 1个字等于2字节，所以不用100
			total = Math.max(total, 1);
			String firstChapter = null;
			while ((line = br.readLine()) != null) {
				size += line.length();
				progress = size / total;
				if (lastProgress != progress) {
					lastProgress = progress;
					activity.sendMsgOnThread(msg_progress, progress, null);
				}
				line = line.trim();
				if (line.length() == 0)
					continue;
				m = p.matcher(line);
				if (m.find()) {// 找到了“第二章 ＊＊＊ ”，创建新章节
					if (c == null) {
						c = new ChapterEntity();
						text.delete(0, text.length());
					} else if (text.length() != 0) {
						// 保存上一章
						chapters.add(c);
						LoadManager.saveChapterContent(book.getId(), c.getName(), text.toString());
						if (firstChapter == null) {
							firstChapter = text.substring(0, Math.min(100, text.length()));
						}
						text.delete(0, text.length());
						// 开始下一章
						c = new ChapterEntity();
					}
					c.setId(chapters.size());
					c.setName(m.group(1));
					line = line.substring(m.end()).trim();
					if (line.length() > 0) {
						if (text.length() > 0)// 每章第一段 不缩进
							text.append("        ");
						text.append(line);
						text.append("\n");
					}
				} else if (c == null) {// 新的章节
					if (line.length() > 0) {
						text.delete(0, text.length());
						c = new ChapterEntity();
						c.setId(chapters.size());
						c.setName("章节" + (c.getId() + 1));
						if (text.length() > 0)
							text.append("        ");
						text.append(line);
						text.append("\n");
					}
				} else {
					if (line.length() > 0) {
						if (text.length() > 0)
							text.append("        ");
						text.append(line);
						text.append("\n");
					}
					if (text.length() > length) {// 章节字数达标，此章结束
						// 保存上一章
						chapters.add(c);
						if (firstChapter == null) {
							firstChapter = text.substring(0, Math.min(100, text.length()));
						}
						LoadManager.saveChapterContent(book.getId(), c.getName(), text.toString());

						c = null;
						text.delete(0, text.length());
					}
				}
			}
			if (chapters.size() == 0) {
				new BookDao().deleteBook(book.getId());
				activity.sendMsgOnThread(msg_error, "读取小说失败");
			} else {
				LoadManager.saveDirectory(book.getId(), chapters);
				if (firstChapter != null) {
					book.setTip(firstChapter);
					new BookDao().updateBook(book);
				}
				book.setUnReadCount(chapters.size());
				activity.sendMsgOnThread(msg_ok, book);
			}
		} catch (Exception e) {
			activity.sendMsgOnThread(msg_error, "读取小说失败");
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
				fis = null;
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
				br = null;
			}
		}
	}
	// private void test(){
	// String a = String.format(
	// "\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x\\u%x",
	// Integer.valueOf('零'), Integer.valueOf('一'), Integer.valueOf('二'),
	// Integer.valueOf('三'), Integer.valueOf('四'), Integer.valueOf('五'),
	// Integer.valueOf('六'), Integer.valueOf('七'), Integer.valueOf('八'),
	// Integer.valueOf('九'), Integer.valueOf('十'), Integer.valueOf('百'),
	// Integer.valueOf('千'), Integer.valueOf('万'), Integer.valueOf('壹'),
	// Integer.valueOf('贰'), Integer.valueOf('叁'), Integer.valueOf('肆'),
	// Integer.valueOf('伍'), Integer.valueOf('陆'), Integer.valueOf('柒'),
	// Integer.valueOf('捌'), Integer.valueOf('玖'), Integer.valueOf('拾'),
	// Integer.valueOf('佰'), Integer.valueOf('仟'));
	// System.out.println(a);
	// for (int i = 0; i < a.length(); i = i + 6) {
	// System.out.println(a.substring(i, i + 6) + "=" + String.format("%c",
	// Integer.valueOf(a.substring(i + 2, i + 6), 16)));
	// }
	// }
}
