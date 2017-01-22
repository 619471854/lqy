package com.lqy.abook.parser.site;

import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.tags.ImageTag;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase3;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class ParserBiquge extends ParserBase3 {
	private static Config config = Config.getBiqugeConfig();

	public ParserBiquge() {
		encodeType = "utf-8";
		site = Site.Biquge;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		try {
			String html = toHtml(parseNodeByUrl(url, createEqualFilter("div id=\"list\""), encodeType));
			if (!Util.isEmpty(html)) {
				MyLog.i(TAG, "parserBookDict getParserResult ok");
				return parserBookDictByHtml("http://www.biquge.com", html);
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	@Override
	public String getChapterDetail(String url) {
		String text = toText(parseNode(url, null, createEqualFilter("div id=\"content\""), encodeType));
		if (!Util.isEmpty(text)) {
			MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
			String text2 = matcher(text, "创建于[\\s\\S]+?&nbsp;&nbsp;&nbsp;&nbsp;([\\s\\S]+)");
			if (!Util.isEmpty(text2))
				text = text2;
			text = text.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\n        ");
			return text.trim();
		}
		return null;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String id = matcher(url, "http://m\\.00ksw\\.net/html/(\\d+/\\d+)");
		if (Util.isEmpty(id))
			id = matcher(url, "http://m\\.00ksw\\.net/ml/(\\d+/\\d+)/?");

		if (Util.isEmpty(id)) {
			id = matcher(url, "http://www\\.biquge\\.com/(\\d+_\\d+)/?");
			if (Util.isEmpty(id))
				return null;
			html = null;// 重新加载电脑版网页,直接用html有问题
		} else {
			html = null;// 手机端网页，需要重新加载电脑版网页
			url = "http://www.biquge.com/" + id + "/";
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
				List<ChapterEntity> chapters = parserBookDictByHtml("http://www.biquge.com/", html);
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

	protected BookEntity parserBookDetail(String url, String html) {
		try {
			String infoHtml = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"info\"")));
			BookEntity book = new BookEntity();
			book.setSite(site);
			book.setName(matcher(infoHtml, "<h1>([^<]*)</h1>"));
			book.setAuthor(matcher(infoHtml, "<p>作&nbsp;&nbsp;&nbsp;&nbsp;者：([^<]*)</p>"));
			book.setUpdateTime(matcher(infoHtml, "<p>最后更新：(\\d{4}/\\d{1,2}/\\d{1,2})[^<]*</p>"));
			MyLog.i(book.getUpdateTime());

			String tipHtml = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"intro\"")));
			book.setTip(tipHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s+", CONSTANT.EMPTY)
					.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\n        "));

			Node node = parseNodeByHtml(html, createEqualFilter("div id=\"fmimg\""));
			ImageTag cover = (ImageTag) node.getChildren().elementAt(0);
			book.setCover("http://www.biquge.com" + cover.getImageURL());

			return book;

		} catch (Exception e) {
			return null;
		}
	}
}
