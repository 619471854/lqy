package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatusEnum;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.parser.Config;
import com.lqy.abook.parser.ParserBase2;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class ParserSM extends ParserBase2 {
	private static Config config = Config.getSMConfig();

	public ParserSM() {
		encodeType = "utf-8";
		site = SiteEnum.SM;
	}

	@Override
	protected Config getConfig() {
		return config;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		try {
			String html = toHtml(parseNode(book.getDetailUrl(), null, createEqualFilter("div id=\"content\""), "gbk"));
			if (!Util.isEmpty(html)) {
				MyLog.i(TAG, "updateBook updateBook ok");
				String newChapter = matcher(html, config.newChapterReg2).trim().replaceAll("\\s*", " ");
				if (newChapter.equals(book.getNewChapter())) {
					return false;// 此书没有更新
				}
				book.setLoadStatus(LoadStatusEnum.hasnew);
				book.setNewChapter(newChapter);
				book.setCompleted(matcher(html, config.completedReg).length() > 0);
				book.setWords(Util.toInt(matcher(html, config.wordsReg2)));
				book.setUpdateTime(matcher(html, config.updateTimeReg2));
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
				MyLog.i(TAG, "updateBookAndDict  此书没有更新");
				return null;
			}
			List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
			if (chapters == null || chapters.size() == 0) {
				book.setLoadStatus(LoadStatusEnum.failed);
				MyLog.i(TAG, "updateBookAndDict getChapters failed");
				return null;// 此书更新失败
			} else {
				return chapters;
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		book.setLoadStatus(LoadStatusEnum.failed);
		return null;
	}

	@Override
	public boolean parserBookDetail(BookEntity detail) {
		try {
			String html = toHtml(parseNode(detail.getDetailUrl(), null, createEqualFilter("div id=\"content\""), "gbk"));
			if (!Util.isEmpty(html)) {
				MyLog.i(TAG, "parserBookDetail updateBook ok");
				detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.lineWrapReg, "\n").replaceAll("\\s+", CONSTANT.EMPTY)
						.replace(Config.nbsp, " ").trim());
				detail.setCompleted(matcher(html, config.completedReg).length() > 0);
				detail.setWords(Util.toInt(matcher(html, config.wordsReg2)));

				detail.setNewChapter(matcher(html, config.newChapterReg2).trim().replaceAll("\\s*", " "));
				detail.setUpdateTime(matcher(html, config.updateTimeReg2));
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		return parserBookDict(url, null);
	}

	private List<ChapterEntity> parserBookDict(String url, String allHtml) {
		try {
			String html = toHtml(parseNode(url, allHtml, createEqualFilter("div id=\"list\""), "gbk"));
			if (Util.isEmpty(html))
				return null;
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = parseHtml(html, new NodeClassFilter(LinkTag.class));
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			ChapterEntity chapter;
			Node node = null;
			LinkTag link = null;
			String baseUrl = "http://www.shenmabook.com";
			while (iterator.hasMoreNodes()) {
				node = iterator.nextNode();
				if (node instanceof LinkTag) {
					link = (LinkTag) node;
					chapter = new ChapterEntity();
					if (!Util.isEmpty(link.getLinkText())) {
						chapter.setName(link.getLinkText().trim());
					}
					if (!Util.isEmpty(link.getLink())) {
						chapter.setUrl(baseUrl + link.getLink());
					}
					chapter.setId(chapters.size());
					if (!Util.isEmpty(chapter.getName()))
						chapters.add(chapter);
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
			Node node = parseNodeByUrl(url, createEqualFilter("div id=\"content\""), "gbk");
			if (node != null) {
				String t = node.toPlainTextString();
				t = t.replaceAll("\\s*", CONSTANT.EMPTY);
				t = t.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\n        ");
				return t.trim();
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
				book.setMatchWords(matchWords(nameHtml) + matchWords(authorHtml));
		} else {
			if (searchKey[0].equals(book.getName()) && searchKey[1].equals(book.getAuthor()))
				book.setMatchWords(MaxMatch);
			else
				book.setMatchWords(matchWords(nameHtml) + matchWords(authorHtml));
		}

		book.setCover(matcher(html, config.coverReg));
		book.setDetailUrl(book.getDirectoryUrl().replace("ml", "xx"));
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg).trim().replaceAll("\\s", " "));
		book.setUpdateTime(matcher(html, config.updateTimeReg));

		MyLog.i(TAG, "search a book " + book.getName() + "  " + book.getAuthor());
		books.add(book);
		return true;
	}

	private int matchWords(String html) {
		try {
			Pattern p = Pattern.compile(config.keyReg);
			Matcher m = p.matcher(html);
			while (m.find()) {
				String result = m.group(1).trim();
				// MyLog.i(result);
				return result.length() + matchWords(html.replace(m.group(), CONSTANT.EMPTY));
			}
		} catch (Exception e) {
		}
		return 0;
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public BookAndChapters parserBrowser(String url, String html, String cookie) {
		String id = matcher(url, "^http://www\\.shenmabook\\.com/xx-(\\d+)/$");
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://www\\.shenmabook\\.com/ml-(\\d+)/$");
		}
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://m\\.shenmabook\\.com/xx-(\\d+)/$");
			html = null;
		}
		if (Util.isEmpty(id)) {
			id = matcher(url, "^http://m\\.shenmabook\\.com/ml-(\\d+)/$");
			html = null;
		}
		if (Util.isEmpty(id))
			return null;
		String detailUrl = "http://www.shenmabook.com/xx-" + id + "/";
		String directUrl = "http://www.shenmabook.com/ml-" + id + "/";
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

	public boolean parserBookDetail(BookEntity detail, String allHtml) {
		try {
			String html = toHtml(parseNode(detail.getDetailUrl(), null, createEqualFilter("div id=\"content\""), "gbk"));
			if (!Util.isEmpty(html)) {
				MyLog.i(TAG, "parserBookDetail getParserResult ok");

				detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.lineWrapReg, "\n").replaceAll("\\s+", CONSTANT.EMPTY)
						.replace(Config.nbsp, " ").trim());
				detail.setCompleted(matcher(html, config.completedReg).length() > 0);
				detail.setWords(Util.toInt(matcher(html, config.wordsReg2)));

				detail.setNewChapter(matcher(html, config.newChapterReg2).trim().replaceAll("\\s*", " "));
				detail.setUpdateTime(matcher(html, config.updateTimeReg2));

				detail.setName(matcher(html, "<img[\\s\\S]+?alt=\"(\\S+)\""));
				detail.setAuthor(matcher(html, "<p\\s*class=\"author\">作者：(\\S+)\\s*</p>"));
				detail.setType(matcher(html, "<p\\s*class=\"infosort\">分类：(\\S+)\\s*点击"));
				detail.setCover(matcher(html, config.coverReg));
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
