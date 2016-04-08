package com.lqy.abook.parser;

import java.util.List;

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
		return parserSearch(books, config.searchUrl + key, key.split(" "), false);
	}

	protected boolean parserSearch(List<BookEntity> books, String url, String[] keys, boolean isStartOrEqual) {
		Config config = getConfig();
		try {
			SimpleNodeIterator iterator = null;
			if (isStartOrEqual)
				iterator = getParserResult(url, new NodeClassFilter(BodyTag.class), encodeType);
			else
				iterator = getParserResult(url, config.searchFilter);
			MyLog.i("Search ok,parsering");
			int count = 0;
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
	public boolean parserSearchSite(List<BookEntity> books, String name, String author) {
		Config config = getConfig();
		return parserSearchSite(books, config.searchUrl + name + " " + author, name, author, false);
	}

	protected boolean parserSearchSite(List<BookEntity> books, String url, String name, String author, boolean isStartOrEqual) {
		Config config = getConfig();
		try {
			SimpleNodeIterator iterator = null;
			if (isStartOrEqual)
				iterator = getParserResult2(url, config.searchFilter);
			else
				iterator = getParserResult(url, config.searchFilter);

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
		Config config = getConfig();
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setDirectoryUrl(matcher(html, config.directoryUrlReg));
		if (Util.isEmpty(book.getName()) || Util.isEmpty(book.getDirectoryUrl()))
			return false;// 不完善的数据
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

		books.add(book);
		return true;// 已找到
	}

}
