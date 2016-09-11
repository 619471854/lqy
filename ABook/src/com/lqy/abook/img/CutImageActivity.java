package com.lqy.abook.img;

import java.io.File;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.R;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.ImageCompress;
import com.lqy.abook.tool.Util;
import com.lqy.abook.widget.CutView;

public class CutImageActivity extends MenuActivity {

	private CutView cutView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_cut);

		hideProgressBar();

		String path = null;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			path = bundle.getString("path");
		}

		if (path == null) {
			Util.toast(_this, "未找到图片");
			finish();
		}

		int viewWidth = GlobalConfig.getScreenWidth();
		int viewHeight = (int) (GlobalConfig.getScreenHeight());
		Bitmap image = new ImageCompress().getSmallBitmap(path, viewWidth, viewHeight);

		if (image == null) {
			Util.toast(_this, "未找到图片");
			finish();
		}

		cutView = (CutView) findViewById(R.id.image_cutview);
		cutView.setParams(viewWidth, viewHeight, viewWidth, viewHeight);
		cutView.setImageBitmap(image);
	}

	public void sendButtonClick(View v) {
		try {
			Bitmap bm = cutView.getResult();
			if (bm != null) {
				// 重命名以前的图片
				File file = new File(FileUtil.getAppPath(), FileUtil.LOADING_NAME);
				if (file.exists())
					file.renameTo(new File(FileUtil.getAppPath(), System.currentTimeMillis() + "_" + FileUtil.LOADING_NAME));
				
				new ImageCompress().saveBmpToFile(bm, FileUtil.getAppPath(), FileUtil.LOADING_NAME);
				Util.toast(_this, "设置成功");
			} else {
				Util.toast(_this, "没有图片");
			}
		} catch (Exception e) {
			Util.toast(_this, "裁切失败:" + e.getMessage());
		}
		finish();
		animationLeftToRight();
	}

	public void rotateButtonClick(View v) {
		cutView.rotate();
	}
}
