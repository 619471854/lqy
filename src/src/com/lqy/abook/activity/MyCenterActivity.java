package com.lqy.abook.activity;

import java.io.File;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.db.HistoryDao;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.MySwitch;

public class MyCenterActivity extends MenuActivity {
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
		case R.id.browser_set_empty_histoy:
			Util.dialog(_this, "确定要清除历史纪录吗", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					new HistoryDao().emptyHistory();
					Util.dialog(_this, "历史纪录已清空");
				}
			});
			break;
		case R.id.browser_set_clear_cache:
			Util.dialog(_this, "确定要清除浏览器缓存吗", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					clearWebViewCache();
					Util.dialog(_this, "缓存已清空");
				}
			});
			break;
		}
	}

	/**
	 * 清除WebView缓存
	 */
	public void clearWebViewCache() {
		// 清理Webview缓存数据库
		try {
			deleteDatabase("webview.db");
			deleteDatabase("webviewCache.db");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// WebView 缓存文件 ,离线缓存
		File appCacheDir = new File(FileUtil.getCachePath());
		File webviewCacheDir = new File(getCacheDir().getAbsolutePath() + "/webviewCache");
		// 删除webview 缓存目录
		if (webviewCacheDir.exists()) {
			FileUtil.delFile(webviewCacheDir);
		}
		// 删除webview 缓存 缓存目录
		if (appCacheDir.exists()) {
			FileUtil.delFile(appCacheDir);
		}
	}
}
