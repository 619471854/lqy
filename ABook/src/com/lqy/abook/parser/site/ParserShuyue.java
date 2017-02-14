package com.lqy.abook.parser.site;

import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase3;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class ParserShuyue extends ParserBase3 {
	private static Config config = Config.getShuyueConfig();

	public ParserShuyue() {
		encodeType = "gbk";
		site = SiteEnum.Shuyue;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		return false;
	}

	@Override
	public String getChapterDetail(String url) {
		return getChapterDetail(url, "您提供精彩小说阅读。\\s*(((?!手机用户请浏览m.shuyuewu.com阅读)[\\s\\S])+)");
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		html = null;// 手机端网页，需要重新加载电脑版网页
		String id = matcher(url, "http://m\\.09xs\\.com/wapbook-(\\d+)[^\\d]?(\\d*)/?");
		if (Util.isEmpty(id))
			id = matcher(url, "http://m\\.09xs\\.com/info-(\\d+)/?");
		if (Util.isEmpty(id)) {
			id = matcher(url, "http://www\\.09xs\\.com/kan_(\\d+)/");
			if (Util.isEmpty(id))
				return null;
		} else {
			html = null;// 手机端网页，需要重新加载电脑版网页
			url = "http://www.09xs.com/kan_" + id;
		}
		if (!url.endsWith("/"))
			url += "/";

		try {
			Node node = parseNode(url, html, createEqualFilter("div id=\"wrapper\""), encodeType);
			if (node != null) {
				MyLog.i(TAG, "parserBookDict getParserResult ok");
				NodeList children = node.getChildren();
				Node dict = children.elementAt(15);
				Node detail = children.elementAt(9);
				dict = dict.getChildren().elementAt(1);
				children = null;
				// 获取内容
				BookEntity book = parserBookDetail(url, detail.toHtml());
				MyLog.i(TAG, "getBookAndDict book  " + book == null ? null : book.getName());
				// 获取章节
				List<ChapterEntity> chapters = parserBookDictByHtml(url, dict.toHtml());
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
		return parserBookDetail(url, html, "http://www.shuyuewu.com");
	}
}
