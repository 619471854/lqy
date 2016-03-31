package com.lqy.abook.entity;

import android.content.Context;
import android.content.SharedPreferences;

import com.lqy.abook.R;
import com.lqy.abook.tool.CONSTANT;

public class FontMode extends SerializableEntity {

	public enum Type {
		white, brown, green, custom
	}

	public int textColor;
	public int bgColor;
	public int bgResId;
	public Type type = Type.brown;

	private static final int CUSTOM_COLOR_TEXT = 0xff4d4530;
	private static final int CUSTOM_COLOR_BG = 0xffddd5c0;

	public static FontMode getDefault() {
		FontMode mode = new FontMode();
		mode.textColor = 0xff4d4530;
		mode.bgResId = R.drawable.read_bg_brown;
		mode.type = Type.brown;
		return mode;
	}

	public static FontMode getCustomerDefault() {
		FontMode mode = new FontMode();
		mode.type = Type.custom;
		mode.textColor = CUSTOM_COLOR_TEXT;
		mode.bgColor = CUSTOM_COLOR_BG;
		return mode;
	}

	public static FontMode getCustomer(SharedPreferences sp) {
		FontMode mode = new FontMode();
		mode.type = Type.custom;
		mode.textColor = sp.getInt("textColor", CUSTOM_COLOR_TEXT);
		mode.bgColor = sp.getInt("bgColor", CUSTOM_COLOR_BG);
		return mode;
	}

	public static FontMode valueOf(Type type, int customTextColor, int customBgColor) {
		FontMode mode = new FontMode();
		mode.type = type;
		switch (mode.type) {
		case white:
			mode.textColor = 0xff5d5f5c;
			mode.bgColor = 0xfff6f2ea;
			mode.bgResId = R.drawable.read_bg_white;
			break;
		case brown:
			mode.textColor = 0xff4d4530;
			mode.bgColor = 0xffddd5c0;
			mode.bgResId = R.drawable.read_bg_brown;
			break;
		case green:
			mode.textColor = 0xff5a6350;
			mode.bgColor = 0xffdfe8d5;
			mode.bgResId = R.drawable.read_bg_green;
			break;
		case custom:
			mode.textColor = customTextColor;
			mode.bgColor = customBgColor;
			break;
		}
		return mode;
	}

	public static FontMode valueOf(int _type, Context context) {
		if (_type == Type.custom.ordinal()) {
			SharedPreferences sp = context.getSharedPreferences(CONSTANT.SP_READ, 0);
			FontMode mode = new FontMode();
			mode.type = Type.custom;
			mode.textColor = sp.getInt("textColor", CUSTOM_COLOR_TEXT);
			mode.bgColor = sp.getInt("bgColor", CUSTOM_COLOR_BG);
			return mode;
		} else {
			try {
				Type type = Type.values()[_type];
				return valueOf(type, 0xff5d5f5c, 0xfff6f2ea);
			} catch (Exception e) {
				return FontMode.getDefault();
			}
		}
	}

	public static FontMode valueOf(SharedPreferences sp) {
		try {
			Type type = Type.values()[sp.getInt("fontMode", Type.brown.ordinal())];
			int customTextColor = sp.getInt("textColor", CUSTOM_COLOR_TEXT);
			int customBgColor = sp.getInt("bgColor", CUSTOM_COLOR_BG);
			return valueOf(type, customTextColor, customBgColor);
		} catch (Exception e) {
			return FontMode.getDefault();
		}
	}

	public void save(SharedPreferences sp) {
		sp.edit().putInt("fontMode", type.ordinal()).putInt("textColor", textColor).putInt("bgColor", bgColor).commit();
	}

}
