package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.LinkTag;
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

public class ParserDSB extends ParserBase3 {
	private static Config config = Config.getdDsbConfig();

	public ParserDSB() {
		encodeType = "gbk";
		site = Site.DSB;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	protected boolean setDetailUrl(BookEntity book) {
		String url = book.getDirectoryUrl();
		if (url == null)
			return true;
		url = url.replace("/index.html", ".htm");
		url = url.replace("book", "jieshaoinfo");
		book.setDetailUrl(url);

		return false;
	}

	@Override
	public boolean parserBookDetail(BookEntity book) {
		Node node = parseNodeByUrl(book.getDetailUrl(), createEqualFilter("section class=\"bookinfo\""), encodeType);
		while (node != null) {
			if ("div class=\"info_m\"".equals(node.getText())) {
				book.setWords(Util.toInt(matcher(node.toHtml(), config.wordsReg2)));
			} else if ("div class=\"info_c\"".equals(node.getText())) {
				String html = node.toHtml();
				Node tipNode = parseNodeByHtml(html, createStartFilter("div class=\"bookintro\""));
				if (tipNode != null) {
					String tip = tipNode.toPlainTextString();
					tip = tip.replace("......展开全部", CONSTANT.EMPTY);
					tip = tip.replace("展开全部", CONSTANT.EMPTY);
					tip = tip.replaceAll(Config.nbsp, CONSTANT.EMPTY);
					tip = tip.replaceAll("\\s", CONSTANT.EMPTY);
					book.setTip(tip);
				}
				book.setNewChapter(matcher(html, config.newChapterReg2).trim());
				return true;
			}
			node = node.getNextSibling();
		}
		return false;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		Node node = parseNodeByUrl(book.getDetailUrl(), createEqualFilter("section class=\"bookinfo\""), encodeType);
		while (node != null) {
			String txt = node.getText();
			MyLog.i("updateBook" + txt);
			if ("div class=\"info_m\"".equals(txt)) {
				book.setWords(Util.toInt(matcher(node.toHtml(), config.wordsReg2)));
			} else if ("div class=\"info_c\"".equals(txt)) {
				String html = node.toHtml();
				String newChapter = matcher(html, config.newChapterReg2).trim();
				if (newChapter.equals(book.getNewChapter())) {
					return false;// 此书没有更新
				}
				Node tipNode = parseNodeByHtml(html, createStartFilter("div class=\"bookintro\""));
				if (tipNode != null) {
					String tip = tipNode.toPlainTextString();
					tip = tip.replace("......展开全部", CONSTANT.EMPTY);
					tip = tip.replace("展开全部", CONSTANT.EMPTY);
					tip = tip.replaceAll(Config.nbsp, CONSTANT.EMPTY);
					tip = tip.replaceAll("\\s", CONSTANT.EMPTY);
					book.setTip(tip);
				}
				book.setNewChapter(matcher(html, config.newChapterReg2));
				return true;
			}
			node = node.getNextSibling();
		}
		return false;
	}

	@Override
	public List<ChapterEntity> updateBookAndDict(BookEntity book) {
		try {
			if (!Util.isEmpty(book.getDetailUrl()) && !updateBook(book)) {
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
		} catch (Exception e) {
			MyLog.e(e);
		}
		book.setLoadStatus(LoadStatus.failed);
		return null;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		return parserBookDict(url, null);
	}

	public List<ChapterEntity> parserBookDict(String url, String html) {
		if (Util.isEmpty(url))
			return null;
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = parseIterator(url, html, createEqualFilter("dd"), encodeType);
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			ChapterEntity e;
			Node node;
			while (iterator.hasMoreNodes()) {
				node = iterator.nextNode();
				node = node.getLastChild();
				if (node instanceof LinkTag) {
					LinkTag tag = (LinkTag) node;
					e = new ChapterEntity();
					e.setName(tag.toPlainTextString().trim());
					e.setUrl(getChildUrl(url, tag.getLink()));
					e.setId(chapters.size());
					if (!Util.isEmpty(e.getName()))
						chapters.add(e);
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
		String text = toText(parseNodeByUrl(url, createEqualFilter("div class=\"yd_text2\""), encodeType));
		if (!Util.isEmpty(text)) {
			MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
			text = text.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\n        ");
			text = text.replaceAll(Config.nbsp, "  ");
			text = text.replaceAll("\r\n", "\n");
			text = text.replaceAll("\n{2,}+", "\n");
			return text.trim();
		}
		return null;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		BookEntity book = null;
		String id = matcher(url, "^http://www\\.dashubao\\.net/book/(\\d+/\\d+)/index\\.html$");
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://www\\.dashubao\\.net/jieshaoinfo/(\\d+/\\d+)\\.htm$");
		}
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://m\\.dashubao\\.net/info-(\\d+)/$");
			if (Util.isEmpty(id)) {
				id = matcher(url, "^http://m\\.dashubao\\.net/wapbook-(\\d+)_?\\d*/$");
			}
			if (Util.isEmpty(id))
				return null;
			html = null;
			book = new BookEntity();
			book.setDetailUrl("http://www.dashubao.net/jieshaoinfo/0/" + id + ".htm");
			// 这里不设置 directoryUrl，获取详情时更新directoryUrl和detailUrl
		} else {
			book = new BookEntity();
			book.setDetailUrl("http://www.dashubao.net/jieshaoinfo/" + id + ".htm");
			book.setDirectoryUrl("http://www.dashubao.net/book/" + id + "/index.html");
		}
		MyLog.i(TAG, "parserBrowser ok");
		book.setSite(site);
		if (parserBookDetail(book, book.getDetailUrl().equals(url) ? html : null)) {
			String directUrl = book.getDirectoryUrl();
			if (Util.isEmpty(directUrl)) {
				MyLog.i(TAG, "parserBrowser directUrl is empty");
				return null;
			}
			List<ChapterEntity> chaters = parserBookDict(directUrl, directUrl.equals(url) ? html : null);
			return new BookAndChapters(book, chaters);
		}
		return null;
	}

	public boolean parserBookDetail(BookEntity book, String html) {
		NodeFilter filter = createEqualFilter("section class=\"bookinfo\"");
		Node node = parseNode(book.getDetailUrl(), html, filter, encodeType);
		MyLog.i(TAG, "parserBookDetail  ok");
		while (node != null) {
			String txt = node.getText();
			if ("div class=\"info_t\"".equals(txt)) {
				html = node.toHtml();
				book.setName(matcher(node.toHtml(), "<h1>([^<]+)</h1>"));
				book.setAuthor(matcher(node.toHtml(), "<span\\s*class=\"author\">作者：([^<]+)</span>"));
			} else if ("div class=\"info_m\"".equals(txt)) {
				html = node.toHtml();
				book.setType(matcher(node.toHtml(), "<span>类别：([^<]+)</span>"));
				book.setUpdateTime(matcher(node.toHtml(), "<span>更新：(\\d\\d\\d\\d-\\d\\d-\\d\\d)</span>"));
				book.setWords(Util.toInt(matcher(node.toHtml(), config.wordsReg2)));
			} else if ("div class=\"info_l\"".equals(txt)) {
				html = node.toHtml();
				book.setCover(matcher(html, config.coverReg));
			} else if ("div class=\"info_c\"".equals(txt)) {
				html = node.toHtml();
				book.setNewChapter(matcher(html, config.newChapterReg2).trim());
				// 手机版网页解析出来没有地址，需要获取directoryUrl
				if (Util.isEmpty(book.getDirectoryUrl())) {
					String directoryUrl = matcher(html, "<div\\s*class=\"lastzj\">[^<]+<a\\s*href=\"([^\"]+)\">");
					if (!Util.isEmpty(directoryUrl)) {
						directoryUrl = directoryUrl.substring(0, directoryUrl.lastIndexOf("/"));
						if (Util.isEmpty(directoryUrl)) {
							return false;
						} else {
							directoryUrl = directoryUrl + "/index.html";
							book.setDirectoryUrl(directoryUrl);
							directoryUrl = directoryUrl.replace("/index.html", ".htm");
							directoryUrl = directoryUrl.replace("book", "jieshaoinfo");
							book.setDetailUrl(directoryUrl);
						}
					}
				}
				Node tipNode = parseNodeByHtml(html, createStartFilter("div class=\"bookintro\""));
				if (tipNode != null) {
					String tip = tipNode.toPlainTextString();
					tip = tip.replace("......展开全部", CONSTANT.EMPTY);
					tip = tip.replace("展开全部", CONSTANT.EMPTY);
					tip = tip.replaceAll(Config.nbsp, CONSTANT.EMPTY);
					tip = tip.replaceAll("\\s", CONSTANT.EMPTY);
					book.setTip(tip);
				}
				return true;
			}
			node = node.getNextSibling();
		}
		return false;
	}
}
