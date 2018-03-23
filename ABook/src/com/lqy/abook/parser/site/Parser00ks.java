package com.lqy.abook.parser.site;

import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.tags.ImageTag;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase3;
import com.lqy.abook.tool.CONSTANT;
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
	public List<ChapterEntity> parserBookDict(String url) {
		String html = toHtml(parseNodeByUrl(url, createEqualFilter("div class=\"ml_list\""), encodeType));
		if (!Util.isEmpty(html)) {
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			return parserBookDictByHtml(url, html);
		}
		return null;
	}

	@Override
	public String getChapterDetail(String url) {
		String text = toText(parseNode(url, null, createEqualFilter("div class=\"novelcontent\""), encodeType));
		if (!Util.isEmpty(text)) {
			MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
			text = text.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\n        ");
			return text.trim();
		}
		return null;
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
		String id = matcher(url, "https://m\\.1200ksw\\.com/html/(\\d+/\\d+)/?");

		if (Util.isEmpty(id)) {
			id = matcher(url, "https://www\\.1200ksw\\.com/html/(\\d+/\\d+)/?");
			if (Util.isEmpty(id))
				return null;
			html = null;// 重新加载电脑版网页,直接用html有问题
		} else {
			html = null;// 手机端网页，需要重新加载电脑版网页
			url = "https://www.1200ksw.com/html/" + id + "/";
		}

		try {
			html = toHtml(parseNode(url, html, createEqualFilter("div class=\"biaoqian\""), encodeType));

			MyLog.i(TAG, "parserBookDict getParserResult ok");
			if (!Util.isEmpty(html)) {
				// 获取内容
				BookEntity book = parserBookDetail(html);
				MyLog.i(TAG, "getBookAndDict book  " + book == null ? null : book.getName());
				// 获取章节
				html = toHtml(parseNodeByHtml(html, createEqualFilter("div class=\"ml_list\"")));
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

	protected BookEntity parserBookDetail(String html) {
		try {
			String infoHtml = toHtml(parseNodeByHtml(html, createEqualFilter("div class=\"catalog\"")));
			BookEntity book = new BookEntity();
			book.setSite(site);
			book.setName(matcher(infoHtml, "<h1>([^<]*)</h1>"));
			book.setAuthor(matcher(infoHtml, "<span>作者：([^<]*)</span>"));
			book.setUpdateTime(matcher(infoHtml, "<span>更新：(\\d\\d\\d\\d-\\d\\d-\\d\\d)[^<]*</span>"));

			String tipHtml = toHtml(parseNodeByHtml(html, createEqualFilter("p class=\"jj\"")));
			book.setTip(tipHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

			Node node = parseNodeByHtml(html, createEqualFilter("div class=\"pic\""));
			ImageTag cover = (ImageTag) node.getChildren().elementAt(0);
			book.setCover("https://www.1200ksw.com" + cover.getImageURL());

			return book;

		} catch (Exception e) {
			return null;
		}
	}
}
