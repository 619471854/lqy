package com.lqy.abook.parser.site;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.htmlparser.Node;
import org.htmlparser.util.SimpleNodeIterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

public class ParserQidian extends ParserBase {
	private static Config config = Config.getQidianConfig();

	public ParserQidian() {
		encodeType = "utf-8";
		site = Site.Qidian;
	}

	@Override
	public boolean parserSearch(List<BookEntity> books, String key) {
		try {
			String html = WebServer.hcGetData(config.searchUrl + key, encodeType);
			SimpleNodeIterator iterator = parseHtml(html, createStartFilter(config.searchFilter));
			MyLog.i(TAG, "Search ok,parsering");
			int count = 0;
			String[] keys = key.split(" ");
			while (iterator.hasMoreNodes() && (count++ < searchMaxSizeSite || books.size() < searchMaxSizeSite)) {
				html = iterator.nextNode().toHtml();
				boolean success = processSearchNode(books, html, keys);
				if (!success)
					break;// 如果未匹配，后面的就不要了
			}
			return true;
		} catch (Exception e) {
			MyLog.i(e.toString());
			return false;
		}
	}

	@Override
	public BookEntity parserSearchSite(String name, String author) {
		try {
			String html = WebServer.hcGetData(config.searchUrl + name, encodeType);
			SimpleNodeIterator iterator = parseHtml(html, createStartFilter(config.searchFilter));

			MyLog.i(TAG, "SearchSite ok,parsering");
			while (iterator.hasMoreNodes()) {
				html = iterator.nextNode().toHtml();
				// 完全匹配到了1个就可以了
				BookEntity e = processSearchSiteNode(html, name, author);
				if (e != null)
					return e;
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 解析 搜索小说所在的所在的site
	 */
	protected BookEntity processSearchSiteNode(String html, String name, String author) throws Exception {
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		String id = matcher(html, "data-bid=\"(\\d+)\"");
		book.setDirectoryUrl("http://book.qidian.com/ajax/book/category?bookId=" + id);
		book.setDetailUrl("http://book.qidian.com/info/" + id);
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return null;// 不完善的数据
		book.setAuthor(matcher(html, config.authorReg).replaceAll("\\s", CONSTANT.EMPTY));

		MyLog.i(TAG, "processSearchSiteNode a book " + book.getName() + "  " + book.getAuthor());

		// 如果有作者，那么必须完全匹配
		if (!name.equals(book.getName()) || !author.equals(book.getAuthor())) {
			return null;// 继续找第二本
		}

		book.setCover(getQidianChildUrl(matcher(html, config.coverReg)));

		Matcher m = getMatcher(html, config.tipsReg);
		if (m != null) {
			book.setType(m.group(1).trim());
			book.setCompleted(m.group(2).trim().equals("完本"));
			book.setTip(m.group(3).replaceAll("\\s", CONSTANT.EMPTY));
		}
		m = getMatcher(html, config.newChapterReg);
		if (m != null) {
			book.setNewChapter(m.group(1).replaceAll("\\s", " "));
			book.setUpdateTime(m.group(2));
		}
		book.setWords((int) (Util.toFloat(matcher(html, config.wordsReg)) * 10000));

		return book;// 已找到
	}


	protected boolean processSearchNode(List<BookEntity> books, String html, String[] searchKey) throws Exception {
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		String id = matcher(html, "data-bid=\"(\\d+)\"");
		book.setDirectoryUrl("http://book.qidian.com/ajax/book/category?bookId=" + id);
		book.setDetailUrl("http://book.qidian.com/info/" + id);
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return false;// 不完善的数据
		book.setAuthor(matcher(html, config.authorReg).replaceAll("\\s", CONSTANT.EMPTY));

		if (searchKey.length == 1) {
			if (searchKey[0].equals(book.getName()) || searchKey[0].equals(book.getAuthor()))
				book.setMatchWords(MaxMatch);
			else
				book.setMatchWords(book.getName().contains(searchKey[0]) ? searchKey[0].length() : 0);
		} else {
			if (searchKey[0].equals(book.getName()) && searchKey[1].equals(book.getAuthor()))
				book.setMatchWords(MaxMatch);
			else
				book.setMatchWords((book.getName().contains(searchKey[0]) ? searchKey[0].length() : 0)
						+ (searchKey[1].equals(book.getAuthor()) ? searchKey[1].length() : 0));
		}
		book.setCover(getQidianChildUrl(matcher(html, config.coverReg)));

		Matcher m = getMatcher(html, config.tipsReg);
		if (m != null) {
			book.setType(m.group(1).trim());
			book.setCompleted(m.group(2).trim().equals("完本"));
			book.setTip(m.group(3).replaceAll("\\s", CONSTANT.EMPTY));
		}
		m = getMatcher(html, config.newChapterReg);
		if (m != null) {
			book.setNewChapter(m.group(1).replaceAll("\\s", " "));
			book.setUpdateTime(m.group(2));
		}
		book.setWords((int) (Util.toFloat(matcher(html, config.wordsReg)) * 10000));

		MyLog.i(TAG, "ParserQidian search a book " + book.getName() + "  " + book.getAuthor());
		books.add(book);
		return true;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		String html = toHtml(parseNode(book.getDetailUrl(), null, createStartFilter("div class=\"book-detail-wrap"), encodeType));
		if (Util.isEmpty(html))
			return false;
		String newChapter = matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " ");
		if (newChapter.equals(book.getNewChapter())) {
			return false;// 此书没有更新
		}
		book.setLoadStatus(LoadStatus.hasnew);
		book.setNewChapter(newChapter);
		book.setUpdateTime(matcher(html, config.updateTimeReg2).trim());
		float words = Util.toFloat(matcher(html, "<em>([\\d\\.]+)</em><cite>万字</cite>"));
		if (words > 0)
			book.setWords(((int) words) * 10000);

		return true;
	}

	@Override
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		if (Util.isEmpty(book.getDetailUrl())) {
			book.setLoadStatus(LoadStatus.failed);
			return null;
		}
		if (!updateBook(book)) {
			MyLog.i(TAG, "updateBookAndDict  此书没有更新");
			return null;
		}
		List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
		if (chapters == null || chapters.size() == 0) {
			book.setLoadStatus(LoadStatus.failed);
			MyLog.i(TAG, "updateBookAndDict getChapters failed");
			return null;// 此书更新失败
		} else {
			return chapters;
		}
	}

	@Override
	public boolean parserBookDetail(BookEntity book) {
		Node tipNode = parseNode(book.getDetailUrl(), null, createEqualFilter("div class=\"book-intro\""), encodeType);
		if (tipNode != null) {
			String tip = tipNode.toPlainTextString().trim();
			tip = tip.replaceAll("\r\n", "\n");
			tip = tip.replaceAll(Config.blank, CONSTANT.EMPTY);// 全角空格

			int index = tip.indexOf("作者自定义标签:");
			if (index != -1) {
				book.setTip(tip.substring(0, index).trim());
			} else {
				book.setTip(tip);
			}
			return true;
		}
		return false;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		try {
			String msg = WebServer.hcGetData(url, encodeType);
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> data = new Gson().fromJson(msg, type);
			data = (Map<String, Object>) data.get("data");
			List<Map<String, Object>> dirList = (List<Map<String, Object>>) data.get("vs");

			List<ChapterEntity> re = new ArrayList<ChapterEntity>();
			ChapterEntity e = null;
			String cUrl = null;
			String baseUrl = "http://read.qidian.com/chapter/";
			for (Map<String, Object> dir : dirList) {
				boolean isVip = Util.toFloat(dir.get("vS")) == 1;
				List<Map<String, Object>> chapterList = (List<Map<String, Object>>) dir.get("cs");
				for (Map<String, Object> chapter : chapterList) {
					String name = Util.toString(chapter.get("cN"));
					cUrl = null;
					if (!isVip) {
						if (chapter.get("cN") != null) {
							cUrl = baseUrl + chapter.get("cU").toString();
						}
					}
					if (!Util.isEmpty(name) && (isVip || !Util.isEmpty(cUrl))) {
						e = new ChapterEntity();
						e.setName(name);
						if (isVip)
							e.setLoadStatus(LoadStatus.vip);
						e.setUrl(cUrl);
						e.setId(re.size());
						re.add(e);
					}
				}
			}
			return re;
		} catch (Exception e) {
			MyLog.e(e);
		}

		return null;
	}

	@Override
	public String getChapterDetail(String url) {
		try {
			Node node = parseNodeByUrl(url, createEqualFilter("div class=\"read-content j_readContent\""), encodeType);
			if (node != null) {
				String t = node.toPlainTextString();
				t = t.replaceAll("\\s*", CONSTANT.EMPTY);
				t = t.replaceAll(Config.blank + Config.blank, "\n        ");// 替换2全角空格为8个半角空格
				return t.trim();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	@Override
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String detailUrl = null;
		String id = matcher(url, "http://m\\.qidian\\.com/book/(\\d+)");
		if (Util.isEmpty(id))
			id = matcher(url, "^http://book\\.qidian\\.com/info/(\\d+)$");
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://read\\.qidian\\.com/chapter/([\\w-]+)/([\\w-]+)$");
			if (Util.isEmpty(id))
				return null;
			detailUrl = parserBookDetailUrl(url, html);
		} else {
			detailUrl = "http://book.qidian.com/info/" + id;
		}

		BookEntity book = new BookEntity();
		book.setSite(site);
		book.setDetailUrl(detailUrl);
		id = matcher(detailUrl, "^http://book\\.qidian\\.com/info/(\\d+)$");
		if (Util.isEmpty(id))
			return null;
		book.setDirectoryUrl("http://book.qidian.com/ajax/book/category?bookId=" + id);

		if (!parserBookDetail(book, url.equals(detailUrl) ? html : null)) {
			return null;
		}

		List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
		if (chapters != null && chapters.size() > 0) {
			book.setNewChapter(chapters.get(chapters.size() - 1).getName());
			return new BookAndChapters(book, chapters);
		}
		return null;

	}

	private String parserBookDetailUrl(String directoryUrl, String allHtml) {
		String html = toHtml(parseNode(directoryUrl, allHtml, createEqualFilter("dd data-eid=\"qd_R85\""), encodeType));
		String url = matcher(html, "<a\\s*href=\"([^\"]+)\"");

		return getQidianChildUrl(url);
	}

	private boolean parserBookDetail(BookEntity book, String allHtml) {
		String html = toHtml(parseNode(book.getDetailUrl(), allHtml, createStartFilter("div class=\"book-detail-wrap"), encodeType));
		if (Util.isEmpty(html))
			return false;
		book.setCover(matcher(html, "<a\\s*class=\"J-getJumpUrl\"[^>]+>\\s*<img\\s*src=\"([^\"]+)\""));

		Matcher m = getMatcher(html, "<h1>\\s*<em>([^<]+)</em>\\s*<span>\\s*<a\\s*class=\"writer\"[^>]+>([^<]+)</a>");
		if (m == null)
			return false;
		book.setName(m.group(1));
		book.setAuthor(m.group(2));
		book.setWords((int) (Util.toFloat(matcher(html, "<em>([\\d\\.]+)</em><cite>万字</cite>")) * 10000));
		book.setNewChapter(matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " "));
		book.setUpdateTime(matcher(html, config.updateTimeReg2).trim());

		Node tipNode = parseNodeByHtml(html, createEqualFilter("div class=\"book-intro\""));
		if (tipNode != null) {
			String tip = tipNode.toPlainTextString().trim();
			tip = tip.replaceAll("\r\n", "\n");
			tip = tip.replaceAll(Config.blank, CONSTANT.EMPTY);// 全角空格

			int index = tip.indexOf("作者自定义标签:");
			if (index != -1) {
				book.setTip(tip.substring(0, index).trim());
				book.setType(tip.substring(index + 8).replaceAll("\\s*", " ").trim());
			} else {
				book.setTip(tip);
			}
		}

		return true;
	}

	private String getQidianChildUrl(String childUrl) {
		if (Util.isEmpty(childUrl))
			return childUrl;
		return "http:" + childUrl;
	}

}
