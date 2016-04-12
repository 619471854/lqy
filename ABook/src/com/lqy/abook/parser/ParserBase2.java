package com.lqy.abook.parser;

import java.util.List;

import org.htmlparser.NodeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public abstract class ParserBase2 extends ParserBase {

	protected abstract Config getConfig();

	@Override
	public boolean parserSearch(List<BookEntity> books, String key) {
		Config config = getConfig();
		try {
			SimpleNodeIterator iterator = getParserResult(config.searchUrl + key, config.searchFilter);
			MyLog.i("Search ok,parsering");
			int count = 0;
			String[] keys = key.split(" ");
			while (iterator.hasMoreNodes() && (count++ < searchMaxSizeSite || books.size() < searchMaxSizeSite)) {
				String html = iterator.nextNode().toHtml().substring(15000);
				boolean success = processSearchNode(books, html, keys);
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

	@Override
	public BookEntity parserSearchSite(String name, String author) {
		Config config = getConfig();
		try {
			SimpleNodeIterator iterator = getParserResult(config.searchUrl + name + " " + author, config.searchFilter);

			MyLog.i("SearchSite ok,parsering");
			while (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
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
		Config config = getConfig();
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setDirectoryUrl(matcher(html, config.directoryUrlReg));
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return null;// 不完善的数据
		String authorHtml = matcher(html, config.authorReg);
		book.setAuthor(authorHtml.replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
		// 如果有作者，那么必须完全匹配
		if (!name.equals(book.getName()) || !author.equals(book.getAuthor())) {
			return null;// 继续找第二本
		}
		book.setCover(matcher(html, config.coverReg));
		book.setDetailUrl(matcher(html, config.detailUrlReg));
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg));
		book.setUpdateTime(matcher(html, config.updateTimeReg));

		return book;// 已找到
	}

}
