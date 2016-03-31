package com.lqy.abook.parser;

import java.util.ArrayList;
import java.util.List;

import com.lqy.abook.MenuActivity;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.site.Parser16K;
import com.lqy.abook.parser.site.Parser17K;
import com.lqy.abook.parser.site.ParserOther;
import com.lqy.abook.parser.site.ParserSM;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class ParserManager {
	// 解析器
	private static List<ParserBase> getParsers() {
		List<ParserBase> parsers = new ArrayList<ParserBase>();
		parsers.add(new ParserSM());
		parsers.add(new Parser17K());
		parsers.add(new Parser16K());
		return parsers;
	}

	public static int asynSearch(final MenuActivity activity, final String key, final int what) {
		List<ParserBase> parsers = getParsers();
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

	public static boolean getBookDetail(BookEntity detail) {
		return detail.getSite().getParser().parserBookDetail(detail);
	}

	public static List<ChapterEntity> updateBookAndDict(BookEntity book) {
		return book.getSite().getParser().updateBookAndDict(book);
	}

	public static void asynSearchSite(final MenuActivity activity, final String name, final String author, final int what) {
		new Thread() {
			public void run() {
				List<BookEntity> data = new ArrayList<BookEntity>();
				boolean fail = true;
				for (ParserBase parser : getParsers()) {
					fail &= !parser.parserSearchSite(data, name, author);
					if (!fail)
						activity.sendMsgOnThread(what, data);
				}
				if (fail)
					activity.sendErrorOnThread("未找到下载点");
			};
		}.start();
	}

	public static List<ChapterEntity> getDict(BookEntity book) {
		MyLog.i("getDict  " + book.getId() + "  " + book.getDirectoryUrl());
		return book.getSite().getParser().parserBookDict(book.getDirectoryUrl());
	}

	public static String getChapterDetail(String url, Site site) {
		return site.getParser().getChapterDetail(url);
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public static void parserBrowser(final MenuActivity activity, final String url, final String html, final int what) {
		if (Util.isEmpty(url) || Util.isEmpty(html))
			activity.sendMsgOnThread(what);
		new Thread() {
			public void run() {
				ParserResult result = null;
				List<ParserBase> parsers = getParsers();
				for (ParserBase parser : parsers) {
					result = parser.parserBrowser(url, html);
					if (result != null) {
						break;
					}
				}
				if (result == null) {
					result = new ParserOther().parserBrowser(url, html);
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
