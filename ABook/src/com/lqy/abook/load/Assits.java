package com.lqy.abook.load;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.entity.FavoriteEntity;
import com.lqy.abook.tool.Util;

public class Assits {
	public static void loadFavorite(final MenuActivity activity, final String path, final int what) {
		new Thread() {
			public void run() {
				String value = readAssits(activity, path);
				if (Util.isEmpty(value)) {
					activity.sendMsgOnThread(what, null);
					return;
				}
				String lines[] = value.trim().split("\n");
				List<FavoriteEntity> data = new ArrayList<FavoriteEntity>();
				FavoriteEntity en;
				for (String line : lines) {
					String values[] = line.trim().split("=");
					if (values.length > 1) {
						en = new FavoriteEntity();
						en.setTitle(values[0].trim());
						en.setUrl(values[1].trim());
						data.add(en);
					}
				}
				activity.sendMsgOnThread(what, data);
			}
		}.start();
	}

	/**
	 * 读取Assits里的文件
	 */
	private static String readAssits(Context context, String path) {
		InputStream is = null;
		try {
			is = context.getAssets().open(path);
		} catch (Exception e) {
		}
		if (is == null)
			return null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		return sb.toString();
	}
}
