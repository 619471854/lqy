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
		return config;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		try {
			String html = toHtml(parseNodeByUrl(book.getDetailUrl(), createEqualFilter("dl class=\"NewsChapter\""), encodeType));
			MyLog.i(TAG, "updateBook getParserResult ok");
			if (Util.isEmpty(html))
				return false;
			String newChapter = null;
			SimpleNodeIterator iterator = parseHtml(html, createStartFilter("a href="));
			while (iterator.hasMoreNodes()) {
				LinkTag chapterNode = (LinkTag) iterator.nextNode();
				newChapter = chapterNode.getLinkText().trim();
			}
			if (book.getNewChapter().equals(newChapter)) {
				return false;// 此书没有更新
			}
			book.setLoadStatus(LoadStatus.hasnew);
			book.setNewChapter(newChapter);
			book.setWords(Util.toInt(matcher(html, config.wordsReg2)));

		} catch (Exception e) {
			MyLog.e(e);
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
	public boolean parserBookDetail(BookEntity detail) {
		return parserBookDetail(detail, null);
	}

	public boolean parserBookDetail(BookEntity detail, String allHtml) {
		try {
			if (Util.isEmpty(detail.getName())) {
				NodeFilter filter = createEqualFilter("div class=\"bLeft\"");
				Node node = Util.isEmpty(allHtml) ? parseNodeByUrl(detail.getDetailUrl(), filter, encodeType) : parseNodeByHtml(allHtml, filter);
				MyLog.i(TAG, "parserBookDetail getParserResult ok");
				if (node != null)
					node = node.getFirstChild();
				while (node != null) {
					String txt = node.getText();
					if ("div class=\"BookInfo\"".equals(txt)) {
						String html = node.toHtml();
						detail.setName(matcher(html, "<img\\s*class=\"book\"\\s*src=\"[^\"]+\"\\s*alt=\"([^\"]+)\"/>"));
						detail.setCover(matcher(html, "<img\\s*class=\"book\"\\s*src=\"([^\"]+)\""));

						String tip = matcher(html, "<div\\s*class=\"cont\"\\s*style=\"display: block;\">\\s*<a[^>]+>(((?!</a>).)+)</a>");
						detail.setTip(tip.replaceAll(Config.lineWrapReg, "\n"));
						detail.setWords(Util.toInt(matcher(html, "<em\\s*class=\"red\">(\\d+)</em>")));

						node = node.getNextSibling();
					} else if ("dl class=\"NewsChapter\"".equals(txt)) {
						String html = node.toHtml();
						SimpleNodeIterator iterator = parseHtml(html, createStartFilter("a href="));
						while (iterator.hasMoreNodes()) {
							LinkTag chapterNode = (LinkTag) iterator.nextNode();
							detail.setNewChapter(chapterNode.getLinkText().trim());
						}
						node = node.getParent().getNextSibling();
					} else if ("div class=\"bRight\"".equals(txt)) {
						node = node.getFirstChild();
					} else if ("div class=\"AuthorInfo\"".equals(txt)) {
						node = node.getFirstChild();
					} else if ("div class=\"author\"".equals(txt)) {
						detail.setAuthor(matcher(node.toHtml(), "<a\\s*class=\"name[^>]+>([^<]+)</a>").trim());
						return true;
					} else {
						node = node.getNextSibling();
					}
				}
			} else {
				SimpleNodeIterator iterator = getParserResult(detail.getDetailUrl(), "div class=\"cont\" style=\"display: block;\"");
				MyLog.i(TAG, "parserBookDetail getParserResult ok");
				if (iterator.hasMoreNodes()) {
					String tip = iterator.nextNode().getChildren().elementAt(1).toHtml();
					tip = matcher(tip, config.tipsDetailReg);
					tip = tip.replaceAll(Config.lineWrapReg, "\n");
					detail.setTip(tip);
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<ChapterEntity> parserBookDict(String url) {
		return parserBookDict(url, null);
	}

	public List<ChapterEntity> parserBookDict(String url, String allHtml) {
		if (Util.isEmpty(url))
			return null;
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			NodeFilter filter = new NodeFilter() {
				public boolean accept(Node node) {
					return node instanceof LinkTag && "dd".equals(node.getParent().getText());
				}
			};
			SimpleNodeIterator iterator = parseIterator(url, allHtml,filter, encodeType);
			
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			ChapterEntity e;
			while (iterator.hasMoreNodes()) {
				Node node = iterator.nextNode();
				if (node instanceof LinkTag) {
					LinkTag tag = (LinkTag) node;
					e = new ChapterEntity();
					e.setName(tag.toPlainTextString().trim());
					e.setId(chapters.size());
					boolean isVip = tag.toHtml().indexOf("alt=\"vip\"") != -1;
					if (isVip) {
						e.setLoadStatus(LoadStatus.vip);
					} else {
						e.setUrl(getChildUrl(url, tag.getLink()));
					}
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
		try {
			SimpleNodeIterator iterator = getParserResult(url, "div id=\"chapterContentWapper\"");
			MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
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

		MyLog.i(TAG, "search a book " + book.getName() + "  " + book.getAuthor());
		books.add(book);
		return true;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String id = matcher(url, "^http://www\\.17k\\.com/list/(\\d+)\\.html$");
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://www\\.17k\\.com/book/(\\d+)\\.html$");
		}
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://h5\\.17k\\.com/list/(\\d+)\\.html$");
			html = null;
		}
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://h5\\.17k\\.com/book/(\\d+)\\.html$");
			html = null;
		}
		if (Util.isEmpty(id))
			return null;
		String detailUrl = "http://www.17k.com/book/" + id + ".html";
		String directUrl = "http://www.17k.com/list/" + id + ".html";
		BookEntity book = new BookEntity();
		book.setDetailUrl(detailUrl);
		book.setDirectoryUrl(directUrl);
		if (parserBookDetail(book, detailUrl.equals(url) ? html : null)) {
			book.setSite(site);
			List<ChapterEntity> chaters = parserBookDict(directUrl, directUrl.equals(url) ? html : null);
			return new BookAndChapters(book, chaters);
		}
		return null;
	}
}
