package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
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

public class ParserSM extends ParserBase2 {
	private static Config config = Config.getSMConfig();

	public ParserSM() {
		encodeType = "utf-8";
		site = Site.SM;
	}

	@Override
	protected Config getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	@Override
	public boolean updateBook(BookEntity book) {
		try {
			SimpleNodeIterator iterator = getParserResult(book.getDetailUrl(), "div id=\"content\"");
			MyLog.i(TAG, "updateBook getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				String newChapter = matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " ");
				if (newChapter.equals(book.getNewChapter())) {
					return false;// 此书没有更新
				}
				book.setLoadStatus(LoadStatus.hasnew);
				book.setNewChapter(newChapter);
				book.setCompleted(matcher(html, config.completedReg).length() > 0);
				book.setWords(Util.toInt(matcher(html, config.wordsReg)));
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
		try {
			SimpleNodeIterator iterator = getParserResult(detail.getDetailUrl(), "div id=\"content\"");
			MyLog.i(TAG, "parserBookDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY)
						.replace(Config.nbsp, " "));
				detail.setCompleted(matcher(html, config.completedReg).length() > 0);
				detail.setWords(Util.toInt(matcher(html, config.wordsReg2)));

				detail.setNewChapter(matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " "));
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
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = parseIterator(url, allHtml, createEqualFilter("li"), encodeType);
			MyLog.i(TAG, "parserBookDict getParserResult ok");
			ChapterEntity chapter;
			String urlRoot = "http://www.shenmaxiaoshuo.com";
			while (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				chapter = new ChapterEntity();
				try {
					Pattern p = Pattern.compile("<a\\s*href=\"([^\"]+)\">(((?!</a>)[\\s\\S])+)</a>");
					Matcher m = p.matcher(html);
					if (m.find()) {
						chapter.setName(m.group(2).trim());
						chapter.setUrl(urlRoot + m.group(1).trim());
					}
				} catch (Exception e) {
				}
				chapter.setId(chapters.size());
				if (!Util.isEmpty(chapter.getName()))
					chapters.add(chapter);
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
			SimpleNodeIterator iterator = getParserResult(url, "div id=\"htmlContent\" class=\"contentbox\"");
			MyLog.i(TAG, "asynGetChapterDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				html = matcher(html, "<div\\s*class=\"ad250left\">(((?!</div>)[\\s\\S])+)</div>(((?!更多手打全文字章节请到)[\\s\\S])+)更多手打全文字章节请到", 3);
				html = html.replaceAll(Config.lineWrapReg, "\n");
				html = html.replaceAll("\r\n", "\n");
				html = html.replaceAll("\n{2,}+", "\n");
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
	public BookAndChapters parserBrowser(String url, String html) {
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

	private boolean parserBookDetail(BookEntity detail, String allHtml) {
		try {
			NodeFilter filter = createEqualFilter("div id=\"content\"");
			Node node = parseNode(detail.getDetailUrl(), allHtml, filter, encodeType);
			MyLog.i(TAG, "parserBookDetail getParserResult ok");
			if (node != null) {
				String html = node.toHtml();
				detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY)
						.replace(Config.nbsp, " "));
				detail.setCompleted(matcher(html, config.completedReg).length() > 0);
				detail.setWords(Util.toInt(matcher(html, config.wordsReg2)));

				detail.setNewChapter(matcher(html, config.newChapterReg2).trim().replaceAll("\\s", " "));
				detail.setUpdateTime(matcher(html, config.updateTimeReg2));
			}
			if (node != null)
				node = node.getFirstChild();
			while (node != null) {
				String txt = node.getText();
				if ("div class=\"wrapper_src\"".equals(txt)) {
					String html = node.toPlainTextString().replaceAll("\\s", CONSTANT.EMPTY).replaceAll("&gt;", ">");
					Matcher m = getMatcher(html, "[^>]+>([^>]+)>*([^>]+)>([^>]+)");
					if (m != null) {
						detail.setName(m.group(3));
						detail.setAuthor(m.group(2));
						detail.setType(m.group(1));
					}
					node = node.getNextSibling();
				} else if ("div class=\"box mainintro\"".equals(txt)) {
					String html = node.toHtml();
					detail.setCover(matcher(html, config.coverReg));
					break;
				} else {
					node = node.getNextSibling();
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
