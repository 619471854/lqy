package com.lqy.abook.parser.site;

import java.net.URI;
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
import com.lqy.abook.entity.ExtEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;
import com.lqy.abook.tool.WebServer;

public class ParserOther extends ParserBase {

	@Override
	public boolean parserSearch(List<BookEntity> books, String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BookEntity parserSearchSite(String name, String author) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		try {
			List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl(), null, book.getExt());
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
		return null;
	}

	public List<ChapterEntity> parserBookDict(String url, String html, ExtEntity ext) {
		try {
			if (Util.isEmpty(html))
				html = WebServer.getDataOnCookie(url, ext.getCookie(), ext.getEncodeType());
			SimpleNodeIterator iterator = parseHtml(html, new NodeClassFilter(LinkTag.class));
			MyLog.i("ParserOther ParserBrowser parserOther ok " + ext.getEncodeType());
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			ChapterEntity e;
			// 获取域名
			String baseUrl = null;
			try {
				String path = new URI(url).getPath();
				int index = url.indexOf(path);
				if ("/".equals(path) || index < 1)
					baseUrl = url;
				else
					baseUrl = url = url.substring(0, index);
			} catch (Exception e2) {
			}
			if (Util.isEmpty(baseUrl)) {
				baseUrl = url.replace("http://", CONSTANT.EMPTY).replace("https://", CONSTANT.EMPTY);
				int index = baseUrl.indexOf("/");
				if (index == -1) {
					baseUrl = CONSTANT.EMPTY;
				} else {
					baseUrl = url.substring(0, url.length() - (baseUrl.length() - index));
				}
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

				if (!Util.isEmpty(baseUrl) && !chapterUrl.startsWith("http")) {
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
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	@Override
	public String getChapterDetail(String url) {
		return null;
	}

	public String getChapterDetail(String url, ExtEntity ext) {
		try {
			String html = WebServer.getDataOnCookie(url, ext.getCookie(), ext.getEncodeType());
			SimpleNodeIterator iterator = parseHtml(html, new NodeClassFilter(BodyTag.class));
			MyLog.i("ParserOther asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				// Node node = iterator.nextNode();
				html = iterator.nextNode().toPlainTextString();
				// html = html .substring(Math.min(html.length(),
				// 700),Math.min(html.length(), 1500));
				html = html.replaceAll(Config.nbsp, " ");
				html = html.replaceAll(Config.blank, " ");// 全角空格
				// 去掉多余的换行和空格，段落首行8格
				html = html.replaceAll("\\s{2,}", "\n        ");
				// 去掉没有标点的段落(这些一般不是内容)
				html = ("\n" + html).replaceAll("\n[^！”“，。；？……]+\n", "\n");
				return html.trim();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	/**
	 * 获取所有链接
	 */
	@Override
	public BookAndChapters parserBrowser(String url, String html) {
		return null;
	}

	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String encodeType = matcher(html, "<meta[\\s\\S]*charset=[\"']?([^\"']+)[\"']?>");
		if (Util.isEmpty(encodeType))
			return null;
		ExtEntity ext = new ExtEntity(cookie, encodeType);
		List<ChapterEntity> chapters = parserBookDict(url, html, ext);

		BookEntity book = new BookEntity();
		book.setDirectoryUrl(url);
		book.setSite(Site.Other);
		book.setExt(ext);

		return new BookAndChapters(book, chapters);
	}
}
