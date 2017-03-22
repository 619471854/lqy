package com.lqy.abook.parser.site;

import java.util.List;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase3;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class Parser00ks extends ParserBase3 {
	private static Config config = Config.get00ksConfig();

	public Parser00ks() {
		encodeType = "gbk";
		site = SiteEnum._00ks;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public String getChapterDetail(String url) {
		return getChapterDetail(url, "<div id=\"content\">\\s*(((?!</div>)[\\s\\S])+)\\s*</div>");
	}

	protected boolean setDetailUrl(BookEntity book) {
		String url = book.getDirectoryUrl();
		if (url != null && url.contains("m.00ksw.com"))
			return true;
		return super.setDetailUrl(book);
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String id = matcher(url, "http://m\\.00ksw\\.net/html/(\\d+/\\d+)");
		if (Util.isEmpty(id))
			id = matcher(url, "http://m\\.00ksw\\.net/ml/(\\d+/\\d+)/?");

		if (Util.isEmpty(id)) {
			id = matcher(url, "http://www\\.00ksw\\.net/html/(\\d+/\\d+)/?");
			if (Util.isEmpty(id)) 
				id = matcher(url, "http://www\\.00ksw\\.com/html/(\\d+/\\d+)/?");
			if (Util.isEmpty(id))
				return null;
			html = null;// 重新加载电脑版网页,直接用html有问题
		} else {
			html = null;// 手机端网页，需要重新加载电脑版网页
			url = "http://www.00ksw.com/html/" + id + "/";
		}
		if (!url.endsWith("/"))
			url += "/";

		try {
			html = toHtml(parseNode(url, html, createEqualFilter("div id=\"wrapper\""), encodeType));

			MyLog.i(TAG, "parserBookDict getParserResult ok");
			if (!Util.isEmpty(html)) {
				// 获取内容
				BookEntity book = parserBookDetail(url, html);
				MyLog.i(TAG, "getBookAndDict book  " + book == null ? null : book.getName());
				// 获取章节
				html = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"list\"")));
				List<ChapterEntity> chapters = parserBookDictByHtml(url, html);
				if (chapters == null || chapters.size() == 0) {
					MyLog.i(TAG, "getBookAndDict getChapters failed");
					return null;
				}
				if (book != null) {
					book.setNewChapter(chapters.get(chapters.size() - 1).getName());
					book.setDetailUrl(url);
					book.setDirectoryUrl(url);
					book.setSite(site);
				}
				return new BookAndChapters(book, chapters);
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	public BookEntity parserBookDetail(String url, String html) {
		return parserBookDetail(url, html, "http://www.00ksw.net");
	}
}
