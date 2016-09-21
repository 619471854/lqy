package com.lqy.abook.parser;

import java.util.regex.Matcher;

import org.htmlparser.Node;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;

import com.lqy.abook.tool.Util;

public class UpdateUtils extends ParserUtil {
	private static final String URL = "http://appfront.handday.cn/ShareWork/Article?id=0CH8BAZgZ2";

	public void checkVersion(Activity a) {
		Node node = parseNodeByUrl(URL, createEqualFilter("div class=\"summary noImg\""), "utf-8");
		String text = node == null ? null : node.toPlainTextString();
		Matcher m = Util.isEmpty(text) ? null : getMatcher(text, "\\s*version:(\\d+)\\s+url:(\\S+)\\s*");
		if (m == null)
			return;
		int version = Integer.parseInt(m.group(1));

		try {
			PackageInfo info = a.getPackageManager().getPackageInfo(a.getPackageName(), 0);
			if (version > info.versionCode) {
				Uri uri = Uri.parse(m.group(2));
				// 创建Intent意图
				Intent intent = new Intent(Intent.ACTION_VIEW);
				// 设置Uri和类型
				intent.setDataAndType(uri, "application/vnd.android.package-archive");
				// 执行意图进行安装
				a.startActivity(intent);
			}
		} catch (Exception e) {
		}
	}
}
