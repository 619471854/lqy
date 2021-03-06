package com.lqy.abook.parser;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.site.ParserBaidu;
import com.lqy.abook.parser.site.ParserOther;
import com.lqy.abook.parser.site.ParserQidian;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class ParserManager {
	/**
	 * 解析器
	 */
	private static List<ParserBase> getParsers(Site exclude) {
		List<ParserBase> parsers = new ArrayList<ParserBase>();
		for (Site s : Site.values()) {
			if (s != Site.Other && s != exclude)
				parsers.add(s.getParser());
		}
		// parsers.add(new ParserQidian());
		return parsers;
	}

	/**
	 * 搜索小说
	 */
	public static int asynSearch(final MenuActivity activity, String _key, final int what) {
		final String key = _key.replaceAll("'", "").replaceAll("\\s", " ");// 去除单引号和多余的空格
		List<ParserBase> parsers = getParsers(null);
		for (final ParserBase parser : parsers) {
			new Thread() {
				public void run() {
					List<BookEntity> data = new ArrayList<BookEntity>();
					parser.parserSearch(data, key);
					activity.sendMsgOnThread(what, data);
				};
			}.start();
		}
		return parsers.size();

	}

	/**
	 * 搜索拥有此小说的网站
	 */
	public static int asynSearchSite(final MenuActivity activity, final BookEntity book, final int what) {
		List<ParserBase> parsers = getParsers(book.getSite());
		for (final ParserBase parser : parsers) {
			new Thread() {
				public void run() {
					BookEntity e = parser.parserSearchSite(book.getName(), book.getAuthor());
					activity.sendMsgOnThread(what, e);
				};
			}.start();
		}
		return parsers.size();
	}

	/**
	 * 更新小说，如果null则未更新或更新失败
	 */
	public static List<ChapterEntity> updateBookAndDict(BookEntity book) {
		return book.getSite().getParser().updateBookAndDict(book);
	}

	/**
	 * 根据小说主页地址获取 小说信息
	 */
	public static boolean getBookDetail(BookEntity detail) {
		return detail.getSite().getParser().parserBookDetail(detail);
	}

	/**
	 * 根据小说目录地址获取 目录
	 */
	public static List<ChapterEntity> getDict(BookEntity book) {
		MyLog.i("getDict  " + book.getId() + "  " + book.getDirectoryUrl());
		return book.getSite().getParser().parserBookDict(book.getDirectoryUrl());
	}

	/**
	 * 获取章节详情
	 */
	public static String getChapterDetail(String url, Site site) {
		return site.getParser().getChapterDetail(url);
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public static void parserBrowser(final MenuActivity activity, String url, final String html, final String cookie, final int what) {
		if (Util.isEmpty(url) || Util.isEmpty(html))
			activity.sendMsgOnThread(what);

		final String url2 = URLDecoder.decode(url);
		new Thread() {
			public void run() {
				BookAndChapters result = null;
				List<ParserBase> parsers = getParsers(null);
				for (ParserBase parser : parsers) {
					result = parser.parserBrowser(url2, html);
					if (result != null) {
						break;
					}
				}
				if (result == null) {
					result = new ParserOther().parserBrowser(url2, html, cookie);
				}
				if (result == null) {
					activity.sendMsgOnThread(what);
				} else {
					activity.sendMsgOnThread(what, result);
				}
			};
		}.start();
	}
}
