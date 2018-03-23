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

public class ParserBiquge extends ParserBase3 {
	private static Config config = Config.getBiqugeConfig();

	public ParserBiquge() {
		encodeType = "utf-8";
		site = SiteEnum.Biquge;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		String html = toHtml(parseNodeByUrl(url, createEqualFilter("div id=\"list\""), encodeType));
		if (!Util.isEmpty(html)) {
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			String urlRoot = url.contains("biqudu") ? "https://www.biqudu.com" : url;
			return parserBookDictByHtml(urlRoot, html);
		}
		return null;
	}

	@Override
	public String getChapterDetail(String url) {
		boolean isOld = url.contains("www.biqudu.com");
		if (isOld) {
			// String html = toHtml(parseNodeByUrl(url,
			// createEqualFilter("div id=\"content\""), encodeType));
			// if (!Util.isEmpty(html)) {
			// MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
			// html = matcher(html,
			// "<div id=\"content\">\\s*(((?!</div>)[\\s\\S])+)\\s*</div>");
			// html = html.replaceAll(Config.blank, CONSTANT.EMPTY);// 替换全角空格
			// html = html.replaceAll("\\s+", CONSTANT.EMPTY);
			// html = html.replaceAll(Config.lineWrapReg, "\n");
			// html = html.replaceAll("\n+", "\n        ");
			// return html.trim();
			// }
			String text = toText(parseNode(url, null, createEqualFilter("div id=\"content\""), encodeType));
			if (!Util.isEmpty(text)) {
				MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
				text = text.replaceAll(Config.blank + Config.blank, "\n        ");
				String text2 = matcher(text, "readx\\(\\);([\\s\\S]+)chaptererror\\(\\);");
				if (!Util.isEmpty(text2))
					text = text2;
				return text.trim();
			}
		} else {
			String text = toText(parseNode(url, null, createEqualFilter("div id=\"content\""), encodeType));
			if (!Util.isEmpty(text)) {
				MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
				text = text.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\n        ");
				return text.trim();
			}
		}
		return null;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String id = matcher(url, "https://m\\.biqudu\\.com/(\\d+_\\d+)/?");
		if (Util.isEmpty(id)) {
			id = matcher(url, "https://m\\.biqudu\\.com/booklist/(\\d+).html/?");
			if (!Util.isEmpty(id)) {
				id = "1_" + id;
			} else {
				id = matcher(url, "http://m\\.biquke\\.com/bq/(\\d+/\\d+)/?");
			}
		}
		boolean isOld = true;
		if (Util.isEmpty(id)) {
			id = matcher(url, "https://www\\.biqudu\\.com/(\\d+_\\d+)/?");
			if (Util.isEmpty(id))
				return null;
			html = null;// 重新加载电脑版网页,直接用html有问题
		} else {
			html = null;// 手机端网页，需要重新加载电脑版网页
			isOld = id.contains("_");
			if (isOld)
				url = "https://www.biqudu.com/" + id + "/";
			else
				url = "http://www.biquke.com/bq/" + id + "/";
		}
		if (!url.endsWith("/"))
			url += "/";

		try {
			html = toHtml(parseNode(url, html, createEqualFilter("div id=\"wrapper\""), encodeType));

			MyLog.i(TAG, "parserBookDict getParserResult ok");
			if (!Util.isEmpty(html)) {
				// 获取内容
				BookEntity book = parserBookDetail(html, isOld);
				MyLog.i(TAG, "getBookAndDict book  " + book == null ? null : book.getName());
				// 获取章节
				html = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"list\"")));

				String urlRoot = isOld ? "https://www.biqudu.com" : url;
				List<ChapterEntity> chapters = parserBookDictByHtml(urlRoot, html);
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

	protected BookEntity parserBookDetail(String html, boolean isOld) {
		try {
			String infoHtml = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"info\"")));
			BookEntity book = new BookEntity();
			book.setSite(site);
			book.setName(matcher(infoHtml, "<h1>([^<]*)</h1>"));
			book.setAuthor(matcher(infoHtml, "<p>作&nbsp;&nbsp;&nbsp;&nbsp;者：([^<]*)</p>"));
			// book.setUpdateTime(matcher(infoHtml,
			// "<p>最后更新：(\\d{4}/\\d{1,2}/\\d{1,2})[^<]*</p>"));
			// MyLog.i(book.getUpdateTime());

			String tipHtml = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"intro\"")));
			book.setTip(tipHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s+", CONSTANT.EMPTY)
					.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\n        "));

			Node node = parseNodeByHtml(html, createEqualFilter("div id=\"fmimg\""));
			ImageTag cover = (ImageTag) node.getChildren().elementAt(1);
			if (isOld) {
				book.setCover("https://www.biqudu.com" + cover.getImageURL());
			} else {
				book.setCover("http://www.biquke.com" + cover.getImageURL());
			}

			return book;

		} catch (Exception e) {
			return null;
		}
	}
}
