package com.lqy.abook.parser.site;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.entity.ResultEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.parser.ParserResult;
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
			key = key.replaceAll("'", "").replaceAll("\\s", " ");// 去除单引号和多余的空格
			SimpleNodeIterator iterator = getParserResult("http://m.baidu.com/tc?appui=alaxs&srct=zw&gid=4145020416&srd=1&src=http://www.wxguan.com/wenzhang/0/990/",new NodeClassFilter(BodyTag.class),encodeType);
			ResultEntity e=	WebServer.hcGetData(config.searchUrl, encodeType);
			MyLog.i("ParserBaidu Search ok,parsering "+e.getMsg());
			int count = 0;
			while (iterator.hasMoreNodes() && (count++ < searchMaxSizeSite || books.size() < searchMaxSizeSite)) {
				Node node = iterator.nextNode();
				boolean success = processSearchNode(books, node, key.split(" "));
				if (!success)
					break;// 如果未匹配，后面的就不要了
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// 搜索小说所在的所在的site
	public boolean parserSearchSite(List<BookEntity> books, String name, String author) {
		try {
			SimpleNodeIterator iterator = getParserResult2(config.searchUrl + name + author, config.searchFilter);
			MyLog.i("ParserBaidu SearchSite ok,parsering");
			while (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				// 完全匹配到了1个就可以了
				// if (processSearchSiteNode(books, html, name, author))
				// break;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean updateBook(BookEntity book) {
		try {
			SimpleNodeIterator iterator = getParserResult(book.getDetailUrl(), "div class=\"bookBox_r\"");
			MyLog.i("ParserBaidu updateBook getParserResult ok");
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
			}
			return true;
		} catch (Exception e) {
			MyLog.e(e);
		}
		return false;
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
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = getParserResult(url, "li");
			MyLog.i("ParserBaidu parserBookDict getParserResult ok");
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
			MyLog.i("ParserBaidu asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				html = matcher(html, "<div id=\"chapterContentWapper\">\\s*(((?!本书首发来自)[\\s\\S])+)");
				html = html.replaceAll(Config.lineWrapReg, "\n");
				html = html.replaceAll("\r\n", "\n");
				html = html.replaceAll("\n{2,}+", "\n");
				html = html.replaceAll("　", "    ");// 替换全角空格为4个半角空格
				return html.trim();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	protected boolean processSearchNode(List<BookEntity> books, Node node, String[] searchKey) throws Exception {
		String html = node.getText();
		MyLog.i(node.toHtml());
		if (true)
			return false;
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

		MyLog.i("ParserBaidu search a book " + book.getName() + "  " + book.getAuthor());
		books.add(book);
		return true;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public ParserResult parserBrowser(String url, String html) {
		if (Util.isEmpty(url))
			return null;
		String url2 = URLDecoder.decode(url);
		// http://www.17k.com/list/40082.html
		// http://h5.17k.com/list/391013.html
		String id = matcher(url2, "^http://www\\.17k\\.com/list/(\\d+)\\.html$");
		if (Util.isEmpty(id)) {
			id = matcher(url2, "^http://h5\\.17k\\.com/list/(\\d+)\\.html$");
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
			if (url2.equals(directUrl)) {
				chaters = parserBookDict(html);
			} else {
				chaters = parserBookDict(directUrl);
			}
			return new ParserResult(book, chaters);
		}
		return null;
	}
}
