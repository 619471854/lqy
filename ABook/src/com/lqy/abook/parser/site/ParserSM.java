package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	protected static Config config = Config.getSMConfig();

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
			MyLog.i("ParserSM updateBook getParserResult ok");
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
				MyLog.i("ParserSM updateBookAndDict  此书没有更新");
				return null;
			}
			List<ChapterEntity> chapters = parserBookDict(book.getDirectoryUrl());
			if (chapters == null || chapters.size() == 0) {
				book.setLoadStatus(LoadStatus.failed);
				MyLog.i("ParserSM updateBookAndDict getChapters failed");
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
			MyLog.i("ParserSM parserBookDetail getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				detail.setTip(matcher(html, config.tipsDetailReg).replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY)
						.replace(Config.nbsp, " "));
				detail.setCompleted(matcher(html, config.completedReg).length() > 0);
				detail.setWords(Util.toInt(matcher(html, config.wordsReg)));

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
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = getParserResult(url, "li");
			MyLog.i("ParserSM parserBookDict getParserResult ok");
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
			MyLog.i("ParserSM asynGetChapterDetail getParserResult ok");
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

		MyLog.i("ParserSM search a book " + book.getName() + "  " + book.getAuthor());
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
		if (url.startsWith("http://m.sm.cn/nove")) {
			// http://m.sm.cn/novel/reader.php?uc_param_str=dnntnwvepffrgibijbprsv&from=novel_wap#catal/修罗武神/善良的蜜蜂
			if (url.startsWith("http://m.sm.cn/novel/reader.php")) {
				Matcher m = getMatcher(url, "^http://m\\.sm\\.cn/novel/reader\\.php?[^/]+/([^/]+)/([^/]+)$");
				if (m != null) {
					return new BookAndChapters(m.group(1), m.group(2));
				}
			} else if (url.startsWith("http://m.sm.cn/novelw/menu.php")) {
				// http://m.sm.cn/novelw/menu.php?uc_param_str=dnntnwvepffrgibijbprsv&from=novel_wap&title=修罗武神&author=善良的蜜蜂
				String name = matcher(url, "&title=([^&]+)");
				String author = matcher(url, "&author=([^&]+)");
				if (!Util.isEmpty(name) && !Util.isEmpty(author)) {
					return new BookAndChapters(name, author);
				}
			}
		}
		return null;
	}
}
