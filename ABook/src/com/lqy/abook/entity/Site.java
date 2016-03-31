package com.lqy.abook.entity;

import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.parser.site.Parser16K;
import com.lqy.abook.parser.site.Parser17K;
import com.lqy.abook.parser.site.ParserOther;
import com.lqy.abook.parser.site.ParserSM;

public enum Site {
	_17K, SM, _16K, Other, Baidu;

	public static Site getDefault() {
		return Other;
	}

	public static Site valueOf(int index) {
		try {
			return Site.values()[index];
		} catch (Exception e) {
			return getDefault();
		}
	}

	public static String getName(int index) {
		return valueOf(index).getName();
	}

	public static ParserBase getParser(int index) {
		return valueOf(index).getParser();
	}

	public ParserBase getParser() {
		switch (this) {
		case _17K:
			return new Parser17K();
		case SM:
			return new ParserSM();
		case _16K:
			return new Parser16K();
		case Baidu:
			return new ParserOther();
		default:
			return new ParserOther();
		}
	}

	public String getName() {
		switch (this) {
		case _17K:
			return "17K小说网";
		case SM:
			return "神马小说网";
		case _16K:
			return "16K小说网";
		case Baidu:
			return "百度书城";
		default:
			return "未知网站";
		}
	}

}
