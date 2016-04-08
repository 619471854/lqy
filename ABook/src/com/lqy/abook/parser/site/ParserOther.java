package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class ParserOther extends ParserBase {

	@Override
	public boolean parserSearch(List<BookEntity> books, String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean parserSearchSite(List<BookEntity> books, String name, String author) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		try {
			List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
			if (chapters == null || chapters.size() == 0) {
				book.setLoadStatus(LoadStatus.failed);
				MyLog.i("ParserOther updateBookAndDict getChapters failed");
				return null;// 此书更新失败
			} else {
				return chapters;
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		book.setLoadStatus(LoadStatus.failed);
		return null;
	}

	@Override
	public boolean parserBookDetail(BookEntity detail) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		return parserBookDict(url, url, "gbk", false);
	}

	@Override
	public String getChapterDetail(String url) {
		return getChapterDetail(url, "gbk", false);
	}

	private String getChapterDetail(String url, String encodeType, boolean isChangeCode) {
		try {
			SimpleNodeIterator iterator = getParserResult(url, new NodeClassFilter(BodyTag.class), encodeType);
			MyLog.i("ParserOther asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				// Node node = iterator.nextNode();
				String html = iterator.nextNode().toPlainTextString();
				// html = html .substring(Math.min(html.length(),
				// 700),Math.min(html.length(), 1500));
				html = html.replaceAll(Config.nbsp, " ");
				html = html.replaceAll("　", " ");// 全角空格
				// 去掉多余的换行和空格，段落首行8格
				html = html.replaceAll("\\s{2,}", "\n        ");
				// 去掉没有标点的段落(这些一般不是内容)
				html = ("\n" + html).replaceAll("\n[^！”“，。；？……]+\n", "\n");
				return html.trim();
			}
		} catch (org.htmlparser.util.EncodingChangeException e) {
			MyLog.e(e);
			if (!isChangeCode) {
				return getChapterDetail(url, "gbk".equals(encodeType) ? "utf-8" : "gbk", true);
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	/**
	 * 获取所有链接
	 */
	public BookAndChapters parserBrowser(String url, String html) {
		String encodeType = matcher(html, "<meta http-equiv=\"[^\"]+\" content=\"[^\"]*charset=([^\"]+)\">");
		if (Util.isEmpty(encodeType))
			encodeType = "gbk";
		List<ChapterEntity> chapters = parserBookDict(url, html, encodeType, false);
		return new BookAndChapters(url, chapters);
	}

	private List<ChapterEntity> parserBookDict(String url, String urlOrHtml, String encodeType, boolean isChangeCode) {
		try {
			SimpleNodeIterator iterator = getParserResult(urlOrHtml, new NodeClassFilter(LinkTag.class), encodeType);
			MyLog.i("ParserOther ParserBrowser parserOther ok " + encodeType);
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			ChapterEntity e;
			// 获取域名
			String baseUrl = url.replace("http://", CONSTANT.EMPTY).replace("https://", CONSTANT.EMPTY);
			int index = baseUrl.indexOf("/");
			if (index == -1) {
				baseUrl = null;
			} else {
				baseUrl = url.substring(0, url.length() - (baseUrl.length() - index));
			}
			MyLog.i("ParserOther ParserBrowser baseUrl= " + baseUrl);

			while (iterator.hasMoreNodes()) {
				Node node = iterator.nextNode();
				if (!(node instanceof LinkTag))
					continue;
				LinkTag link = (LinkTag) node;
				String name = link.getLinkText();
				String chapterUrl = link.getLink();
				if (Util.isEmpty(name))
					continue;
				name = name.trim().replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s*", " ");
				if (Util.isEmpty(name))
					continue;
				if (Util.isEmpty(chapterUrl) || chapterUrl.startsWith("javascript") || chapterUrl.startsWith("#"))
					continue;
				if (!chapterUrl.startsWith("http")) {
					if (chapterUrl.startsWith("/")) {
						chapterUrl = baseUrl + chapterUrl;
					} else {
						chapterUrl = baseUrl + "/" + chapterUrl;
					}
				}
				e = new ChapterEntity();
				e.setName(name);
				e.setUrl(chapterUrl);
				e.setId(chapters.size());
				chapters.add(e);
				// MyLog.i(e.getName() + " " + e.getUrl());
			}
			return chapters;
		} catch (org.htmlparser.util.EncodingChangeException e) {
			MyLog.e(e);
			if (!isChangeCode) {
				return parserBookDict(url, urlOrHtml, "gbk".equals(encodeType) ? "utf-8" : "gbk", true);
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}
}
