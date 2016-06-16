package com.lqy.abook.activity;

import java.io.File;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.DataCleanManager;
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
		case R.id.my_center_del:
			Util.dialog(_this, "确定要删除所有数据吗(建议版本升级后出现不能保存书籍等情况时使用)？", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					FileUtil.delFile(new File(FileUtil.APP_PATH));
					DataCleanManager.cleanApplicationData(_this);

					Util.toast(_this, "已经删除了所有的书籍和使用记录");

				}
			});
			break;
		case R.id.my_center_site:
			Util.notCompleted(_this);
			break;
		case R.id.my_center_exit:
			if (MainActivity.getInstance() != null && !MainActivity.getInstance().isFinishing())
				MainActivity.getInstance().finish();
			finish();
			break;
		}
	}
}
