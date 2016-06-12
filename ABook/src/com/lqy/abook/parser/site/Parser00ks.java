package com.lqy.abook.parser.site;

import java.util.List;
import java.util.regex.Matcher;

import org.htmlparser.Node;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase3;
import com.lqy.abook.tool.CONSTANT;
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
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		try {
			SimpleNodeIterator iterator = parseUrl(book.getDirectoryUrl(), createEqualFilter("div id=\"wrapper\""), "gbk");
			MyLog.i("ParserShuyue parserBookDict getParserResult ok");
			if (iterator.hasMoreNodes()) {
				// 获取章节
				NodeList children = iterator.nextNode().getChildren();
				Node dict = children.elementAt(13);
				dict = dict.getChildren().elementAt(1);
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

	@Override
	public String getChapterDetail(String url) {
		try {
			SimpleNodeIterator iterator = parseUrl(url, createEqualFilter("div id=\"content\""), "gbk");
			MyLog.i("Parser00ks asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				html = matcher(html, "<div id=\"content\">\\s*(((?!</div>)[\\s\\S])+)\\s*</div>");
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
		String url = m.group(1);
		if (url.contains("m.00ksw.com"))// 去掉手机版
			return true;
		if (!url.endsWith("/"))
			url += "/";
		book.setDetailUrl(url);
		book.setDirectoryUrl(url);
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

		// MyLog.i("Parser00ks search a book " + book.getName() + "  " +
		// book.getAuthor());
		MyLog.i("Parser00ks search a book " + book.toString());
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
		String url = m.group(1);
		if (url.contains("m.00ksw.com"))// 去掉手机版
			return null;
		if (!url.endsWith("/"))
			url += "/";
		book.setDetailUrl(url);
		book.setDirectoryUrl(url);

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

		MyLog.i("Parser00ks search a book " + book.getName() + "  " + book.getAuthor());

		return book;// 已找到
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
			SimpleNodeIterator iterator = null;
			if (Util.isEmpty(html)) {
				iterator = parseUrl(url, createEqualFilter("div id=\"wrapper\""), "gbk");
			} else {
				iterator = parseHtml(html, createEqualFilter("div id=\"wrapper\""));
			}
			MyLog.i("Parser00ks parserBookDict getParserResult ok");
			if (iterator.hasMoreNodes()) {
				NodeList children = iterator.nextNode().getChildren();
				Node dict = children.elementAt(13);
				Node detail = children.elementAt(9);
				dict = dict.getChildren().elementAt(1);
				children = null;
				// 获取内容
				BookEntity book = parserBookDetail(url, detail.toHtml());
				MyLog.i("Parser00ks getBookAndDict book  " + book == null ? null : book.getName());
				// 获取章节
				List<ChapterEntity> chapters = parserBookDictByHtml(url, dict.toHtml());
				if (chapters == null || chapters.size() == 0) {
					MyLog.i("Parser00ks getBookAndDict getChapters failed");
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
			book.setCover("http://www.00ksw.com" + cover.getImageURL());

			return book;

		} catch (Exception e) {
			return null;
		}
	}
}
