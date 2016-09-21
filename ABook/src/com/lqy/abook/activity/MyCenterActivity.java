package com.lqy.abook.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.MyApp;
import com.lqy.abook.R;
import com.lqy.abook.entity.Site;
import com.lqy.abook.img.SelectImageDialog;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.parser.ParserUtil;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.DataCleanManager;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.ArrayDialog;
import com.lqy.abook.widget.MySwitch;

public class MyCenterActivity extends MenuActivity {
	private SelectImageDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_center);

		final SharedPreferences sp = getSharedPreferences(CONSTANT.SP_CENTER, 0);
		boolean autoCheckUpdate = !sp.getBoolean("not_auto_check_udate", false);
		// 启动时默认检查更新
		MySwitch mySwitch = (MySwitch) findViewById(R.id.my_center_check_update);
		mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sp.edit().putBoolean("not_auto_check_udate", !isChecked).commit();
			}
		});
		mySwitch.setChecked(autoCheckUpdate);

	}

	public void sendButtonClick(View v) {
		switch (v.getId()) {
		case R.id.my_center_del:
			Util.dialog(_this, "确定要删除所有数据吗(建议版本升级后出现不能保存书籍等情况时使用)？", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					FileUtil.delFile(new File(FileUtil.getAppPath()));
					DataCleanManager.cleanApplicationData(_this);

					Util.toast(_this, "已经删除了所有的书籍和使用记录");

				}
			});
			break;
		case R.id.my_center_site:
			Site[] all = Site.allSearchSite;
			String[] titles = new String[all.length];
			boolean[] select = new boolean[all.length];
			for (int i = 0; i < all.length; i++) {
				Site s = all[i];
				titles[i] = s.getName();
				for (Site s2 : Site.searchSite) {
					if (s2 == s) {
						select[i] = true;
						break;
					}
				}
			}
			new ArrayDialog(_this).setTitle("设置搜索网站").setItems(titles, select, new ArrayDialog.Click() {

				@Override
				public void onClick(boolean[] o) {
					try {
						List<Site> searchSite = new ArrayList<Site>();
						Site[] all = Site.allSearchSite;
						for (int i = 0; i < all.length; i++) {
							if (o[i])
								searchSite.add(all[i]);
						}
						MyApp.getInstance().saveSearchSite(searchSite);
					} catch (Exception e) {
					}
				}
			}).show();
			break;
		case R.id.my_center_exit:
			if (MainActivity.getInstance() != null && !MainActivity.getInstance().isFinishing())
				MainActivity.getInstance().finish();
			finish();
			break;
		case R.id.my_center_loading:
			if (dialog == null)
				dialog = new SelectImageDialog(_this);
			dialog.show();
			break;
		case R.id.my_center_version:
			v.setEnabled(false);
			checkVersion(v);
			break;
		case R.id.my_center_feedback:
			feedback();
			break;
		case R.id.my_center_helper:
			startActivity(new Intent(_this, MyHelpActivity.class));
			animationRightToLeft();
			break;
		}
	}

	/**
	 * 版本检测
	 */
	private void checkVersion(final View v) {
		new Thread() {
			public void run() {
				Node node = ParserUtil.parseNodeByUrl(CONSTANT.VERSION_URL, ParserUtil.createEqualFilter("div class=\"summary noImg\""), "utf-8");
				final String text = node == null ? null : node.toPlainTextString();

				if (!isFinishing()) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								Pattern p = Pattern.compile("\\s*version:(\\d+)\\s+url:(\\S+)\\s*");
								Matcher m = p.matcher(text);
								if (m.find()) {
									int version = Integer.parseInt(m.group(1));
									final String url = m.group(2);

									PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
									if (version > info.versionCode) {
										Util.dialog(_this, "发现新的版本，是否更新", new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												Uri uri = Uri.parse(url);
												Intent intent = new Intent(Intent.ACTION_VIEW);
												intent.setData(uri);
												try {
													intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
													startActivity(intent);
												} catch (Exception e) {
													startActivity(intent);
												}
											}
										});
										v.setEnabled(true);
										return;
									}
								}
							} catch (Exception e) {
							}
							Util.toast(_this, "已是最新版本");
							v.setEnabled(true);
						}
					});
				}
			}
		}.start();
	}

	/**
	 * 意见和建议
	 */
	private void feedback() {
		try {
			String[] reciver = new String[] { "921419024@qq.com" };
			String[] myCc = new String[] {};
			StringBuilder sb = new StringBuilder();// 内容
			try {
				sb.append("Android Version：" + android.os.Build.VERSION.RELEASE + "\n  ");
				sb.append("Platform：" + Util.getMobileBrand() + "\n  ");
				sb.append("App Version：" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n  ");
			} catch (Exception e) {
			}

			Intent myIntent = new Intent(android.content.Intent.ACTION_SEND);
			myIntent.setType("plain/text");
			myIntent.putExtra(android.content.Intent.EXTRA_EMAIL, reciver); // 接收人
			myIntent.putExtra(android.content.Intent.EXTRA_CC, myCc); // 抄送
			myIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "意见和建议"); // 标题
			myIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
			startActivity(Intent.createChooser(myIntent, ""));
		} catch (Exception e) {
			Util.dialog(_this, "未找到邮件客户端或者邮件客户端未设置账户");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (dialog != null && dialog.onActivityResult(requestCode, resultCode, data))
			return;
		super.onActivityResult(requestCode, resultCode, data);
	}
}
