package com.lqy.abook.parser.site;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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

public class ParserBaidu extends ParserBase {

	private static Config config = Config.getBaiduConfig();

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
			boolean hasBookid = book.getDirectoryUrl().contains("book_id");

			String html = WebServer.hcGetData(book.getDirectoryUrl(), encodeType);
			html = matcher(html, "var\\s*Novel\\s*=\\s*\\{\\s*listData:\\s*(\\{[\\s\\S]+\\}),\\s*speed\\s*:\\s*\\{");
			Type type = new TypeToken<Map<String, Object>>() {
			}.getType();
			Map<String, Object> data = new Gson().fromJson(html, type);
			book.setName(Util.toString(data.get("title")));
			book.setCover(Util.toString(data.get("coverImage")).replaceAll(config.amp, "&"));
			book.setType(Util.toString(data.get("category")));
			book.setAuthor(Util.toString(data.get("author")));
			book.setSite(site);
			book.setTip(Util.toString(data.get("summary")));
			book.setWords(Util.toInt(data.get("total_wordsum")));
			long time = Util.toLongOr_1(data.get("last_chapter_update_time"));
			if (time != CONSTANT._1)
				time *= 1000;
			book.setUpdateTime(Util.formatDate(time, 10));
			book.setNewChapter(Util.toString(data.get("last_chapter_title")).trim());
			book.setCompleted(Util.toString(data.get("status")).contains("完"));// 未测试
			List<Map<String, String>> list = (List<Map<String, String>>) data.get("group");

			// String dicturl =
			// "http://m.baidu.com/tc?srd=1&appui=alaxs&ajax=4&id=wisenovel&pi="
			// + page + "&order=asc&gid=" + id;
			// book.setDirectoryUrl("http://m.baidu.com/tc?appui=alaxs&srct=zw&gid="
			// + id + "&srd=1&src=" + src);
			// if (!Util.isEmpty(book.getDetailUrl()) && !updateBook(book)) {
			// MyLog.i(TAG, "updateBookAndDict  此书没有更新");
			// return null;
			// }
			// List<ChapterEntity> chapters =
			// parserBookDict(book.getDirectoryUrl());
			// if (chapters == null || chapters.size() == 0) {
			// book.setLoadStatus(LoadStatus.failed);
			// MyLog.i(TAG, "updateBookAndDict getChapters failed");
			// return null;// 此书更新失败
			// } else {
			// return chapters;
			// }
		} catch (Exception e) {
			MyLog.e(e);
		}
		book.setLoadStatus(LoadStatus.failed);
		return null;
	}

	@Override
	public boolean parserBookDetail(BookEntity detail) {
		try {
			String text = toText(parseNodeByUrl(detail.getDirectoryUrl(), createEqualFilter("div class=\" s-hover  xs-sum-short\" data-action=\"summary\""),
					encodeType));
			if (!Util.isEmpty(text)) {
				MyLog.i(TAG, "parserBookDetail getParserResult ok");
				text = text.replaceAll("全部", CONSTANT.EMPTY);
				text = text.replaceAll("收起", CONSTANT.EMPTY);
				text = text.replaceAll("\\s", CONSTANT.EMPTY);
				// MyLog.i(html);
				detail.setTip(text);
			}
			return true;
		} catch (Exception e) {
			MyLog.i(e);
			return false;
		}
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		try {
			String html = WebServer.hcGetData(url, encodeType);
			return parserBookDictByHtml(html, new BookEntity());
		} catch (Exception e) {
			MyLog.e(e);
			return null;
		}
	}

	@Override
	public String getChapterDetail(String url) {
		return null;
	}

	public List<ChapterEntity> parserBookDictByHtml(String html, BookEntity book) throws Exception {
		// html = matcher(html,
		// "var\\s*Novel\\s*=\\s*\\{\\s*listData:\\s*(\\{[\\s\\S]+\\}),\\s*speed\\s*:\\s*\\{");
		// Type type = new TypeToken<Map<String, Object>>() {
		// }.getType();
		// Map<String, Object> data = new Gson().fromJson(html, type);
		// int count=Util.toInt(data.get("chapter_count"));
		// if (book != null) {// 获取书的信息
		// book.setName(Util.toString(data.get("title")));
		// book.setCover(Util.toString(data.get("coverImage")).replaceAll(config.amp,
		// "&"));
		// book.setType(Util.toString(data.get("category")));
		// book.setAuthor(Util.toString(data.get("author")));
		// book.setSite(site);
		// book.setTip(Util.toString(data.get("summary")));
		// book.setWords(Util.toInt(data.get("total_wordsum")));
		// long time = Util.toLongOr_1(data.get("last_chapter_update_time"));
		// if (time != CONSTANT._1)
		// time *= 1000;
		// book.setUpdateTime(Util.formatDate(time, 10));
		// book.setNewChapter(Util.toString(data.get("last_chapter_title")).trim());
		// book.setCompleted(Util.toString(data.get("status")).contains("完"));//
		// 未测试
		// }
		// if()
		// List<Map<String, String>> list = (List<Map<String, String>>)
		// data.get("group");
		// MyLog.i("parserBookDictByHtml ok " + book.toString());
		// List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
		// initChapterList(list, chapters);
		// for (int i = 0; i < array.length; i++) {
		//
		// }
		// return chapters;
		return null;
	}

	private void initChapterList(List<Map<String, String>> data, List<ChapterEntity> chapters) {
		ChapterEntity e;
		for (Map<String, String> map : data) {
			e = new ChapterEntity();
			if (e != null) {
				e.setName(Util.toString(map.get("text")).trim());
				e.setUrl(Util.toString(map.get("href")));
				e.setId(chapters.size());
				if (!Util.isEmpty(e.getName()))
					chapters.add(e);
			}
		}
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

		MyLog.i(TAG, "search a book " + book.getName() + "  " + book.getAuthor());
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

		MyLog.i(TAG, "search a book " + book.getName() + "  " + book.getAuthor());
		return book;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		if (url.startsWith("http://m.baidu.com/tc"))
			return new BookAndChapters((BookEntity) null, null);
		else
			return null;
	}
}
