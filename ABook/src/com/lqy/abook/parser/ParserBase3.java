package com.lqy.abook.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.htmlparser.Node;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public abstract class ParserBase3 extends ParserBase2 {
	@Override
	public boolean updateBook(BookEntity book) {
		return false;
	}

	protected String getSearchEncodeType() {
		return "utf-8";
	}

	@Override
	public boolean parserBookDetail(BookEntity book) {
		Node node = parseNodeByUrl(book.getDirectoryUrl(), createEqualFilter("div id=\"intro\""), encodeType);
		String html = toHtml(node);
		if (html != null) {
			book.setTip(html.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
			return true;
		}
		return false;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		try {
			SimpleNodeIterator iterator = parseUrl(url, createEqualFilter("div id=\"list\""), encodeType);
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				return parserBookDictByHtml(url, html);
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	@Override
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		try {
			SimpleNodeIterator iterator = parseUrl(book.getDirectoryUrl(), createEqualFilter("div id=\"list\""), encodeType);
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			if (iterator.hasMoreNodes()) {
				Node dict = iterator.nextNode();
				List<ChapterEntity> chapters = parserBookDictByHtml(book.getDirectoryUrl(), dict.toHtml());
				if (chapters == null || chapters.size() == 0) {
					book.setLoadStatus(LoadStatus.failed);
					MyLog.i("ParserOther updateBookAndDict getChapters failed");
					return null;// 此书更新失败
				}
				String newChapter = chapters.get(chapters.size() - 1).getName();
				if (newChapter.equals(book.getNewChapter())) {
					return null;// 此书没有更新
				}
				book.setLoadStatus(LoadStatus.hasnew);
				book.setNewChapter(newChapter);
				return chapters;
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		book.setLoadStatus(LoadStatus.failed);
		return null;
	}

	protected String getChapterDetail(String url, String filterReg) {
		try {
			SimpleNodeIterator iterator = parseUrl(url, createEqualFilter("div id=\"content\""), encodeType);
			MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				html = matcher(html, filterReg);
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

	protected static List<ChapterEntity> parserBookDictByHtml(String urlRoot, String h) {
		if (Util.isEmpty(h))
			return null;
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

	protected boolean processSearchNode(List<BookEntity> books, String html, String[] searchKey) throws Exception {
		Config config = getConfig();

		BookEntity book = new BookEntity();
		book.setSite(site);
		Matcher m = getMatcher(html, config.nameReg);
		if (m == null)
			return true;
		book.setName(m.group(2));
		book.setDirectoryUrl(m.group(1));

		if (setDetailUrl(book))// 去掉
			return true;

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

		// MyLog.i(TAG, "search a book " + book.getName() + "  " +
		// book.getAuthor());
		MyLog.i(TAG, "search a book " + book.toString());
		books.add(book);
		return true;
	}

	protected boolean setDetailUrl(BookEntity book) {
		String url = book.getDirectoryUrl();
		if (url == null)
			return true;
		if (!url.endsWith("/"))
			url += "/";
		book.setDetailUrl(url);
		return false;
	}

	protected BookEntity processSearchSiteNode(String html, String name, String author) throws Exception {
		Config config = getConfig();
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		Matcher m = getMatcher(html, config.nameReg);
		if (m == null)
			return null;
		book.setName(m.group(2));
		book.setDirectoryUrl(m.group(1));
		if (setDetailUrl(book))// 去掉
			return null;

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

		MyLog.i(TAG, "search a book " + book.getName() + "  " + book.getAuthor());

		return book;// 已找到
	}

	protected BookEntity parserBookDetail(String url, String html, String coverUrlRoot) {
		try {
			String infoHtml = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"info\"")));
			BookEntity book = new BookEntity();
			book.setSite(site);
			book.setName(matcher(infoHtml, "<h1>([^<]*)</h1>"));
			book.setAuthor(matcher(infoHtml, "<p>作&nbsp;&nbsp;&nbsp;&nbsp;者：([^<]*)</p>"));
			book.setUpdateTime(matcher(infoHtml, "<p>最后更新：(\\d\\d\\d\\d-\\d\\d-\\d\\d)[^<]*</p>"));

			String tipHtml = toHtml(parseNodeByHtml(html, createEqualFilter("div id=\"intro\"")));
			book.setTip(tipHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

			Node node = parseNodeByHtml(html, createEqualFilter("div id=\"fmimg\""));
			ImageTag cover = (ImageTag) node.getChildren().elementAt(0);
			book.setCover(coverUrlRoot + cover.getImageURL());

			return book;

		} catch (Exception e) {
			return null;
		}
	}
}
