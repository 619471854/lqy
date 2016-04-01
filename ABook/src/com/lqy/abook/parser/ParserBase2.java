package com.lqy.abook.parser;

import java.util.List;

import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;

public abstract class ParserBase2 extends ParserBase {

	protected abstract Config getConfig();

	/**
	 * 搜索小说
	 */
	public boolean parserSearch(List<BookEntity> books, String key) {
		Config config=getConfig();
		try {
			key = key.replaceAll("'", "").replaceAll("\\s", " ");// 去除单引号和多余的空格
			SimpleNodeIterator iterator = getParserResult(config.searchUrl + key, config.searchFilter);
			MyLog.i("Search ok,parsering");
			int count = 0;
			while (iterator.hasMoreNodes() && (count++ < searchMaxSizeSite || books.size() < searchMaxSizeSite)) {
				String html = iterator.nextNode().toHtml();
				boolean success = processSearchNode(books, html, key.split(" "));
				if (!success)
					break;// 如果未匹配，后面的就不要了
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// 解析 搜索小说
	protected abstract boolean processSearchNode(List<BookEntity> books, String html, String[] searchKey) throws Exception;

	/**
	 * 搜索小说所在的所在的site
	 */
	public boolean parserSearchSite(List<BookEntity> books, String name, String author) {
		Config config=getConfig();
		try {
			SimpleNodeIterator iterator = getParserResult(config.searchUrl + name + author, config.searchFilter);
			MyLog.i("SearchSite ok,parsering");
			while (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				// 完全匹配到了1个就可以了
				if (processSearchSiteNode(books, html, name, author))
					break;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 解析 搜索小说所在的所在的site
	 */
	protected boolean processSearchSiteNode(List<BookEntity> books, String html, String name, String author) throws Exception {
		Config config=getConfig();
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
		String authorHtml = matcher(html, config.authorReg);
		book.setAuthor(authorHtml.replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		// 如果有作者，那么必须完全匹配
		if (!name.equals(book.getName()) || !author.equals(book.getAuthor())) {
			return false;// 继续找第二本
		}
		book.setCover(matcher(html, config.coverReg));
		book.setDetailUrl(matcher(html, config.detailUrlReg));
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg));
		book.setUpdateTime(matcher(html, config.updateTimeReg));
		book.setDirectoryUrl(matcher(html, config.directoryUrlReg));

		books.add(book);
		return true;// 已找到
	}

}
