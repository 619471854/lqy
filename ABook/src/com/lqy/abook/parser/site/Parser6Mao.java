package com.lqy.abook.parser.site;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.ImageTag;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatusEnum;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase3;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class Parser6Mao extends ParserBase3 {
	private static Config config = Config.get6MaoConfig();

	public Parser6Mao() {
		encodeType = "gbk";
		site = SiteEnum._6Mao;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public boolean parserBookDetail(BookEntity book) {
		String html = toHtml(parseNodeByUrl(book.getDirectoryUrl(), createEqualFilter("div class=\"intro\""), encodeType));
		if (!Util.isEmpty(html)) {
			book.setType(matcher(html, "<span\\s*class=\"sort\">类型：([^<]+)</span>"));
			book.setTip(matcher(html, "<li\\s*class=\"jieshao\">\\s*<span>([\\s\\S]+?)</span>\\s*</li>").replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll(
					"\\s+", CONSTANT.EMPTY));
			return true;
		}
		return false;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		String html = toHtml(parseNodeByUrl(url, createEqualFilter("div class=\"liebiao_bottom\""), encodeType));
		if (!Util.isEmpty(html)) {
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			return parserBookDictByHtml("http://www.6mao.com", html);
		}
		return null;
	}

	@Override
	public String getChapterDetail(String url) {
		String text = toText(parseNode(url, null, createEqualFilter("div id=\"neirong\""), encodeType));
		if (!Util.isEmpty(text)) {
			MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
			text = text.replaceAll(Config.nbsp, "  ");
			text = text.replaceAll("\r\n", "\n");
			text = text.replaceAll("\n{2,}+", "\n");
			return text.trim();
		}
		return null;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String id = matcher(url, "http://m\\.6mao\\.com/wapbook/(\\d+)\\.html/?");

		if (Util.isEmpty(id)) {
			id = matcher(url, "http://www\\.6mao\\.com/html/(\\d+/\\d+)/index\\.html?");
			if (Util.isEmpty(id))
				return null;
			html = null;// 重新加载电脑版网页,直接用html有问题
		} else {
			html = null;// 手机端网页，需要重新加载电脑版网页
			url = "http://www.6mao.com/html/" + getTypeId(id, url, html) + "/" + id + "/index.html";
		}

		try {
			html = toHtml(parseNode(url, html, createEqualFilter("div class=\"liumaoxs_all\""), encodeType));

			MyLog.i(TAG, "parserBookDict getParserResult ok");
			if (!Util.isEmpty(html)) {
				// 获取内容
				BookEntity book = parserBookDetail(url, html);
				MyLog.i(TAG, "getBookAndDict book  " + book == null ? null : book.getName());
				// 获取章节
				html = toHtml(parseNodeByHtml(html, createEqualFilter("div class=\"liebiao_bottom\"")));
				List<ChapterEntity> chapters = parserBookDictByHtml("http://www.6mao.com", html);
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

	private String getTypeId(String id, String url, String html) {
		if (Util.isEmpty(id))
			return CONSTANT.EMPTY;
		final Pattern p = Pattern.compile("files/article/image/(\\d+)/" + id + "/" + id + "s\\.jpg");
		NodeFilter filter = new NodeFilter() {
			public boolean accept(Node node) {
				if (node instanceof ImageTag) {
					String src = ((ImageTag) node).getImageURL();
					Matcher m = p.matcher(src);
					return m.find();
				}
				return false;
			}
		};
		Node node = parseNode(url, html, filter, encodeType);
		if (node != null) {
			String src = ((ImageTag) node).getImageURL();
			Matcher m = p.matcher(src);
			if (m.find()) {
				return m.group(1);
			}
		}
		return id.substring(0, Math.max(1, id.length() - 3));
	}

	protected BookEntity parserBookDetail(String url, String html) {
		try {
			html = toHtml(parseNodeByHtml(html, createEqualFilter("div class=\"intro\"")));
			BookEntity book = new BookEntity();
			book.setSite(site);
			book.setCover("http://www.6mao.com" + matcher(html, "<img[\\s\\S]+?src=\"([^\"]+)\""));
			book.setName(matcher(html, "<li\\s*class=\"title\">\\s*<h2>([^<]+)</h2>\\s*</li>"));
			book.setAuthor(matcher(html, "<span\\s*class=\"author\">作者：([^<]+)</span>"));
			book.setUpdateTime(matcher(html, "最后更新:(\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2})"));

			book.setTip(matcher(html, "<li\\s*class=\"jieshao\">\\s*<span>([\\s\\S]+?)</span>\\s*</li>").replaceAll("\\s+", CONSTANT.EMPTY));

			return book;

		} catch (Exception e) {
			return null;
		}
	}

	protected boolean setDetailUrl(BookEntity book) {
		book.setDetailUrl(book.getDirectoryUrl());
		return false;
	}
}
