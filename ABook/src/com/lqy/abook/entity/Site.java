package com.lqy.abook.entity;

import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.parser.site.Parser16K;
import com.lqy.abook.parser.site.Parser17K;
import com.lqy.abook.parser.site.ParserBaidu;
import com.lqy.abook.parser.site.ParserOther;
import com.lqy.abook.parser.site.ParserQidian;
import com.lqy.abook.parser.site.ParserSM;
import com.lqy.abook.parser.site.ParserShuyue;

public enum Site {
	_17K, SM, _16K, Other, Baidu, Qidian, Shuyue;

	public static Site getDefault() {
		return Other;
	}

	public static Site[] searchSite = new Site[] { Site.SM, Site.Shuyue, Site._17K, Site._16K, Site.Qidian };

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
			return new ParserBaidu();
		case Qidian:
			return new ParserQidian();
		case Shuyue:
			return new ParserShuyue();
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
		case Qidian:
			return "起点中文网";
		case Shuyue:
			return "书阅屋";
		default:
			return "未知网站";
		}
	}

}
