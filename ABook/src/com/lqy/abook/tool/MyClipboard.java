package com.lqy.abook.tool;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MyClipboard {

	/**
	 * 复制文本到剪贴板
	 */
	public static void copy(Context con, String msg) {
		if (msg == null)
			return;
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion < 11) {
			android.text.ClipboardManager cmb = (android.text.ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setText(msg);
			if (cmb.hasText()) {
				cmb.getText();
			}

		} else {
			android.content.ClipboardManager cmb = (android.content.ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setPrimaryClip(ClipData.newPlainText(null, msg));
			if (cmb.hasPrimaryClip()) {
				cmb.getPrimaryClip().getItemAt(0).getText();
			}
		}
		if (msg.length() > 0) {
			Toast.makeText(con, msg + "  复制成功", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(con, "剪贴板已清空", Toast.LENGTH_LONG).show();
		}
	}

	public static String get(Context con) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion < 11) {
			android.text.ClipboardManager cmb = (android.text.ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
			if (cmb.hasText()) {
				return Util.toString(cmb.getText()).trim();
			}
		} else {
			android.content.ClipboardManager cmb = (android.content.ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
			if (cmb.hasPrimaryClip()) {
				return Util.toString(cmb.getPrimaryClip().getItemAt(0).getText()).trim();
			}
		}
		return null;
	}
}
