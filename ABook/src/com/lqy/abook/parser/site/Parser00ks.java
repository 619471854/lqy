package com.lqy.abook.parser.site;

import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase3;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class Parser00ks extends ParserBase3 {
	private static Config config = Config.get00ksConfig();

	public Parser00ks() {
		encodeType = "utf-8";
		site = Site._00kw;
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
	public BookAndChapters parserBrowser(String url, String html) {
		String id = matcher(url, "http://m\\.00ksw\\.com/html/(\\d+/\\d+)/?");
		if (Util.isEmpty(id))
			id = matcher(url, "http://m\\.00ksw\\.com/ml/(\\d+/\\d+)/?");

		if (Util.isEmpty(id)) {
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
			SimpleNodeIterator iterator = parseIterator(url, html, createEqualFilter("div id=\"wrapper\""), "gbk");
			
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			if (iterator.hasMoreNodes()) {
				NodeList children = iterator.nextNode().getChildren();
				Node dict = children.elementAt(13);
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
		return parserBookDetail(url, html, "http://www.00ksw.com");
	}
}
