package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase2;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class Parser17K extends ParserBase2 {
	private static Config config = Config.get17KConfig();

	public Parser17K() {
		encodeType = "gbk";
		site = Site._17K;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		try {
			SimpleNodeIterator iterator = getParserResult(book.getDetailUrl(), "div class=\"bookBox_r\"");
			MyLog.i("Parser17K updateBook getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				String newChapter = matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " ");
				if (newChapter.equals(book.getNewChapter())) {
					return false;// 此书没有更新
				}
				book.setLoadStatus(LoadStatus.hasnew);
				book.setNewChapter(newChapter);
				book.setWords(Util.toInt(matcher(html, config.wordsReg2)));
				// book.setUpdateTime(matcher(html, ));

				book.setCompleted(matcher(html, config.completedReg).length() > 0);
				return true;
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return false;
	}

	@Override
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		try {
			if (!Util.isEmpty(book.getDetailUrl()) && !updateBook(book)) {
				MyLog.i("Parser17K updateBookAndDict  此书没有更新");
				return null;
			}
			List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
			if (chapters == null || chapters.size() == 0) {
				book.setLoadStatus(LoadStatus.failed);
				MyLog.i("Parser17K updateBookAndDict getChapters failed");
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
				SimpleNodeIterator iterator = parseUrl(detail.getDetailUrl(), createStartFilter("div itemscope"), encodeType);
				MyLog.i("Parser17K parserBookDetail getParserResult ok");
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
				MyLog.i("Parser17K parserBookDetail getParserResult ok");
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
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = getParserResult(url, "li");
			MyLog.i("Parser17K parserBookDict getParserResult ok");
			ChapterEntity e;
			String urlRoot = "http://www.17k.com";
			while (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				e = new ChapterEntity();
				// e.setId(matcher(html, "id=\"tips_(\\d+)\"").replaceAll("\\s",
				// CONSTANT.EMPTY));
				e.setName(matcher(html, "tn=\"([^\"]+)\""));
				boolean isVip = html.indexOf("<span class=\"red\">VIP</span>") != -1;
				if (isVip) {
					e.setLoadStatus(LoadStatus.vip);
				} else {
					e.setUrl(urlRoot + matcher(html, "href=\"([^\"]+)\""));
				}
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
			SimpleNodeIterator iterator = getParserResult(url, "div id=\"chapterContentWapper\"");
			MyLog.i("Parser17K asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				html = matcher(html, "<div id=\"chapterContentWapper\">\\s*(((?!本书首发来自)[\\s\\S])+)");
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
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
		book.setDirectoryUrl(matcher(html, config.directoryUrlReg));
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return false;// 不完善的数据
		String authorHtml = matcher(html, config.authorReg);
		book.setAuthor(authorHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		if (searchKey.length == 1) {
			if (searchKey[0].equals(book.getName()))
				book.setMatchWords(MaxMatch);
			else
				book.setMatchWords(nameHtml.split(config.keyReg).length + authorHtml.split(config.keyReg).length - 2);
		} else {
			if (searchKey[0].equals(book.getName()) && searchKey[1].equals(book.getAuthor()))
				book.setMatchWords(MaxMatch);
			else
				book.setMatchWords(nameHtml.split(config.keyReg).length + authorHtml.split(config.keyReg).length - 2);
		}
		book.setCover(matcher(html, config.coverReg));
		book.setDetailUrl(matcher(html, config.detailUrlReg));
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg).trim().replaceAll("\\s", " "));
		book.setUpdateTime(matcher(html, config.updateTimeReg).trim());
		book.setWords(Util.toInt(matcher(html, config.wordsReg)));

		MyLog.i("Parser17K search a book " + book.getName() + "  " + book.getAuthor());
		books.add(book);
		return true;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html) {
		// http://www.17k.com/list/40082.html
		// http://h5.17k.com/list/391013.html
		String id = matcher(url, "^http://www\\.17k\\.com/list/(\\d+)\\.html$");
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://h5\\.17k\\.com/list/(\\d+)\\.html$");
		}
		if (Util.isEmpty(id))
			return null;
		String detailUrl = "http://www.17k.com/book/" + id + ".html";
		String directUrl = "http://www.17k.com/list/" + id + ".html";
		BookEntity book = new BookEntity();
		book.setDetailUrl(detailUrl);
		book.setDirectoryUrl(directUrl);
		if (parserBookDetail(book)) {
			book.setSite(site);
			List<ChapterEntity> chaters = null;
			if (url.equals(directUrl)) {
				chaters = parserBookDict(html);
			} else {
				chaters = parserBookDict(directUrl);
			}
			return new BookAndChapters(book, chaters);
		}
		return null;
	}
}
