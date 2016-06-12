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
import com.lqy.abook.parser.ParserBase;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;
import com.lqy.abook.tool.WebViewParser;

public class Parser16K extends ParserBase {
	private static Config config = Config.get16KConfig();

	public Parser16K() {
		encodeType = "gbk";
		site = Site._16K;
	}

	@Override
	public boolean parserSearch(List<BookEntity> books, String key) {
		if (Util.isEmpty(key)) {
			return false;
		}
		// 如果有空格，就搜索关键字，否则先搜索作者再搜索名字
		String values[] = key.split(" ");
		if (values.length == 1) {
			// 搜关键字效果不好，改为先搜名字，如果搜不到就搜作者
			// String searchJs =
			// "javascript:var a=document.getElementsByTagName('form')[0];a.searchtype.value='keywords';a.searchkey.value='"
			// + values[0] + "';a.submit();";
			// String html = WebViewParser.getSearchResult(config.searchUrl,
			// searchJs);
			// return parserSearch(books, html, values);

			String searchJs = "javascript:var a=document.getElementsByTagName('form')[0];a.searchtype.value='articlename';a.searchkey.value='" + values[0]
					+ "';a.submit();";
			String html = WebViewParser.getSearchResult(config.searchUrl, searchJs);
			boolean re = parserSearch(books, html, values);
			if (books.size() == 0) {
				searchJs = "javascript:var a=document.getElementsByTagName('form')[0];a.searchtype.value='author';a.searchkey.value='" + values[0]
						+ "';a.submit();";
				html = WebViewParser.getSearchResult(config.searchUrl, searchJs);
				re = re || parserSearch(books, html, values);
			}
			return re;
		} else {
			String searchJs = "javascript:var a=document.getElementsByTagName('form')[0];a.searchtype.value='author';a.searchkey.value='" + values[1]
					+ "';a.submit();";
			String html = WebViewParser.getSearchResult(config.searchUrl, searchJs);
			boolean re = parserSearch(books, html, values);
			searchJs = "javascript:var a=document.getElementsByTagName('form')[0];a.searchtype.value='articlename';a.searchkey.value='" + values[0]
					+ "';a.submit();";
			html = WebViewParser.getSearchResult(config.searchUrl, searchJs);
			re = re || parserSearch(books, html, values);
			return re;
		}
	};

	// 搜索小说所在的所在的site
	@Override
	public BookEntity parserSearchSite(String name, String author) {
		return null;
	}

	private boolean parserSearch(List<BookEntity> books, String html, String[] keys) {
		if (html == null)
			return false;
		try {
			MyLog.i("Parser16K Search  parseHtmlByWebView ok");
			int index = html.indexOf("===");
			String resultUrl = html.substring(0, index);// 搜索结果的url地址
			html = html.substring(index + 3);
			if (resultUrl.equals(config.searchUrl)) {// search html
				SimpleNodeIterator iterator = getParserResult(html, config.searchFilter);
				MyLog.i("Parser16K Search ok,parsering");
				int count = 0;
				while (iterator.hasMoreNodes() && (count++ < searchMaxSizeSite || books.size() < searchMaxSizeSite)) {
					html = iterator.nextNode().toHtml();
					boolean success = processSearchNode(books, html, keys);
					if (!success)
						break;// 如果未匹配，后面的就不要了
				}
			} else {// 如果只有1本，取出的是目录界面
				SimpleNodeIterator iterator = getParserResult(html, "div class=\"one\"");
				MyLog.i("Parser16K Search ok is directory");
				if (iterator.hasMoreNodes()) {
					html = iterator.nextNode().toHtml();
					BookEntity book = processSearchDirectNode(html, resultUrl);
					if (book != null)
						books.add(book);
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean updateBook(BookEntity book) {
		try {
			SimpleNodeIterator iterator = getParserResult(book.getDirectoryUrl(), "div class=\"hotTag\"");
			MyLog.i("Parser16K updateBook getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				String newChapter = matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " ");
				if (newChapter.equals(book.getNewChapter())) {
					return false;// 此书没有更新
				}
				book.setLoadStatus(LoadStatus.hasnew);
				book.setNewChapter(newChapter);
				// book.setUpdateTime(matcher(html, ));
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
			SimpleNodeIterator iterator = getParserResult(book.getDirectoryUrl(), "div class=\"wrap\"");
			MyLog.i("Parser16K updateBookAndDict getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				iterator = getParserResult(html, "div class=\"hotTag\"");
				if (iterator.hasMoreNodes()) {
					String detail = iterator.nextNode().toHtml();
					String newChapter = matcher(detail, config.newChapterReg2).trim().replaceAll("\\s", " ");
					if (newChapter.equals(book.getNewChapter())) {
						MyLog.i("Parser16K updateBookAndDict  此书没有更新");
						return null;// 此书没有更新
					}
					book.setLoadStatus(LoadStatus.hasnew);
					book.setNewChapter(newChapter);
				} else {
					MyLog.i("Parser16K updateBookAndDict getNewChapter failed");
					book.setLoadStatus(LoadStatus.failed);
					return null;// 此书更新失败
				}
				List<ChapterEntity> chapters = parserBookDict(html);
				if (chapters == null || chapters.size() == 0) {
					book.setLoadStatus(LoadStatus.failed);
					MyLog.i("Parser16K updateBookAndDict getChapters failed");
					return null;// 此书更新失败
				} else {
					return chapters;
				}
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
			if (!Util.isEmpty(detail.getCover()) || !Util.isEmpty(detail.getTip()))
				return true;// 从目录界面过来
			// 从搜索列表过来，更新封面和tip
			SimpleNodeIterator iterator = getParserResult(detail.getDirectoryUrl(), "div class=\"one\"");
			MyLog.i("Parser16K parserBookDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				detail.setCover(matcher(html, config.coverReg));
				detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
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
			SimpleNodeIterator iterator = getParserResult(url, "dd");
			MyLog.i("Parser16K parserBookDict getParserResult ok");
			ChapterEntity e;
			String urlRoot = url.endsWith("/") ? url : url + '/';
			while (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				e = new ChapterEntity();
				e.setName(matcher(html, "title=\"([^\"]+)\""));
				e.setUrl(urlRoot + matcher(html, "href=\"([^\"]+)\""));
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
			SimpleNodeIterator iterator = parseUrl(url, createStartFilter("div id=\"htmlContent\""), encodeType);
			MyLog.i("Parser16K asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				html = html.replaceAll(Config.lineWrapReg, "\n");
				html = html.replaceAll("\r\n", "\n");
				html = html.replaceAll("\n{2,}+", "\n");
				html = html.replaceAll(Config.tagReg, "");
				html = html.replaceAll(Config.nbsp, "  ");
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
		book.setName(matcher(html, config.nameReg).replaceAll("\\s", CONSTANT.EMPTY));
		book.setDirectoryUrl(matcher(html, config.directoryUrlReg));
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return false;// 不完善的数据
		for (BookEntity e : books) {
			if (e.getDirectoryUrl().equals(book.getDirectoryUrl()))
				return false;// 已处理
		}

		book.setAuthor(matcher(html, config.authorReg).replaceAll("\\s", CONSTANT.EMPTY));

		if (searchKey.length == 1) {
			if (searchKey[0].equals(book.getName()) || searchKey[0].equals(book.getAuthor()))
				book.setMatchWords(MaxMatch);
			else if (book.getName().contains(searchKey[0]) || book.getAuthor().contains(searchKey[0])) {
				book.setMatchWords(searchKey[0].length());
			}
		} else {
			if (searchKey[0].equals(book.getName()) && searchKey[1].equals(book.getAuthor()))
				book.setMatchWords(MaxMatch);
			else {
				int matchWords = 0;
				if (book.getName().contains(searchKey[0]))
					matchWords += searchKey[0].length();
				else if (book.getAuthor().contains(searchKey[1]))
					matchWords += searchKey[1].length();
				book.setMatchWords(matchWords);
			}
		}
		if (book.getMatchWords() == 0)
			return false;// 匹配不好
		// book.setCover(matcher(html, config.coverReg));
		// book.setDetailUrl(matcher(html, config.detailUrlReg));
		// book.setType(matcher(html, config.typeReg));
		// book.setTip(matcher(html, config.tipsReg).replaceAll(Config.tagReg,
		// CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg).trim().replaceAll("\\s", " "));
		book.setUpdateTime(matcher(html, config.updateTimeReg).trim());
		book.setWords(Util.toInt(matcher(html, config.wordsReg)) * 1000);

		MyLog.i("Parser16K search a book " + book.getName() + "  " + book.getAuthor());
		books.add(book);
		return true;
	}

	private BookEntity processSearchDirectNode(String html, String directoryUrl) throws Exception {
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		book.setName(matcher(html, "<div\\s*class=\"title\">\\s*<h2>([^<]+)</h2>").replaceAll("\\s", CONSTANT.EMPTY));
		book.setDirectoryUrl(directoryUrl);
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return null;// 不完善的数据

		book.setAuthor(matcher(html, "<strong>作者</strong>：([^<]+)</span>").replaceAll("\\s", CONSTANT.EMPTY));
		book.setMatchWords(MaxMatch);
		book.setCover(matcher(html, config.coverReg));
		book.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
		book.setNewChapter(matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " "));
		book.setDetailUrl(directoryUrl);
		// book.setWords(0);
		// book.setType(null);
		// book.setUpdateTime(null);

		MyLog.i("Parser16K search a book " + book.getName() + "  " + book.getAuthor());
		return book;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html) {
		// http://www.16kxsw.com/16k/12/12217/index.html
		String id = matcher(url, "^http://www\\.16kxsw\\.com/16k/\\d+/(\\d+)/(index\\.html)?$");
		if (Util.isEmpty(id))
			return null;
		BookEntity book = null;
		try {
			SimpleNodeIterator iterator = getParserResult(html, "div class=\"one\"");
			MyLog.i("Parser16K Search ok is directory");
			if (iterator.hasMoreNodes()) {
				String detailHtml = iterator.nextNode().toHtml();
				book = processSearchDirectNode(detailHtml, url);
			}
		} catch (Exception e) {
		}
		if (book != null) {
			List<ChapterEntity> chaters = parserBookDict(html);
			return new BookAndChapters(book, chaters);
		}
		return null;
	}
}
