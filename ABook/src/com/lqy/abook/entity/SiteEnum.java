package com.lqy.abook.entity;

import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.parser.site.*;
import com.lqy.abook.tool.CONSTANT;

public enum SiteEnum {
	_17K, SM, _16K, Other, Baidu, Qidian, Shuyue, Pic, _00kw, DSB, Biquge, Located, Single;

	public static SiteEnum getDefault() {
		return Other;
	}

	public static SiteEnum[] allSearchSite = new SiteEnum[] { SiteEnum.Biquge, SiteEnum.SM, SiteEnum.Shuyue, SiteEnum._00kw, SiteEnum.DSB, SiteEnum.Qidian };
	public static SiteEnum[] searchSite = allSearchSite;

	// public static Site[] searchSite = new Site[] { Site.DSB };

	/**
	 * 是否没有网址
	 */
	public boolean notDictUrl() {
		return this == SiteEnum.Single || this == SiteEnum.Located;
	}

	/**
	 * 是否支持更新
	 */
	public boolean supportUpdated() {
		return this != SiteEnum.Pic && !notDictUrl();
	}

	public static SiteEnum valueOf(int index) {
		try {
			return SiteEnum.values()[index];
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
		case _00kw:
			return new Parser00ks();
		case DSB:
			return new ParserDSB();
		case Biquge:
			return new ParserBiquge();
		case Pic:
			return new ParserPic();
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
		case _00kw:
			return "零点看书";
		case DSB:
			return "大书包小说网";
		case Biquge:
			return "笔趣阁";
		case Pic:
			return CONSTANT.EMPTY;
		case Located:
			return "本地导入";
		default:
			return CONSTANT.EMPTY;
		}
	}

}
