package com.lqy.abook.parser.site;

import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;
import com.lqy.abook.tool.WebServer;

public class ParserBaidu extends ParserBase {

	protected static Config config = Config.getBaiduConfig();

	public ParserBaidu() {
		encodeType = "utf-8";
		site = Site.Baidu;
	}

	// 搜索小说
	public boolean parserSearch(List<BookEntity> books, String key) {
		try {
			String html = WebServer.hcGetData(config.searchUrl + key, encodeType);
			SimpleNodeIterator iterator = parseHtml(html, createStartFilter(config.searchFilter));
			int count = 0;
			while (iterator.hasMoreNodes() && (count++ < searchMaxSizeSite || books.size() < searchMaxSizeSite)) {
				Node node = iterator.nextNode();
				boolean success = processSearchNode(books, node, key.split(" "));
				if (!success)
					break;// 如果未匹配，后面的就不要了
			}
			return true;
		} catch (Exception e) {
			MyLog.e(e);
			return false;
		}
	}

	// 搜索小说所在的所在的site
	@Override
	public BookEntity parserSearchSite(String name, String author) {
		try {
			String html = WebServer.hcGetData(config.searchUrl + name + " " + author, encodeType);
			SimpleNodeIterator iterator = parseHtml(html, createStartFilter(config.searchFilter));
			while (iterator.hasMoreNodes()) {
				Node node = iterator.nextNode();
				BookEntity e = processSearchSiteNode(node, name, author);
				if (e != null)
					return e;
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		return true;
	}

	@Override
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		try {
			if (!Util.isEmpty(book.getDetailUrl()) && !updateBook(book)) {
				MyLog.i("ParserBaidu updateBookAndDict  此书没有更新");
				return null;
			}
			List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
			if (chapters == null || chapters.size() == 0) {
				book.setLoadStatus(LoadStatus.failed);
				MyLog.i("ParserBaidu updateBookAndDict getChapters failed");
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
		try {
			if (Util.isEmpty(detail.getName())) {
				SimpleNodeIterator iterator = getParserResult2(detail.getDetailUrl(), "div itemscope");
				MyLog.i("ParserBaidu parserBookDetail getParserResult ok");
				if (iterator.hasMoreNodes()) {
					String html = iterator.nextNode().toHtml();
					detail.setName(matcher(html, "<font\\s*itemprop=\"name\">(.+)</font>"));
					detail.setAuthor(matcher(html, "<span\\s*itemprop=\"author\"[^<]+<span>[^<]+<a[^<]+<font itemprop=\"name\">(.+)</font></a>"));
					detail.setCover(matcher(html, "<img\\s*itemprop=\"image\"\\s*src=\"([^\"]+)\""));

					detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
					detail.setCompleted(matcher(html, config.completedReg).length() > 0);
					detail.setNewChapter(matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " "));
					detail.setWords(Util.toInt(matcher(html, config.wordsReg2)));
					detail.setUpdateTime(matcher(html, config.updateTimeReg2));
				}
			} else {
				SimpleNodeIterator iterator = getParserResult(detail.getDetailUrl(), "div class=\"bookBox_r\"");
				MyLog.i("ParserBaidu parserBookDetail getParserResult ok");
				if (iterator.hasMoreNodes()) {
					String html = iterator.nextNode().toHtml();
					detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		return null;
	}

	@Override
	public String getChapterDetail(String url) {
		return null;
	}

	protected boolean processSearchNode(List<BookEntity> books, Node node, String[] searchKey) throws Exception {
		String text = node.getText();
		String html = node.toHtml();
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		if (text != null && text.indexOf("data-bookid") != -1) {
			String id = matcher(text, "data-bookid=\"(\\d+)\"");
			if (!Util.isEmpty(id))
				book.setDirectoryUrl("http://m.baidu.com/tc?version=2&book_id=" + id + "&router=pagetpl&appui=alaxs");
		} else {
			String id = matcher(text, "data-gid=\"(\\d+)\"");
			String src = matcher(text, "data-src=\"?([^\"\\s]+)\"?\\s").replaceAll(Config.amp, "&");
			if (!Util.isEmpty(id) && !Util.isEmpty(src))
				book.setDirectoryUrl("http://m.baidu.com/tc?appui=alaxs&srct=zw&gid=" + id + "&srd=1&src=" + src);
		}
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return false;// 不完善的数据
		book.setAuthor(matcher(html, config.authorReg).trim());

		if (searchKey.length == 1) {
			if (searchKey[0].equals(book.getName()))
				book.setMatchWords(MaxMatch);
			else
				book.setMatchWords(matcher(nameHtml, "<span>(\\S+)</span>").length());
		} else {
			if (searchKey[0].equals(book.getName()) && searchKey[1].equals(book.getAuthor()))
				book.setMatchWords(MaxMatch);
			else
				book.setMatchWords(matcher(nameHtml, "<span>(\\S+)</span>").length() + book.getAuthor().length());
		}
		book.setCover(matcher(html, config.coverReg).replaceAll(Config.amp, "&"));
		// book.setDetailUrl(null);
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		// book.setNewChapter(null);
		// book.setUpdateTime(null);
		// book.setWords(null);
		book.setCompleted(matcher(html, config.completedReg).trim().equals("完结"));

		MyLog.i("ParserBaidu search a book " + book.getName() + "  " + book.getAuthor());
		books.add(book);
		return true;
	}

	protected BookEntity processSearchSiteNode(Node node, String name, String author) throws Exception {
		String text = node.getText();
		String html = node.toHtml();
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		if (text != null && text.indexOf("data-bookid") != -1) {
			String id = matcher(text, "data-bookid=\"(\\d+)\"");
			if (!Util.isEmpty(id))
				book.setDirectoryUrl("http://m.baidu.com/tc?version=2&book_id=" + id + "&router=pagetpl&appui=alaxs");
		} else {
			String id = matcher(text, "data-gid=\"(\\d+)\"");
			String src = matcher(text, "data-src=\"?([^\"\\s]+)\"?\\s").replaceAll(Config.amp, "&");
			if (!Util.isEmpty(id) && !Util.isEmpty(src))
				book.setDirectoryUrl("http://m.baidu.com/tc?appui=alaxs&srct=zw&gid=" + id + "&srd=1&src=" + src);
		}
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return null;// 不完善的数据
		book.setAuthor(matcher(html, config.authorReg).trim());

		// 如果有作者，那么必须完全匹配
		if (!name.equals(book.getName()) || !author.equals(book.getAuthor())) {
			return null;// 继续找第二本
		}
		book.setCover(matcher(html, config.coverReg).replaceAll(Config.amp, "&"));
		// book.setDetailUrl(null);
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		// book.setNewChapter(null);
		// book.setUpdateTime(null);
		// book.setWords(null);
		book.setCompleted(matcher(html, config.completedReg).trim().equals("完结"));

		MyLog.i("ParserBaidu search a book " + book.getName() + "  " + book.getAuthor());
		return book;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html) {
		if (url.startsWith("http://m.baidu.com/tc"))
			return new BookAndChapters((BookEntity) null, null);
		else
			return null;
	}
}
