package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase2;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class ParserShuyue extends ParserBase2 {
	protected static Config config = Config.getShuyueConfig();

	public ParserShuyue() {
		encodeType = "utf-8";
		site = Site.Shuyue;
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
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		return null;
	}

	@Override
	public boolean parserBookDetail(BookEntity detail) {
		return true;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = parseUrl(url, createEqualFilter("div id=\"list\""), "gbk");
			MyLog.i("ParserShuyue parserBookDict getParserResult ok");
			if (!url.endsWith("/"))
				url += "/";
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				return parserBookDictByHtml(url, html);
			}
			return chapters;
		} catch (Exception e) {
			MyLog.e(e);
			return null;
		}
	}

	public List<ChapterEntity> parserBookDictByHtml(String urlRoot, String h) {
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = parseHtml(h, new NodeClassFilter(LinkTag.class));

			ChapterEntity e;
			while (iterator.hasMoreNodes()) {
				LinkTag node = (LinkTag) iterator.nextNode();
				e = new ChapterEntity();

				e.setName(node.getLinkText() == null ? CONSTANT.EMPTY : node.getLinkText().trim());
				e.setUrl(urlRoot + node.getLink());
				e.setId(chapters.size());
				if (!Util.isEmpty(e.getName()))
					chapters.add(e);
			}
			return chapters;
		} catch (Exception e) {
			MyLog.e(e);
			return null;
		}
	}

	@Override
	public String getChapterDetail(String url) {
		try {
			SimpleNodeIterator iterator = parseUrl(url, createEqualFilter("div id=\"content\""), "gbk");
			MyLog.i("ParserShuyue asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				html = matcher(html, "为您提供精彩小说阅读。\\s*(((?!手机用户请浏览m.shuyuewu.com阅读)[\\s\\S])+)");
				html = html.replaceAll(Config.nbsp, "  ");
				html = html.replaceAll(Config.lineWrapReg, "\n");
				html = html.replaceAll("\r\n", "\n");
				html = html.replaceAll("\n{2,}+", "\n");
				html = html.replaceAll(Config.blank, "    ");// 替换全角空格为4个半角空格
				return html.trim();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	protected boolean processSearchNode(List<BookEntity> books, String html, String[] searchKey) throws Exception {
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		Matcher m = getMatcher(html, config.nameReg);
		if (m == null)
			return true;
		book.setName(m.group(2));
		book.setDetailUrl(m.group(1));
		book.setDirectoryUrl(m.group(1));
		int matchWord1 = m.group(4) == null ? 0 : m.group(4).trim().length();

		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return false;// 不完善的数据
		String authorHtml = matcher(html, config.authorReg);
		book.setAuthor(authorHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		int matcherWord2 = getMatcherCount(authorHtml, "<em>([^<]*)</em>");
		if (searchKey[0].equals(book.getName()))
			book.setMatchWords(MaxMatch);
		else
			book.setMatchWords(matchWord1 + matcherWord2);

		book.setCover(matcher(html, config.coverReg));
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg).trim().replaceAll("\\s", " "));
		book.setUpdateTime(matcher(html, config.updateTimeReg).trim());

		// MyLog.i("ParserShuyue search a book " + book.getName() + "  " +
		// book.getAuthor());
		MyLog.i("ParserShuyue search a book " + book.toString());
		books.add(book);
		return true;
	}

	protected BookEntity processSearchSiteNode(String html, String name, String author) throws Exception {
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		Matcher m = getMatcher(html, config.nameReg);
		if (m == null)
			return null;
		book.setName(m.group(2));
		book.setDetailUrl(m.group(1));
		book.setDirectoryUrl(m.group(1));

		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return null;// 不完善的数据
		String authorHtml = matcher(html, config.authorReg);
		book.setAuthor(authorHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		// 如果有作者，那么必须完全匹配
		if (!name.equals(book.getName()) || !author.equals(book.getAuthor())) {
			return null;// 继续找第二本
		}

		book.setCover(matcher(html, config.coverReg));
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg).trim().replaceAll("\\s", " "));
		book.setUpdateTime(matcher(html, config.updateTimeReg).trim());

		MyLog.i("ParserShuyue search a book " + book.getName() + "  " + book.getAuthor());

		return book;// 已找到
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url2, String html) {
		return null;
	}
}
