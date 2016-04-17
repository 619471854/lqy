package com.lqy.abook.parser.site;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Span;
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
	protected static Config config = Config.getQidianConfig();

	public ParserQidian() {
		encodeType = "utf-8";
		site = Site.Qidian;
	}

	@Override
	public boolean parserSearch(List<BookEntity> books, String key) {
		try {
			String msg = WebServer.getDataByUrlConnection(config.searchUrl + key, encodeType);
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> data = new Gson().fromJson(msg, type);
			data = (Map<String, Object>) data.get("Data");
			data = (Map<String, Object>) data.get("search_response");
			List<Map<String, String>> list = (List<Map<String, String>>) data.get("books");
			String keys[] = key.split(" ");
			for (Map<String, String> map : list) {
				processSearchNode(books, map, keys, null, null);
			}
			return true;
		} catch (Exception e) {
			MyLog.i("ParserQidian parserSearch Search error " + e.toString());
			return false;
		}
	}

	@Override
	public BookEntity parserSearchSite(String name, String author) {
		try {
			String msg = WebServer.getDataByUrlConnection(config.searchUrl + name + "+" + author, encodeType);
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> data = new Gson().fromJson(msg, type);
			data = (Map<String, Object>) data.get("Data");
			data = (Map<String, Object>) data.get("search_response");
			List<Map<String, String>> list = (List<Map<String, String>>) data.get("books");
			for (Map<String, String> map : list) {
				BookEntity e = processSearchNode(null, map, null, name, author);
				if (e != null) {
					return e;
				}
			}
		} catch (Exception e) {
			MyLog.i("ParserQidian parserSearchSite error " + e.toString());
		}
		return null;
	}

	protected BookEntity processSearchNode(List<BookEntity> books, Map<String, String> item, String[] searchKey, String name, String author) throws Exception {
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		book.setName(item.get(config.nameReg));
		String directorUrl = String.format(config.directoryUrlReg, item.get("bookid"), item.get("internalsiteid"));
		book.setDirectoryUrl(directorUrl);// siteId
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return null;// 不完善的数据
		book.setAuthor(item.get(config.authorReg));

		if (searchKey != null) {
			if (searchKey[0].equals(book.getName()))
				book.setMatchWords(MaxMatch);
			else if (searchKey.length == 1)
				book.setMatchWords(searchKey[0].length());
			else
				book.setMatchWords(searchKey[0].length() + searchKey[1].length());

			MyLog.i("ParserQidian search a book " + book.getName() + "  " + book.getAuthor());

		} else if (!name.equals(book.getName()) || !author.equals(book.getAuthor())) {
			return null;// 继续找第二本
		}

		book.setCover(item.get(config.coverReg));
		book.setDetailUrl(item.get(config.detailUrlReg));
		book.setType(item.get(config.typeReg));
		book.setTip(Util.toString(item.get(config.tipsReg)).replaceAll(Config.blank, " ").trim());
		book.setWords(Util.toInt(item.get(config.wordsReg)));
		book.setNewChapter(item.get(config.newChapterReg));
		long time = Util.toLongOr_1(item.get(config.updateTimeReg));
		if (time != CONSTANT._1)
			time *= 1000;
		book.setUpdateTime(Util.formatDate(time, 10));
		// "1": "出版中","2": "封 笔","3": "已完成","4": "已经完本", "5": "情节展开","6":
		// "接近尾声", "7": "新书上传","8": "暂 停", "9": "精彩纷呈", "10": "连载中"
		String status = item.get(config.completedReg);
		book.setCompleted("3".equals(status) || "4".equals(status));

		if (books != null)
			books.add(book);
		return book;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		try {
			String html = WebServer.hcGetData(book.getDetailUrl(), encodeType);

			SimpleNodeIterator iterator = parseHtml(html, createStartFilter("div class=\"updata_cont\" id=\"readV\""));
			MyLog.i("ParserQidian updateBook getParserResult ok");

			if (iterator.hasMoreNodes()) {
				String html1 = iterator.nextNode().toHtml();
				String newChapter = matcher(html1, config.newChapterReg2).trim().replaceAll("\\s", " ");
				// MyLog.i("ParserQidian updateBook newChapter="+newChapter);
				if (newChapter.equals(book.getNewChapter())) {
					return false;// 此书没有更新
				}
				book.setLoadStatus(LoadStatus.hasnew);
				book.setNewChapter(newChapter);
				book.setUpdateTime(matcher(html1, config.updateTimeReg2));

				iterator = parseHtml(html, createEqualFilter("div class=\"info_box\""));
				if (iterator.hasMoreNodes()) {
					html = iterator.nextNode().toHtml();
					book.setCompleted(matcher(html, "<span\\s*itemprop=\"updataStatus\">([^<]+)</span>").indexOf("完本") != -1);
					book.setWords(Util.toInt(matcher(html, "<span\\s*itemprop=\"wordCount\">(\\d+)</span>")));
				}
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
				MyLog.i("ParserQidian updateBookAndDict  此书没有更新");
				return null;
			}
			List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
			if (chapters == null || chapters.size() == 0) {
				book.setLoadStatus(LoadStatus.failed);
				MyLog.i("ParserQidian updateBookAndDict getChapters failed");
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
			NodeFilter filter = new NodeFilter() {
				public boolean accept(Node node) {
					String text = node.getText();
					return text.equals("span itemprop=\"description\"") || text.startsWith("a itemprop=\"url\" stat-type");
				}
			};
			SimpleNodeIterator iterator = parseUrl(detail.getDetailUrl(), filter, encodeType);
			MyLog.i("ParserQidian parserBookDetail getParserResult ok");
			while (iterator.hasMoreNodes()) {
				Node node = iterator.nextNode();
				if (node instanceof Span) {
					String tip = node.toPlainTextString();
					tip = tip.replaceAll(config.nbsp, CONSTANT.EMPTY);
					tip = tip.replaceAll("\\s", CONSTANT.EMPTY);
					tip = tip.replaceAll(Config.blank, CONSTANT.EMPTY);// 全角空格
					detail.setTip(tip);
				} else if (node instanceof LinkTag) {
					String directoryUrl = ((LinkTag) node).getLink();
					if (!Util.isEmpty(directoryUrl))
						detail.setDirectoryUrl(directoryUrl);
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
			SimpleNodeIterator iterator = parseUrl(url, createStartFilter("li style='width:33%;' itemprop='chapter'"), encodeType);
			MyLog.i("ParserQidian parserBookDict getParserResult ok");
			ChapterEntity chapter;
			while (iterator.hasMoreNodes()) {
				Node node = iterator.nextNode();
				try {
					LinkTag a = (LinkTag) node.getFirstChild();
					chapter = new ChapterEntity();
					chapter.setName(a.toPlainTextString().trim().replaceAll("\\s", " "));
					chapter.setUrl(a.getLink());
					chapter.setId(chapters.size());
					if (!Util.isEmpty(chapter.getName()))
						chapters.add(chapter);
				} catch (Exception e) {
				}
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
			SimpleNodeIterator iterator = parseUrl(url, createStartFilter("div class=\"bookreadercontent\""), encodeType);
			MyLog.i("ParserQidian asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				Matcher m = getMatcher(html, "<script\\ssrc='([^']+)'\\s*charset='([^']+)'");
				if (m == null)
					return null;
				MyLog.i("ParserQidian asynGetChapterDetail " + m.group(1));

				String text = WebServer.hcGetData(m.group(1), m.group(2));
				m = getMatcher(text, "^document.write\\('([\\s\\S]+)'\\);$");
				if (m != null)
					text = m.group(1);

				text = text.replaceAll("&amp;", CONSTANT.EMPTY);
				text = text.replaceAll("&lt;", CONSTANT.EMPTY);
				text = text.replaceAll("&gt;", CONSTANT.EMPTY);
				int i = text.indexOf("<a href=http://www.qidian.com>");
				if (i != -1)
					text = text.substring(0, i);
				text = text.replaceAll(Config.lineWrapReg2, "\n");
				text = text.replaceAll(Config.blank, "    ");// 替换全角空格为4个半角空格
				text = text.replaceAll(Config.nbsp, "  ");
				text = text.replaceAll("\n{2,}+", "\n");
				return text.trim();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html) {
		String id = matcher(url, "http://m\\.qidian\\.com/book/bookchapterlist\\.aspx\\?bookid=(\\d+)");
		if (Util.isEmpty(id))
			id = matcher(url, "http://m\\.qidian\\.com/book/showbook\\.aspx\\?bookid=(\\d+)");
		if (Util.isEmpty(id))
			id = matcher(url, "http://www\\.qidian\\.com/Book/(\\d+)\\.aspx");
		if (Util.isEmpty(id)) {
			id = matcher(url, "http://read\\.qidian\\.com/BookReader/(\\w+)\\.aspx");
			if (Util.isEmpty(id))
				return null;
			id = null;
		} else {
			url = String.format(config.directoryUrlReg, id, 1);
		}
		BookEntity book = new BookEntity();
		if (id != null)
			book.setDetailUrl("http://www.qidian.com/Book/" + id + ".aspx");
		book.setDirectoryUrl(url);
		if (parserBookDetail(book)) {
			book.setSite(site);
			List<ChapterEntity> chaters = null;
			if (id == null) {
				chaters = parserBookDict(html);
			} else {
				chaters = parserBookDict(url);
			}
			return new BookAndChapters(book, chaters);
		}
		return null;
	}

}
